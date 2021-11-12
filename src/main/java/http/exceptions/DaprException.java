package http.exceptions;

public class DaprException extends RuntimeException {
    public DaprException(String message) {
        super(message);
    }
}
