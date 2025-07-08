package tech.mayanksoni.safebrowsing.services;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import tech.mayanksoni.safebrowsing.clients.TrancoHttpClient;
import tech.mayanksoni.safebrowsing.configuration.SafeBrowsignConfig;
import tech.mayanksoni.safebrowsing.exceptions.MalformedCSVRecord;
import tech.mayanksoni.safebrowsing.models.TrancoCSVFileRecord;
import tech.mayanksoni.safebrowsing.models.TrancoDailyFileMetadata;
import tech.mayanksoni.safebrowsing.models.TrancoFileEntity;
import tech.mayanksoni.safebrowsing.repository.TrancoListRepository;
import tech.mayanksoni.safebrowsing.repository.TrancoProvidedDomainRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
public class TrustedDomainDataService {
    private static final DateTimeFormatter TRANCO_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final String TRANCO_FILE_NAME_FORMAT = "tranco-full-%s.csv";
    private final MinioService minioService;
    private final TrancoHttpClient trancoHttpClient;
    private final SafeBrowsignConfig safeBrowsignConfig;
    private final TrancoProvidedDomainRepository trancoProvidedDomainRepository;
    private final TrancoListRepository trancoListRepository;
    private final RetryRegistry retryRegistry;
    private Retry retry;

    @PostConstruct
    private void postConstructTasks() {
        log.debug("Running post construct tasks for : {}", this.getClass().getSimpleName());
        retry = retryRegistry.retry("dateShiftRetry");
        TrancoDailyFileMetadata latestDailyFileMetadata = executeServerCallWithRetry(LocalDate.now());
        if (trancoListRepository.getFileInformation(latestDailyFileMetadata.getListId()).isEmpty()) {
            downloadTrancoListFromServer(latestDailyFileMetadata);
            downloadFromObjectStoreAndUploadToDB();
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void midnightMaintenanceTasks() throws IOException {
        log.info("Executing midnight maintenance tasks...");
        downloadTrancoListFromServer();
    }

    @Scheduled(cron = "0 0 3 * * *")
    private void daily3AMTasks() {
        log.info("Executing daily 3AM tasks...");
        downloadFromObjectStoreAndUploadToDB();
    }

    private void downloadFromObjectStoreAndUploadToDB() {
        log.info("Downloading Tranco list from object store...");
        Optional<TrancoFileEntity> trancoFileEntityOptional = trancoListRepository.getLatestUnprocessedFile();
        String listIdToPurge = trancoFileEntityOptional.map(TrancoFileEntity::listId).orElse(null);
        if (listIdToPurge != null) {
            this.trancoProvidedDomainRepository.purgeDomainsByListId(listIdToPurge);
        }
        trancoFileEntityOptional.ifPresentOrElse(trancoFileEntity -> {
            if (!trancoFileEntity.processed()) {
                try (BufferedReader trancoFileBufferedReader = minioService.downloadFile(String.format(TRANCO_FILE_NAME_FORMAT, trancoFileEntity.listId()))) {
                    List<TrancoCSVFileRecord> fileRecordsBatch = new ArrayList<>(safeBrowsignConfig.getDataLoadBatch());
                    String line;
                    int count = 0;
                    int batchCount = 0;
                    while ((line = trancoFileBufferedReader.readLine()) != null) {
                        count++;
                        String[] lineParts = line.split(",");
                        if (lineParts.length != 2) {
                            throw new MalformedCSVRecord(String.format("Malformed CSV record at line %d, line: %s", count, line));
                        }
                        long domainRank = Long.parseLong(lineParts[0]);
                        String domain = lineParts[1];
                        log.trace("Extracted from TrancoCSV domain {} with rank {}", domain, domainRank);
                        fileRecordsBatch.add(new TrancoCSVFileRecord(domainRank, domain));
                        if (fileRecordsBatch.size() >= safeBrowsignConfig.getDataLoadBatch()) {
                            batchCount++;
                            log.debug("Inserting {} batch into DB", count);
                            this.trancoProvidedDomainRepository.insertDomainsFromCSVRecords(trancoFileEntity.listId(), fileRecordsBatch);
                            fileRecordsBatch.clear();
                        }
                    }
                    if (!fileRecordsBatch.isEmpty()) {
                        batchCount++;
                        log.debug("Inserting {} batch into DB", count);
                        this.trancoProvidedDomainRepository.insertDomainsFromCSVRecords(trancoFileEntity.listId(), fileRecordsBatch);
                    }
                    log.debug("Completed inserting {} batches into DB, total Records processed: {}", batchCount, count);
                    this.trancoListRepository.updateTrancoFile(trancoFileEntity.listId(), true, count);
                    this.trancoListRepository.updateActiveTrancoList(trancoFileEntity.listId());
                } catch (MalformedCSVRecord e) {
                    log.error("Malformed CSV Record Exception thrown for : {}", e.getMessage());
                } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                         NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException |
                         XmlParserException | InternalException e) {
                    throw new RuntimeException(e);
                }
            }
        }, () -> log.error("Unable to find tranco file to process"));
    }

    private void downloadTrancoListFromServer(TrancoDailyFileMetadata dailyFileMetadata) {
        log.debug("Downloaded Tranco list metadata: {}", dailyFileMetadata);
        try {
            if (!minioService.isFilePresent(String.format(TRANCO_FILE_NAME_FORMAT, dailyFileMetadata.getListId()))) {
                InputStream fis = executeServerCallWithRetry(dailyFileMetadata.getListId()).getInputStream();
                minioService.uploadFile(String.format(TRANCO_FILE_NAME_FORMAT, dailyFileMetadata.getListId()), fis);
                this.trancoListRepository.createTrancoFile(dailyFileMetadata.getListId(), dailyFileMetadata.getListId(), Instant.now(), fis.available())
                        .ifPresentOrElse(trancoFileEntity -> {
                            log.debug("Saved information about tranco file downloaded with list id : {}, database Identifier: {}", trancoFileEntity.listId(), trancoFileEntity.databaseRecordId());
                        }, () -> log.error("Unable to save information about downloaded tranco file"));
            } else {
                log.debug("File is already uploaded to Object Store, Skipping upload to Object Store for List ID: {}", dailyFileMetadata.getListId());
            }
        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            log.error("Error uploading tranco file to minio, message: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void downloadTrancoListFromServer() throws IOException {
        TrancoDailyFileMetadata latestDailyFileMetadata = executeServerCallWithRetry(LocalDate.now());
        downloadTrancoListFromServer(latestDailyFileMetadata);
    }

    private Resource executeServerCallWithRetry(String listId) {
        Supplier<Resource> retryableSupplier = Retry.decorateSupplier(retry, () -> {
            log.info("Executing server call for listId {}", listId);
            try {
                return downloadTrancoFullListFromServerUsingListId(listId);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().is4xxClientError()) {
                    log.error("Error downloading tranco file for listId {}", listId);
                    throw e;
                } else {
                    throw e;
                }
            }
        });
        try {
            return retryableSupplier.get();
        } catch (Exception e) {
            log.error("Final Fallback: Error downloading tranco file for listId {}", listId);
            throw e;
        }
    }

    private TrancoDailyFileMetadata executeServerCallWithRetry(LocalDate initialDate) {
        LocalDate[] currentDate = {initialDate};
        Supplier<TrancoDailyFileMetadata> retryableSupplier = Retry.decorateSupplier(retry, () -> {
            log.info("Executing server call for date {}", currentDate[0]);
            try {
                return downloadTrancoListDailyMetadataFromServer(currentDate[0]);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().equals(HttpStatus.NOT_FOUND) || e.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                    log.error("Error downloading tranco file for date {}", currentDate[0], e);
                    currentDate[0] = currentDate[0].minusDays(1);
                    throw e;
                } else {
                    throw e;
                }
            }
        });
        try {
            return retryableSupplier.get();
        } catch (Exception e) {
            log.error("Final Fallback: Error downloading tranco file for date {}", initialDate, e);
            throw e;
        }
    }

    private Resource downloadTrancoFullListFromServerUsingListId(String listId) {
        log.debug("Downloading Tranco full list for listId {}", listId);
        return trancoHttpClient.downloadTrancoFullListById(listId);
    }

    private TrancoDailyFileMetadata downloadTrancoListDailyMetadataFromServer(LocalDate date) {
        log.debug("Downloading Tranco list metadata for date {}", date);
        return trancoHttpClient.getTrancoFileMetadataByDate(date.format(TRANCO_DATE_FORMATTER));
    }

}
