package tech.mayanksoni.threatdetectionbackend.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.mayanksoni.threatdetectionbackend.configuration.ThreatDetectionConfig;
import tech.mayanksoni.threatdetectionbackend.models.DomainTyposquattingValidationResults;
import tech.mayanksoni.threatdetectionbackend.models.DomainValidationResponse;
import tech.mayanksoni.threatdetectionbackend.models.StateModel;
import tech.mayanksoni.threatdetectionbackend.services.StateManagementService;
import tech.mayanksoni.threatdetectionbackend.services.TyposquattingDetectionService;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainCheckProcessorTest {

    @Mock
    private TyposquattingDetectionService typosquattingDetectionService;

    @Mock
    private StateManagementService stateManagementService;

    @Mock
    private ThreatDetectionConfig threatDetectionConfig;

    @InjectMocks
    private DomainCheckProcessor domainCheckProcessor;

    @Test
    void checkDomain_shouldReturnMonoWithResults() {
        // Given
        String domainName = "example.com";
        String stateId = UUID.randomUUID().toString();
        boolean isTyposquatted = true;
        String closestMatchingDomain = "exampie.com";
        String ipAddress = "127.0.0.1";
        int editDistance = 1;

        DomainTyposquattingValidationResults mockResults = new DomainTyposquattingValidationResults(
                isTyposquatted, domainName, closestMatchingDomain, editDistance);

        // Mock state management service to return empty (no existing state)
        when(stateManagementService.getStateById(stateId))
                .thenReturn(Mono.empty());

        // Mock typosquatting detection service
        when(typosquattingDetectionService.checkDomainForTypoSquatting(domainName))
                .thenReturn(Mono.just(mockResults));

        // Mock state creation
        StateModel mockStateModel = new StateModel(
                stateId, domainName, null, false, false, Instant.now().plusSeconds(86400)
        );
        when(stateManagementService.createStateModel(any(), eq(domainName), any(), eq(false),eq(false)))
                .thenReturn(Mono.just(mockStateModel));

        // When
        Mono<DomainValidationResponse> result = domainCheckProcessor.checkDomain(domainName, stateId, ipAddress);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    DomainTyposquattingValidationResults validationResults = response.typosquattingValidationResults();
                    StateModel stateModel = response.stateModel();
                    return validationResults != null &&
                           validationResults.isTyposquatted() == isTyposquatted && 
                           validationResults.domainName().equals(domainName) &&
                           validationResults.closestMatchingDomain().equals(closestMatchingDomain) &&
                           validationResults.editDistance() == editDistance &&
                           stateModel != null &&
                           stateModel.state().equals(stateId) &&
                           stateModel.domainName().equals(domainName);
                })
                .verifyComplete();
    }

    @Test
    void checkDomain_shouldHandleEmptyMono() {
        // Given
        String domainName = "example.com";
        String stateId = UUID.randomUUID().toString();

        // Mock state management service to return empty (no existing state)
        when(stateManagementService.getStateById(stateId))
                .thenReturn(Mono.empty());

        // Mock typosquatting detection service to return empty
        when(typosquattingDetectionService.checkDomainForTypoSquatting(domainName))
                .thenReturn(Mono.empty());

        // When
        Mono<DomainValidationResponse> result = domainCheckProcessor.checkDomain(domainName, stateId, null);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }
}
