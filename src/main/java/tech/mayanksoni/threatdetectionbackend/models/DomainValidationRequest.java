package tech.mayanksoni.threatdetectionbackend.models;

public record DomainValidationRequest(
        String domainName,
        String stateId,
        String ipAddress
) {
}
