package tech.mayanksoni.safebrowsing.repository.mongo;

import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import tech.mayanksoni.safebrowsing.documents.TrancoFile;
import tech.mayanksoni.safebrowsing.exceptions.DuplicateRecordException;
import tech.mayanksoni.safebrowsing.exceptions.ListEntryNotFound;
import tech.mayanksoni.safebrowsing.mapper.TrancoFileInformationMapper;
import tech.mayanksoni.safebrowsing.models.TrancoFileEntity;
import tech.mayanksoni.safebrowsing.repository.TrancoListRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TrancoListRepositoryMongoImpl implements TrancoListRepository {
    private final MongoTemplate mongoTemplate;
    private final TrancoFileInformationMapper TRANCO_FILE_MAPPER;

    private TrancoFile getTrancoFileDocumentByListId(String listId) {
        Query mongoSelectionQuery = Query.query(Criteria.where("listId").is(listId));
        return this.mongoTemplate.findOne(mongoSelectionQuery, TrancoFile.class);
    }

    @Override
    public Optional<TrancoFileEntity> getFileInformation(String listId) {
        return Optional.ofNullable(getTrancoFileDocumentByListId(listId)).map(TRANCO_FILE_MAPPER::toTrancoFileEntity);
    }

    @Override
    public Optional<TrancoFileEntity> getLatestUnprocessedFile() {
        return fetchUnprocessedTrancoFiles().stream().findFirst().map(TRANCO_FILE_MAPPER::toTrancoFileEntity);
    }


    @Override
    public List<TrancoFileEntity> getProcessedFiles() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<TrancoFileEntity> getLatestUnprocessedFiles() {
        return fetchUnprocessedTrancoFiles().stream().map(TRANCO_FILE_MAPPER::toTrancoFileEntity).toList();
    }

    @Override
    public void updateActiveTrancoList(String updatedActiveTrancoList) {
        Optional<TrancoFile> trancoListToBeUpdatedAsActive = Optional.ofNullable(getTrancoFileDocumentByListId(updatedActiveTrancoList));
        trancoListToBeUpdatedAsActive.ifPresentOrElse(trancoFile -> {
            deactivateAllTrancoLists();
            Update mongoUpdateSpec = Update.update("active", true);
            Query mongoSelectionQuery = Query.query(Criteria.where("listId").is(updatedActiveTrancoList));
            UpdateResult mongoUpdateResult = this.mongoTemplate.updateFirst(mongoSelectionQuery, mongoUpdateSpec, TrancoFile.class);
            log.debug("Updated active tranco list to {}, Update Count: {}", updatedActiveTrancoList, mongoUpdateResult.getModifiedCount());
        }, () -> {
            throw new ListEntryNotFound("The list could not be located in DB, listId: " + updatedActiveTrancoList + " or the list is not yet processed");
        });

    }

    private void deactivateAllTrancoLists() {
        Update mongoUpdateSpec = Update.update("active", false);
        UpdateResult mongoUpdateResult = this.mongoTemplate.updateMulti(new Query(), mongoUpdateSpec, TrancoFile.class);
        log.debug("Deactivated all tranco lists, Update Count: {}", mongoUpdateResult.getModifiedCount());
    }

    @Override
    public Optional<TrancoFileEntity> createTrancoFile(String fileName, String listId, Instant downloadedOn, long contentSize) {
        try {
            TrancoFile trancoFileToCreate = TrancoFile.builder()
                    .sizeInBytes(contentSize)
                    .downloadedOn(downloadedOn)
                    .listId(listId)
                    .id(UUID.randomUUID().toString())
                    .build();
            if (getCountOfFileRecordsForListId(listId) > 0) {
                throw new DuplicateRecordException("trancoFileEntity", listId);
            }
            return Optional.of(this.mongoTemplate.save(trancoFileToCreate)).map(TRANCO_FILE_MAPPER::toTrancoFileEntity);
        } catch (DuplicateRecordException e) {
            log.error("Duplicate record found for listId {}", e.recordId);
            return getFileInformation(listId);
        }
    }

    @Override
    public Optional<TrancoFileEntity> updateTrancoFile(String listId, boolean isProcessed, long recordCount) {
        Update mongoUpdateQuery = Update.update("processed", isProcessed).set("recordCount", recordCount);
        Query mongoSelectionQuery = Query.query(Criteria.where("listId").is(listId));
        UpdateResult mongoUpdateResult = this.mongoTemplate.updateMulti(mongoSelectionQuery, mongoUpdateQuery, TrancoFile.class);
        log.debug("Updated {} records for listId {}", mongoUpdateResult.getModifiedCount(), listId);
        return getFileInformation(listId);
    }

    private List<TrancoFile> fetchUnprocessedTrancoFiles() {
        Query mongoSelectionQuery = Query.query(Criteria.where("processed").is(false));
        return this.mongoTemplate.find(mongoSelectionQuery, TrancoFile.class).stream()
                .sorted((o1, o2) -> o2.getDownloadedOn().compareTo(o1.getDownloadedOn())).toList();
    }

    private long getCountOfFileRecordsForListId(String listId) {
        Query selectionForList = Query.query(Criteria.where("listId").is(listId));
        return this.mongoTemplate.count(selectionForList, TrancoFile.class);
    }
}
