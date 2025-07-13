package tech.mayanksoni.safebrowsing.repository;

import tech.mayanksoni.safebrowsing.models.TrancoFileEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TrancoListRepository {
    Optional<TrancoFileEntity> getFileInformation(String listId);

    Optional<TrancoFileEntity> getLatestUnprocessedFile();

    List<TrancoFileEntity> getAllTrancoListsReadyToDelete();
    List<TrancoFileEntity> getProcessedFiles();

    List<TrancoFileEntity> getLatestUnprocessedFiles();

    void updateActiveTrancoList(String updatedActiveTrancoList);

    void markedListAsPurged(String listId);

    Optional<TrancoFileEntity> createTrancoFile(String fileName, String listId, Instant downloadedOn, long contentSize);

    Optional<TrancoFileEntity> updateTrancoFile(String fileId, boolean isProcessed, long recordCount);
}
