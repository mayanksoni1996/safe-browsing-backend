package tech.mayanksoni.threatdetectionbackend.dm.impl.mongo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.mayanksoni.threatdetectionbackend.configuration.ThreatDetectionConfig;
import tech.mayanksoni.threatdetectionbackend.dm.StateDataManager;
import tech.mayanksoni.threatdetectionbackend.documents.StateDocument;
import tech.mayanksoni.threatdetectionbackend.mappers.StateMapper;
import tech.mayanksoni.threatdetectionbackend.models.StateModel;
import tech.mayanksoni.threatdetectionbackend.utils.TimeProcessor;

import java.time.Instant;
@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
public class StateMongoDataManagerImpl implements StateDataManager {
    private final ReactiveMongoTemplate mongoTemplate;
    private final ThreatDetectionConfig threatDetectionConfig;
    private static final Query ACTIVE_STATES_QUERY = Query.query(Criteria.where("stateExpiresAt").gt(Instant.now()));
    private static final Query INACTIVE_STATES_QUERY = Query.query(Criteria.where("stateExpiresAt").lt(Instant.now()));
    private final StateMapper STATE_MAPPER;

    @Scheduled(cron = "0 */5 * * * *")
    @PostConstruct
    private void performCleanupTask() {
        // This method is a placeholder for any cleanup logic that might be needed every 15 minutes.
        // It can be implemented as needed.
        log.info("Performing cleanup task for state");
        this.deleteAllInactiveStates();
    }

    private static Instant processTime(String timeString){
        return TimeProcessor.processTime(timeString);
    }
    @Override
    public Mono<StateModel> createState(String state, String domainName, String ipAddress, boolean accessOverrideControlAvailable) {
        StateDocument stateDocument = StateDocument.builder()
                .id(state)
                .stateExpiresAt(TimeProcessor.processTime(threatDetectionConfig.getDefaultStateLifetime()))
                .creationTimestamp(Instant.now())
                .accessAllowed(false)
                .stateExpiresAt(processTime(threatDetectionConfig.getDefaultStateLifetime()))
                .accessOverrideControlAvailable(accessOverrideControlAvailable)
                .domainName(domainName)
                .ipAddress(ipAddress)
                .build();
        return mongoTemplate.save(stateDocument).map(STATE_MAPPER::toStateModel);
    }

    @Override
    public Flux<StateModel> getActiveStateByIpAddress(String ipAddress) {
        Query query = ACTIVE_STATES_QUERY.addCriteria(Criteria.where("ipAddress").is(ipAddress));
        return mongoTemplate.find(query, StateDocument.class).map(STATE_MAPPER::toStateModel);
    }

    @Override
    public Mono<StateModel> getStateById(String stateId) {
        Query query = ACTIVE_STATES_QUERY.addCriteria(Criteria.where("id").is(stateId));
        return mongoTemplate.findOne(query, StateDocument.class)
                .map(STATE_MAPPER::toStateModel)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("State not found with id: " + stateId)));
    }

    @Override
    public Mono<StateModel> updateStateById(String stateId, boolean accessAllowed) {
        Query query = ACTIVE_STATES_QUERY.addCriteria(Criteria.where("id").is(stateId).and("accessOverrideControlAvailable").is(false));
        Update update = new Update().set("accessAllowed", accessAllowed).set("stateExpiresAt",processTime(threatDetectionConfig.getDefaultStateLifetime()));
        return this.mongoTemplate.updateFirst(query, update, StateDocument.class).flatMap(s -> {
            if (s.getModifiedCount() == 0) {
                return Mono.error(new IllegalArgumentException("State not found with id: " + stateId));
            }
            return getStateById(stateId);
        });
    }

    @Override
    public Flux<StateModel> getAllStates() {
        return mongoTemplate.findAll(StateDocument.class).map(STATE_MAPPER::toStateModel);
    }

    @Override
    public Flux<StateModel> getAllActiveStates() {
        return mongoTemplate.find(ACTIVE_STATES_QUERY, StateDocument.class).map(STATE_MAPPER::toStateModel)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No active states found")));
    }

    @Override
    public void deleteStateById(String stateId) {
        Query deleteSelectionQuery = Query.query(Criteria.where("id").is(stateId));
       this.mongoTemplate.remove(deleteSelectionQuery, StateDocument.class)
                .doOnSuccess(result -> {
                    if (result.getDeletedCount() == 0) {
                        log.warn("No state found with id: {}", stateId);
                    } else {
                        log.info("State with id {} deleted successfully", stateId);
                    }
                }).subscribe();
    }

    @Override
    public void deleteAllStates() {
        this.mongoTemplate.dropCollection(StateDocument.class).doOnSuccess(success -> log.info("Successfully deleted all states")).subscribe();
    }

    @Override
    public void deleteAllInactiveStates() {
        this.mongoTemplate.remove(INACTIVE_STATES_QUERY, StateDocument.class).doOnSuccess(result -> log.info("Successfully deleted {} inactive states",result.getDeletedCount())).subscribe();
    }

    @Override
    public void deleteAllActiveStates() {
        this.mongoTemplate.remove(ACTIVE_STATES_QUERY, StateDocument.class).subscribe();
    }
}
