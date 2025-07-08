package tech.mayanksoni.safebrowsing.repository.mongo;

import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import tech.mayanksoni.safebrowsing.documents.TrancoFile;
import tech.mayanksoni.safebrowsing.documents.TrancoProvidedDomain;
import tech.mayanksoni.safebrowsing.exceptions.ListEntryNotFound;
import tech.mayanksoni.safebrowsing.mapper.TrancoDomainMapper;
import tech.mayanksoni.safebrowsing.models.DomainFeatures;
import tech.mayanksoni.safebrowsing.models.PossibleReferenceDomain;
import tech.mayanksoni.safebrowsing.models.TrancoCSVFileRecord;
import tech.mayanksoni.safebrowsing.models.TrancoProvidedDomainEntity;
import tech.mayanksoni.safebrowsing.repository.TrancoProvidedDomainRepository;
import tech.mayanksoni.safebrowsing.utils.DomainUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TrancoDomainRepositoryMongoImpl implements TrancoProvidedDomainRepository {
    private final MongoTemplate mongoTemplate;
    private final TrancoDomainMapper TRANCO_MAPPER;

    @Override
    public int countDomainsByListId(String listId) {
        return 0;
    }

    @Override
    public void insertDomainsFromCSVRecords(String listId, List<TrancoCSVFileRecord> csvRecords) {
        List<DomainFeatures> processedDomains = csvRecords.stream().map(DomainUtils::extractDomainFeatures).filter(Objects::nonNull).toList();
        this.inserDomainsFromDomainFeatures(listId, processedDomains);
    }

    @Override
    public void inserDomainsFromDomainFeatures(String listId, List<DomainFeatures> domains) {
        Query listExistSelectionQuery = Query.query(Criteria.where("listId").is(listId));
        boolean listExists = this.mongoTemplate.exists(listExistSelectionQuery, TrancoFile.class);
        if (listExists) {
            List<TrancoProvidedDomain> domainDocuments = domains.stream().map(feature -> toTrancoProvidedDomain(listId, feature)).toList();
            this.mongoTemplate.insertAll(domainDocuments);
        } else {
            throw new ListEntryNotFound("The list entry could not be located in DB");
        }
    }

    @Override
    public void purgeAllDomains() {
        this.mongoTemplate.dropCollection(TrancoProvidedDomainEntity.class);
    }

    @Override
    public void purgeDomainsByListId(String listId) {
        Query domainsSelectionUsingListId = Query.query(Criteria.where("listId").is(listId));
        DeleteResult deleteResult = this.mongoTemplate.remove(domainsSelectionUsingListId, TrancoProvidedDomain.class);
        log.debug("Removed all domains from listId {}, Delete Count: {}", listId, deleteResult.getDeletedCount());
    }

    @Override
    public Optional<TrancoProvidedDomainEntity> getExactMatchDomain(String ownerDomain, String registrySuffix) {
        Query exactMatchDomainSelectionQuery = Query.query(Criteria.where("domain").is(ownerDomain).and("registrySuffix").is(registrySuffix));
        return Optional.ofNullable(this.mongoTemplate.findOne(exactMatchDomainSelectionQuery, TrancoProvidedDomain.class)).map(TRANCO_MAPPER::toTrancoProvidedDomainEntity);
    }

    @Override
    public List<PossibleReferenceDomain> getPossibleReferenceDomainsForTyposquattingValidation(int domainLengthLowerLimit, int domainLengthUpperLimit, char firstCharacter, String registrySuffix) {
        Aggregation aggregationPipeline = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("active").is(true)),
                Aggregation.lookup("trancoProvidedDomain", "listId", "listId", "domains"),
                Aggregation.unwind("domains"),
                Aggregation.match(Criteria.where("domains.domainLength")
                        .gte(domainLengthLowerLimit)
                        .lte(domainLengthUpperLimit)
                        .and("domains.registrySuffix").is(registrySuffix)
                        .and("domains.domainFirstCharacter").is(firstCharacter)
                ),
                Aggregation.project()
                        .and("domains.registrySuffix").as("registrySuffix")
                        .and("domains.domainLength").as("ownerDomainLength")
                        .and("domains.domain").as("ownerDomain")
        );
        List<PossibleReferenceDomain> possibleReferenceDomains = this.mongoTemplate.aggregate(aggregationPipeline, TrancoFile.class, PossibleReferenceDomain.class).getMappedResults();
        log.debug("Found {} possible reference domains", possibleReferenceDomains.size());
        return possibleReferenceDomains;
    }


    private TrancoProvidedDomain toTrancoProvidedDomain(String listId, DomainFeatures domainFeatures) {
        return TrancoProvidedDomain.builder()
                .id(UUID.randomUUID().toString())
                .domain(domainFeatures.getPrivateDomain())
                .domainFirstCharacter(domainFeatures.getPrivateDomain().charAt(0))
                .domainLength(domainFeatures.getPrivateDomain().length())
                .ownerDomainSoundexCode(domainFeatures.getPhoneticModel().soundexCode())
                .ownerDomainMetaphoneCode(domainFeatures.getPhoneticModel().metaphoneCode())
                .ownerDomainDoubleMetaphoneCode(domainFeatures.getPhoneticModel().doubleMetaphoneCode())
                .domainRank(domainFeatures.getDomainRankFromTranco())
                .listId(listId)
                .registrySuffix(domainFeatures.getRegistrySuffix())
                .build();
    }
}
