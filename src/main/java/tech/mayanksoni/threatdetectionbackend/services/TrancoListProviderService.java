package tech.mayanksoni.threatdetectionbackend.services;

import com.google.common.net.InternetDomainName;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.mayanksoni.threatdetectionbackend.models.TrancoDomainEntry;
import tech.mayanksoni.threatdetectionbackend.models.TrancoListMetadata;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class TrancoListProviderService {
    private final WebClient trancoListProviderWebClient;

    public TrancoListProviderService() {
        this(WebClient.create("https://tranco-list.eu"));
    }

    // Constructor for testing
    public TrancoListProviderService(WebClient webClient) {
        this.trancoListProviderWebClient = webClient;
    }
    private static final String TRANCO_LIST_METADATA_ENDPOINT = "/api/lists/date/latest";
    private static final String BASE_URL = "https://tranco-list.eu";

    public Mono<TrancoListMetadata> getTrancoListLatestMetadata(){
        String fullUrl = BASE_URL + TRANCO_LIST_METADATA_ENDPOINT;
        log.info("Retrieving latest Tranco list metadata from URL: {}", fullUrl);
        return trancoListProviderWebClient.get()
                .uri(TRANCO_LIST_METADATA_ENDPOINT)
                .retrieve()
                .bodyToMono(TrancoListMetadata.class)
                .doOnError(e -> log.error("Error retrieving latest Tranco list metadata from URL {}: {}", fullUrl, e.getMessage()));
    }
    public Flux<TrancoDomainEntry> getTrancoDomainsAsFlux(){
        return getTrancoListLatestMetadata()
                .flatMapMany(metadata -> getTrancoDomainsAsFlux(metadata.getListId()));
    }
    public Flux<TrancoDomainEntry> getTrancoDomainsAsFlux(String listId){
        String uri = "/download/"+ listId + "/full";
        String fullUrl = BASE_URL + uri;
        log.info("Retrieving Tranco domains from URL: {}", fullUrl);
        return trancoListProviderWebClient.get()
                .uri(uri)
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .flatMap(buffer -> {
                    try{
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(buffer.asInputStream(true), StandardCharsets.UTF_8));
                        // Create a Flux that emits items from the CSV reader
                        return Flux.create(sink -> {
                            try {
                                String line;
                                int count = 0;
                                while ((line = reader.readLine()) != null && !sink.isCancelled()) {
                                    // Parse the CSV line manually
                                    String[] parts = line.split(",", 2);
                                    if (parts.length == 2) {
                                        try {
                                            long rank = Long.parseLong(parts[0].trim());
                                            String domainName = parts[1].trim();
                                            sink.next(new TrancoDomainEntry(rank, domainName));
                                            count++;
                                            if (count % 10000 == 0) {
                                                log.debug("Processed {} Tranco domains from list: {}", count, listId);
                                            }
                                        } catch (NumberFormatException e) {
                                            log.warn("Skipping invalid line in CSV: {}", line);
                                        }
                                    } else {
                                        log.warn("Skipping malformed line in CSV: {}", line);
                                    }
                                }
                                log.info("Completed processing Tranco domains from URL: {}", fullUrl);
                                sink.complete();
                            } catch (Exception e) {
                                log.error("Error processing Tranco domains from URL {}: {}", fullUrl, e.getMessage());
                                sink.error(e);
                            }
                        });
                    }catch (Exception e){
                        log.error("Error retrieving Tranco domains from URL {}: {}", fullUrl, e.getMessage());
                        return Flux.error(e);
                    }
                });
    }
}
