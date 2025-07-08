package tech.mayanksoni.safebrowsing.models;

public record TrancoFileEntity(
        String databaseRecordId,
        String listId,
        String downloadedOn,
        long sizeInBytes,
        boolean processed,
        boolean purged,
        long recordCount
) {
}
