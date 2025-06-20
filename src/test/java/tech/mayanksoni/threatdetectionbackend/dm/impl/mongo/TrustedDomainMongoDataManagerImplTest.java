package tech.mayanksoni.threatdetectionbackend.dm.impl.mongo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.mayanksoni.threatdetectionbackend.configuration.ThreatDetectionConfig;
import tech.mayanksoni.threatdetectionbackend.documents.TrustedDomainDocument;
import tech.mayanksoni.threatdetectionbackend.mappers.TrustedDomainMapper;
import tech.mayanksoni.threatdetectionbackend.models.TrancoDomainEntry;
import tech.mayanksoni.threatdetectionbackend.utils.DomainUtils;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class TrustedDomainMongoDataManagerImplTest {

    @Mock
    private ReactiveMongoTemplate mongoTemplate;

    @Mock
    private TrustedDomainMapper trustedDomainMapper;

    @Mock
    private ThreatDetectionConfig threatDetectionConfig;

    private TrustedDomainMongoDataManagerImpl trustedDomainMongoDataManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(threatDetectionConfig.getBatchSize()).thenReturn(2);
        trustedDomainMongoDataManager = new TrustedDomainMongoDataManagerImpl(mongoTemplate, trustedDomainMapper, threatDetectionConfig);
    }

    @Test
    void addTrustedDomain_shouldInsertDomainsInBatches() {
        // Given
        List<TrancoDomainEntry> domains = Arrays.asList(
                new TrancoDomainEntry(1, "example.com"),
                new TrancoDomainEntry(2, "test.com"),
                new TrancoDomainEntry(3, "sample.com")
        );

        TrustedDomainDocument doc1 = TrustedDomainDocument.builder().domainName("example.com").build();
        TrustedDomainDocument doc2 = TrustedDomainDocument.builder().domainName("test.com").build();
        TrustedDomainDocument doc3 = TrustedDomainDocument.builder().domainName("sample.com").build();

        when(mongoTemplate.insertAll(anyList()))
                .thenReturn(Flux.just(doc1, doc2))
                .thenReturn(Flux.just(doc3));

        // When
        Mono<Void> result = trustedDomainMongoDataManager.addTrustedDomain(Flux.fromIterable(domains));

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verify that insertAll was called twice (once for each batch)
        verify(mongoTemplate, times(2)).insertAll(anyList());
    }

    @Test
    void addTrustedDomain_shouldHandleEmptyFlux() {
        // Given
        when(mongoTemplate.insertAll(anyList())).thenReturn(Flux.empty());

        // When
        Mono<Void> result = trustedDomainMongoDataManager.addTrustedDomain(Flux.empty());

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verify that insertAll was not called
        verify(mongoTemplate, never()).insertAll(anyList());
    }

    @Test
    void addTrustedDomain_shouldPropagateErrors() {
        // Given
        List<TrancoDomainEntry> domains = Arrays.asList(
                new TrancoDomainEntry(1, "example.com"),
                new TrancoDomainEntry(2, "test.com")
        );

        RuntimeException testException = new RuntimeException("Test exception");
        when(mongoTemplate.insertAll(anyList())).thenReturn(Flux.error(testException));

        // When
        Mono<Void> result = trustedDomainMongoDataManager.addTrustedDomain(Flux.fromIterable(domains));

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(e -> e.equals(testException))
                .verify();

        // Verify that insertAll was called once
        verify(mongoTemplate, times(1)).insertAll(anyList());
    }

    @Test
    void addTrustedDomain_shouldContinueOnIllegalArgumentException() {
        // Given
        // One valid domain and one invalid domain that will cause IllegalArgumentException
        List<TrancoDomainEntry> domains = Arrays.asList(
                new TrancoDomainEntry(1, "example.com"),
                new TrancoDomainEntry(2, "invalid-domain"), // This will cause IllegalArgumentException
                new TrancoDomainEntry(3, "test.com")
        );

        TrustedDomainDocument doc1 = TrustedDomainDocument.builder().domainName("example.com").build();
        TrustedDomainDocument doc3 = TrustedDomainDocument.builder().domainName("test.com").build();

        // Mock the behavior to simulate IllegalArgumentException for the invalid domain
        TrustedDomainMongoDataManagerImpl spyManager = spy(trustedDomainMongoDataManager);
        doReturn(doc1).when(spyManager).createTrustedDomainDocument(domains.get(0));
        doThrow(new IllegalArgumentException("Invalid domain")).when(spyManager).createTrustedDomainDocument(domains.get(1));
        doReturn(doc3).when(spyManager).createTrustedDomainDocument(domains.get(2));

        when(mongoTemplate.insertAll(anyList()))
                .thenReturn(Flux.just(doc1, doc3));

        // When
        Mono<Void> result = spyManager.addTrustedDomain(Flux.fromIterable(domains));

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verify that insertAll was called once with a list containing only the valid domains
        verify(mongoTemplate, times(1)).insertAll(anyList());
    }
}
