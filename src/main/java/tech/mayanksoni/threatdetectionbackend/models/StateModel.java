package tech.mayanksoni.threatdetectionbackend.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
@Schema
public record StateModel(
        @Schema(description = "The state id which is a unique identifier for the domain and the ip check", example = "UUID-1234-5678-90AB-CDEF12345678")
        String state,
        @Schema(description = "The domain name for which the state is created", example = "example.com")
        String domainName,
        @Schema(description = "The ip address for which the state is created", example = "127.0.0.1")
        String ipAddress,
        @Schema(description = "Is access already allowed for this state", example = "false")
        boolean accessAllowed,
        @Schema(description = "Is access override control available for this state", example = "true")
        boolean accessOverrideControlAvailable,
        @Schema(description = "The time at which the state expires", example = "2022-01-01T00:00:00Z")
        Instant stateExpiresAt
) {
}
