package tech.mayanksoni.safebrowsing.repository;

import org.springframework.stereotype.Repository;
import tech.mayanksoni.safebrowsing.models.DomainFeatures;
import tech.mayanksoni.safebrowsing.models.PossibleReferenceDomain;
import tech.mayanksoni.safebrowsing.models.TrancoCSVFileRecord;
import tech.mayanksoni.safebrowsing.models.TrancoProvidedDomainEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrancoProvidedDomainRepository {
    int countDomainsByListId(String listId);

    void insertDomainsFromCSVRecords(String listId, List<TrancoCSVFileRecord> csvRecords);

    void inserDomainsFromDomainFeatures(String listId, List<DomainFeatures> domains);

    void purgeAllDomains();

    void purgeDomainsByListId(String listId);

    Optional<TrancoProvidedDomainEntity> getExactMatchDomain(String ownerDomain, String registrySuffix);

    List<PossibleReferenceDomain> getPossibleReferenceDomainsForTyposquattingValidation(int domainLengthLowerLimit, int domainLengthUpperLimit, char firstCharacter, String registrySuffix);

}
