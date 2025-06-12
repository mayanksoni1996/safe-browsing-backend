package tech.mayanksoni.threatdetectionbackend.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.http.client.reactive.AbstractClientHttpConnectorProperties;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.mayanksoni.threatdetectionbackend.configuration.ThreatDetectionConfig;
import tech.mayanksoni.threatdetectionbackend.models.DomainTyposquattingValidationResults;
import tech.mayanksoni.threatdetectionbackend.models.DomainValidationResponse;
import tech.mayanksoni.threatdetectionbackend.services.StateManagementService;
import tech.mayanksoni.threatdetectionbackend.services.TyposquattingDetectionService;

import java.util.List;

/**
 * Processor for checking domains against typosquatting threats.
 * Provides methods for checking individual domains and batches of domains.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DomainCheckProcessor {
    private final TyposquattingDetectionService typosquattingDetectionService;
    private final StateManagementService stateManagementService;
    private final ThreatDetectionConfig threatDetectionConfig;
    private final AbstractClientHttpConnectorProperties abstractClientHttpConnectorProperties;

    /**
     * Checks a single domain for typosquatting.
     * 
     * @param domainName The domain name to check
     * @param stateId the state UUID associated with the domain check
     * @return A Mono containing the validation results
     */
    public Mono<DomainValidationResponse> checkDomain(String domainName, String stateId, String ipAddress) {
        log.info("Checking domain for typosquatting: {}", domainName);
        return stateManagementService.getStateById(stateId).map(state -> {
            if(state.accessAllowed()){
                log.info("Access allowed for domain: {} by state id : {}", domainName, stateId);
            }else{
                log.info("Access not allowed for domain: {} by state id : {}", domainName, stateId);
            }
            return DomainValidationResponse.builder()
                    .stateModel(state)
                    .typosquattingValidationResults(null)
                    .build();
        }).switchIfEmpty(typosquattingDetectionService.checkDomainForTypoSquatting(domainName).flatMap(result -> {
            log.info("Domain {} checked for typosquatting, results: {}", domainName, result);
            boolean accessAllowed = !result.isTyposquatted() || !result.isPhoneticMatch();
            return stateManagementService.createStateModel(stateId, domainName, ipAddress, false,accessAllowed)
                    .map(stateModel -> DomainValidationResponse.builder()
                            .stateModel(stateModel)
                            .typosquattingValidationResults(result)
                            .build());
        })).doOnError(e -> log.error("Error checking domain {} for typosquatting: {}", domainName, e.getMessage()));
        }
    /**
     * Checks multiple domains for typosquatting in parallel.
     * The processing will be done in parallel if parallel processing is enabled in the configuration.
     * 
     * @param domainNames List of domain names to check
     * @return A Flux of validation results
     */
    public Flux<DomainTyposquattingValidationResults> checkDomains(List<String> domainNames) {
        if (domainNames == null || domainNames.isEmpty()) {
            log.warn("Empty domain list provided for batch checking");
            return Flux.empty();
        }

        log.info("Checking {} domains for typosquatting", domainNames.size());

        // Process in batches if the list is large
        if (domainNames.size() > threatDetectionConfig.getBatchSize()) {
            log.info("Processing domains in batches of {}", threatDetectionConfig.getBatchSize());

            // Split the list into batches
            return Flux.fromIterable(domainNames)
                    .buffer(threatDetectionConfig.getBatchSize())
                    .flatMap(typosquattingDetectionService::checkDomainsForTypoSquattingInParallel);
        }

        // Process the entire list at once if it's small enough
        return typosquattingDetectionService.checkDomainsForTypoSquattingInParallel(domainNames);
    }
}
