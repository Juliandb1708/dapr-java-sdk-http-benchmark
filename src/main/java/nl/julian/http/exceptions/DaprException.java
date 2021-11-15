package nl.julian.http.exceptions;

public class DaprException extends RuntimeException {

    public DaprException(Throwable exception) {
        this("UNKNOWN", exception.getMessage(), exception);
    }

    public DaprException(int errorCode, String message) {
        this(Integer.toString(errorCode), message);
    }

    public DaprException(String errorCode, String message) {
        super(String.format("%s: %s", errorCode, message));
    }

    public DaprException(String errorCode, String message, Throwable cause) {
        super(String.format("%s: %s", errorCode, emptyIfNull(message)), cause);
    }

    private static String emptyIfNull(String str) {
        return str == null ? "" : str;
    }
}
