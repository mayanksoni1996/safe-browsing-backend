package tech.mayanksoni.safebrowsing.exceptions;

public class MalformedCSVRecord extends RuntimeException {
    public MalformedCSVRecord(String message) {
        super(message);
    }
}
