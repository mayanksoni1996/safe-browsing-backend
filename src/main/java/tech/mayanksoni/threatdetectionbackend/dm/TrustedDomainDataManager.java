package tech.mayanksoni.threatdetectionbackend.dm;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.mayanksoni.threatdetectionbackend.models.TrancoDomainEntry;
import tech.mayanksoni.threatdetectionbackend.models.TrustedDomain;

import java.util.List;

public interface TrustedDomainDataManager {
    Mono<Long> countTrustedDomains();
    Mono<Void> truncateTrustedDomains();
    void removeTrustedDomain(String domainName);
    Mono<Void> addTrustedDomain(Flux<TrancoDomainEntry> domains);
    Flux<TrustedDomain> getTrustedDomains();
    Flux<TrustedDomain> getTrustedDomainByTldAndDomain(String tld, String domain);
    Flux<TrustedDomain> getTrustedDomainsByTLD(String tld, String domain);
    Mono<TrustedDomain> getTrustedDomainByDomainName(String domainName);
    Mono<Boolean> isTrustedDomain(String domainName);

}
