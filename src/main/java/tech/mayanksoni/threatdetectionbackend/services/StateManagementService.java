package tech.mayanksoni.threatdetectionbackend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tech.mayanksoni.threatdetectionbackend.dm.StateDataManager;
import tech.mayanksoni.threatdetectionbackend.models.StateModel;

@Service
@Slf4j
@RequiredArgsConstructor
public class StateManagementService {
    private final StateDataManager stateDataManager;

    /**
     * Creates a new state with the given parameters.
     *
     * @param state The state identifier.
     * @param domainName The domain name associated with the state.
     * @param ipAddress The IP address associated with the state.
     * @param accessOverrideControlAvailable Whether access override control is available for this state.
     * @return A Mono containing the created state model.
     */
    public Mono<StateModel> createStateModel(String state, String domainName, String ipAddress, boolean accessOverrideControlAvailable, boolean accessAllowed) {
        log.info("Creating state model with ID: {}, Domain: {}, IP: {}, Access Override Control Available: {}",
                state, domainName, ipAddress, accessOverrideControlAvailable);
        return stateDataManager.createState(state, domainName, ipAddress, accessOverrideControlAvailable, accessAllowed);
    }
    public Mono<StateModel> getStateById(String state){
        log.info("Retrieving state model with ID: {}", state);
        return stateDataManager.getStateById(state);
    }
    public Mono<StateModel> updateStateById(String state, boolean accessAllowed){
        log.info("Updating state model with ID: {}, Access Allowed: {}", state, accessAllowed);
        return stateDataManager.updateStateById(state, accessAllowed);
    }
}
