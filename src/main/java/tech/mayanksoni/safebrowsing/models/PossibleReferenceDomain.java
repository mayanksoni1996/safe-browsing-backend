package tech.mayanksoni.safebrowsing.models;

public record PossibleReferenceDomain(
        String registrySuffix,
        String ownerDomain,
        int ownerDomainLength,
        String soundexCode
) {
}
