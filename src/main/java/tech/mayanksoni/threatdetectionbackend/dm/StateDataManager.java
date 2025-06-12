package tech.mayanksoni.threatdetectionbackend.dm;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.mayanksoni.threatdetectionbackend.models.StateModel;

public interface StateDataManager {
    Mono<StateModel> createState(String state, String domainName, String ipAddress, boolean accessOverrideControlAvailable);
    Flux<StateModel> getActiveStateByIpAddress(String ipAddress);
    Mono<StateModel> getStateById(String stateId);
    Mono<StateModel> updateStateById(String stateId, boolean accessAllowed);
    Flux<StateModel> getAllStates();
    Flux<StateModel> getAllActiveStates();
    void deleteStateById(String stateId);
    void deleteAllStates();
    Mono<Void> deleteAllInactiveStates();
    void deleteAllActiveStates();
}
