package tech.mayanksoni.safebrowsing.models;

public record EditDistanceRecordForDomain(
        int editDistance,
        String domainToCheck,
        String referenceDomain
) {
}
