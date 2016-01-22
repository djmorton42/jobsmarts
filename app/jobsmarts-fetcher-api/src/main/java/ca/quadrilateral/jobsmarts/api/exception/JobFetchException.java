package ca.quadrilateral.jobsmarts.api.exception;

public class JobFetchException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public JobFetchException() {
        super();
    }

    public JobFetchException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JobFetchException(final String message) {
        super(message);
    }

    public JobFetchException(final Throwable cause) {
        super(cause);
    }

    
}
