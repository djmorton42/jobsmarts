package ca.quadrilateral.jobsmarts.api.exception;

public class JobPageNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public JobPageNotFoundException() {
        super();
    }

    public JobPageNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JobPageNotFoundException(final String message) {
        super(message);
    }

    public JobPageNotFoundException(final Throwable cause) {
        super(cause);
    }

}
