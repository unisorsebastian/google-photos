package ro.jmind.photos.model;

public class ApplicationBusinessException extends Exception {
    public ApplicationBusinessException(String message) {
        super(message);
    }

    public ApplicationBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationBusinessException(Throwable cause) {
        super(cause);
    }
}
