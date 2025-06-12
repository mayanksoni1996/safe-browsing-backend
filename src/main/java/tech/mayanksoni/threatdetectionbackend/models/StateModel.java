package tech.mayanksoni.threatdetectionbackend.models;

import java.time.Instant;

public record StateModel(
        String state,
        String domainName,
        String ipAddress,
        boolean accessAllowed,
        boolean accessOverrideControlAvailable,
        Instant stateExpiresAt
) {
}
