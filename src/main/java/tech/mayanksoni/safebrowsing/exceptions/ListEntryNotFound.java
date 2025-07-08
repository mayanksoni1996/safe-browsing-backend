package tech.mayanksoni.safebrowsing.exceptions;

public class ListEntryNotFound extends RuntimeException {
    public ListEntryNotFound(String message) {
        super(message);
    }
}
