package tech.mayanksoni.threatdetectionbackend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.mayanksoni.threatdetectionbackend.models.TrancoDomainEntry;
import tech.mayanksoni.threatdetectionbackend.models.TrancoListMetadata;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrancoListProviderServiceTest {

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpecMock;

    @Mock
    private WebClient.ResponseSpec responseSpecMock;

    private TrancoListProviderService trancoListProviderService;

    @BeforeEach
    public void setup() {
        // Create a WebClient mock
        WebClient webClientMock = Mockito.mock(WebClient.class);

        // Setup the WebClient mock chain
        when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(anyString())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.accept(any())).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);

        // Initialize the service with the mock
        trancoListProviderService = new TrancoListProviderService(webClientMock);
    }

    @Test
    public void testGetTrancoDomainsAsFlux_NoHeader() {
        // We're directly testing getTrancoDomainsAsFlux(listId), so no need to mock metadata

        // Create a sample CSV content without header
        String csvContent = "1,google.com\n2,youtube.com\n3,facebook.com";
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(csvContent.getBytes(StandardCharsets.UTF_8));

        // Mock the CSV response
        when(responseSpecMock.bodyToFlux(DataBuffer.class)).thenReturn(Flux.just(dataBuffer));

        // Test the method
        Flux<TrancoDomainEntry> result = trancoListProviderService.getTrancoDomainsAsFlux("test-list");

        // Verify the result
        StepVerifier.create(result)
                .expectNextMatches(entry -> entry.rank() == 1 && entry.domainName().equals("google.com"))
                .expectNextMatches(entry -> entry.rank() == 2 && entry.domainName().equals("youtube.com"))
                .expectNextMatches(entry -> entry.rank() == 3 && entry.domainName().equals("facebook.com"))
                .verifyComplete();
    }
}
