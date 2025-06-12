package tech.mayanksoni.threatdetectionbackend.dm.impl.mongo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.mayanksoni.threatdetectionbackend.configuration.ThreatDetectionConfig;
import tech.mayanksoni.threatdetectionbackend.dm.TrustedDomainDataManager;
import tech.mayanksoni.threatdetectionbackend.documents.TrustedDomainDocument;
import tech.mayanksoni.threatdetectionbackend.mappers.TrustedDomainMapper;
import tech.mayanksoni.threatdetectionbackend.models.TrancoDomainEntry;
import tech.mayanksoni.threatdetectionbackend.models.TrustedDomain;
import tech.mayanksoni.threatdetectionbackend.utils.DomainUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
@Slf4j
@RequiredArgsConstructor
public class TrustedDomainMongoDataManagerImpl implements TrustedDomainDataManager {
    private final ReactiveMongoTemplate mongoTemplate;
    private final TrustedDomainMapper TRUSTED_DOMAIN_MAPPER;
    private final ThreatDetectionConfig threatDetectionConfig;

    @Override
    public Mono<Long> countTrustedDomains() {
        return mongoTemplate.count(new Query(), TrustedDomainDocument.class)
                .doOnSuccess(count -> log.info("Counted {} trusted domains", count))
                .doOnError(e -> log.error("Error counting trusted domains: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> truncateTrustedDomains() {
       return mongoTemplate.dropCollection(TrustedDomainDocument.class).doOnSuccess(success -> log.info("Truncated trusted domains"));
    }

    private TrustedDomainDocument createTrustedDomainDocument(TrancoDomainEntry domainEn) {
        return TrustedDomainDocument.builder()
                .domainName(domainEn.domainName())
                .domainRank(domainEn.rank())
                .tld(DomainUtils.extractTLDFromDomain(domainEn.domainName()))
                .build();
    }

    @Override
    public void removeTrustedDomain(String domainName) {
        Query query = Query.query(Criteria.where("tld").is(DomainUtils.extractTLDFromDomain(domainName)).and("domainName").is(domainName));
        this.mongoTemplate.remove(query, TrustedDomainDocument.class).subscribe();
        log.info("Removed trusted domain: {}", domainName);
    }



    @Override
    public Mono<Void> addTrustedDomain(Flux<TrancoDomainEntry> domains) {
        return domains.map(this::createTrustedDomainDocument)
                .buffer(threatDetectionConfig.getBatchSize())
                .flatMap(mongoTemplate::insertAll)
                .doOnComplete(() -> log.info("Successfully added trusted domains"))
                .doOnError(e -> log.error("Error adding trusted domains: {}", e.getMessage()))
                .then();
    }

    private List<List<String>> createBatches(List<String> domains, int batchSize) {
        return IntStream.range(0, (domains.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> domains.subList(
                        i * batchSize, 
                        Math.min((i + 1) * batchSize, domains.size())))
                .collect(Collectors.toList());
    }

    @Override
    public Flux<TrustedDomain> getTrustedDomains() {
        return mongoTemplate.findAll(TrustedDomainDocument.class).map(TRUSTED_DOMAIN_MAPPER::toModel);
    }

    @Override
    public Flux<TrustedDomain> getTrustedDomainByTldAndDomain(String tld, String domain) {
        Query query = Query.query(Criteria.where("tld").is(tld).and("domainName").is(domain));
        return mongoTemplate.find(query, TrustedDomainDocument.class)
                .map(TRUSTED_DOMAIN_MAPPER::toModel)
                .doOnComplete(() -> log.info("Retrieved trusted domain for TLD: {}, domain: {}", tld, domain))
                .doOnError(e -> log.error("Error retrieving trusted domain: {}", e.getMessage()))
                ;
    }
    @Override
    public Flux<TrustedDomain> getTrustedDomainsByTLD(String tld) {
        Query query = Query.query(Criteria.where("tld").is(tld));
        return mongoTemplate.find(query, TrustedDomainDocument.class)
                .map(TRUSTED_DOMAIN_MAPPER::toModel)
                .doOnComplete(() -> log.info("Retrieved trusted domains for TLD: {}", tld));
    }

    @Override
    public Mono<TrustedDomain> getTrustedDomainByDomainName(String domainName) {
        Query query = Query.query(Criteria.where("tld").is(DomainUtils.extractTLDFromDomain(domainName)).and("domainName").is(domainName)).with(Sort.by(Sort.Direction.ASC, "domainRank"));
        return mongoTemplate.findOne(query, TrustedDomainDocument.class)
                .map(TRUSTED_DOMAIN_MAPPER::toModel)
                .doOnSuccess(domain -> log.info("Retrieved trusted domain: {}", domainName))
                .doOnError(e -> log.error("Error retrieving trusted domain: {}", e.getMessage()));
    }

    @Override
    public Mono<Boolean> isTrustedDomain(String domainName) {
        Mono<TrustedDomain> trustedDomainMono = getTrustedDomainByDomainName(domainName);
        return trustedDomainMono.hasElement().defaultIfEmpty(false);
    }
}
