package tech.mayanksoni.safebrowsing.exceptions;

public class DuplicateRecordException extends RuntimeException {
    public String resourceName;
    public String recordId;

    public DuplicateRecordException(String resourceName, String recordId) {
        this.resourceName = resourceName;
        this.recordId = recordId;
    }

    public DuplicateRecordException(String message, String resourceName, String recordId) {
        super(message);
        this.resourceName = resourceName;
        this.recordId = recordId;
    }

    public DuplicateRecordException(String message, Throwable cause, String resourceName, String recordId) {
        super(message, cause);
        this.resourceName = resourceName;
        this.recordId = recordId;
    }

    public DuplicateRecordException(Throwable cause, String resourceName, String recordId) {
        super(cause);
        this.resourceName = resourceName;
        this.recordId = recordId;
    }

    public DuplicateRecordException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String resourceName, String recordId) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.resourceName = resourceName;
        this.recordId = recordId;
    }
}
