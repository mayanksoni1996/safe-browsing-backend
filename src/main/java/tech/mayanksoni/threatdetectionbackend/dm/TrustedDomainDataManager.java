package tech.mayanksoni.threatdetectionbackend.dm;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.mayanksoni.threatdetectionbackend.models.TrustedDomain;

import java.util.List;

public interface TrustedDomainDataManager {
    Mono<Long> countTrustedDomains();
    Mono<Void> truncateTrustedDomains();
    void addTrustedDomain(String domainName);
    void removeTrustedDomain(String domainName);
    @Deprecated
    void addTrustedDomain(List<String> domains);
    Mono<Void> addTrustedDomain(Flux<String> domains);
    Flux<TrustedDomain> getTrustedDomains();
    Flux<TrustedDomain> getTrustedDomainByTldAndDomain(String tld, String domain);
    Flux<TrustedDomain> getTrustedDomainsByTLD(String tld);
    Mono<TrustedDomain> getTrustedDomainByDomainName(String domainName);
    Mono<Boolean> isTrustedDomain(String domainName);

}
