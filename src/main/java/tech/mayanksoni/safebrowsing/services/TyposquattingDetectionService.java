package tech.mayanksoni.safebrowsing.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.mayanksoni.safebrowsing.configuration.SafeBrowsignConfig;
import tech.mayanksoni.safebrowsing.models.DomainFeatures;
import tech.mayanksoni.safebrowsing.models.EditDistanceRecordForDomain;
import tech.mayanksoni.safebrowsing.models.PossibleReferenceDomain;
import tech.mayanksoni.safebrowsing.models.TyposquattingValidationResponse;
import tech.mayanksoni.safebrowsing.models.dto.DomainTyposquattingValidationRequest;
import tech.mayanksoni.safebrowsing.repository.TrancoProvidedDomainRepository;
import tech.mayanksoni.safebrowsing.utils.DomainUtils;
import tech.mayanksoni.safebrowsing.utils.EditDistanceUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TyposquattingDetectionService {
    private final TrancoProvidedDomainRepository trancoProvidedDomainRepository;
    private final SafeBrowsignConfig safeBrowsignConfig;

    private static EditDistanceRecordForDomain computeEditDistance(String domainUnderTest, String referenceDomain) {
        return new EditDistanceRecordForDomain(
                EditDistanceUtil.computeLevenshteinDistance(domainUnderTest, referenceDomain),
                domainUnderTest,
                referenceDomain
        );
    }

    public TyposquattingValidationResponse checkDomainForTyposquatting(DomainTyposquattingValidationRequest validationRequest) {
        List<EditDistanceRecordForDomain> editDistanceLessThanThreshold = computeEditDistances(validationRequest);
        boolean isExactMatchPresent = editDistanceLessThanThreshold.stream().anyMatch(s -> s.editDistance() == 0);
        log.info("is Domain an Exact Match: {}", isExactMatchPresent);
        if (!isExactMatchPresent && !editDistanceLessThanThreshold.isEmpty()) {
            EditDistanceRecordForDomain closestMatchingDomainEditDistanceRecord = editDistanceLessThanThreshold.stream().min(Comparator.comparingInt(EditDistanceRecordForDomain::editDistance)).get();
            log.info("Closest matching domain edit distance record: {}", closestMatchingDomainEditDistanceRecord);
            return new TyposquattingValidationResponse(
                    true,
                    closestMatchingDomainEditDistanceRecord.referenceDomain(),
                    validationRequest.getDomain(),
                    safeBrowsignConfig.isPhoneticMatchingEnabled()
            );
        }
        return new TyposquattingValidationResponse(
                false,
                null,
                validationRequest.getDomain(),
                safeBrowsignConfig.isPhoneticMatchingEnabled()
        );
    }

    public List<EditDistanceRecordForDomain> computeEditDistances(DomainTyposquattingValidationRequest validationRequest) {
        try {
            DomainFeatures domainFeaturesForDomainToValidate = DomainUtils.extractDomainFeatures(validationRequest.getDomain(), 0);
            Objects.requireNonNull(domainFeaturesForDomainToValidate);
            int domainLengthLowerLimit = domainFeaturesForDomainToValidate.getPrivateDomain().length() - safeBrowsignConfig.getEditDistanceThreshold();
            int domainLengthUpperLimit = domainFeaturesForDomainToValidate.getPrivateDomain().length() + safeBrowsignConfig.getEditDistanceThreshold();
            char firstCharacter = domainFeaturesForDomainToValidate.getPrivateDomain().charAt(0);
            String registrySuffix = domainFeaturesForDomainToValidate.getRegistrySuffix();
            List<PossibleReferenceDomain> possibleDomainMatches = this.trancoProvidedDomainRepository.getPossibleReferenceDomainsForTyposquattingValidation(domainLengthLowerLimit, domainLengthUpperLimit, firstCharacter, registrySuffix);
            return possibleDomainMatches.parallelStream().map(s -> computeEditDistance(domainFeaturesForDomainToValidate.getPrivateDomain(), s.ownerDomain())).filter(s -> s.editDistance() <= safeBrowsignConfig.getEditDistanceThreshold()).sorted(Comparator.comparingInt(EditDistanceRecordForDomain::editDistance)).toList();
        } catch (NullPointerException e) {
            log.error("Invalid domain name {}", validationRequest.getDomain(), e);
            return List.of();
        }
    }
}
