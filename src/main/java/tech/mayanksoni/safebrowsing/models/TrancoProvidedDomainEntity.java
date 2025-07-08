package tech.mayanksoni.safebrowsing.models;

public record TrancoProvidedDomainEntity(
        String databaseIdentifier,
        long domainRank,
        String domain,
        String listId,
        char domainFirstCharacter,
        int domainLength
) {
}
