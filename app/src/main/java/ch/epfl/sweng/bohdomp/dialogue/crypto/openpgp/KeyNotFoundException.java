package ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp;

/**
 * Thrown when a key cannot be found.
 */
public class KeyNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public KeyNotFoundException() {
        super();
    }

    public KeyNotFoundException(String message) {
        super(message);
    }

    public KeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyNotFoundException(Throwable cause) {
        super(cause);
    }

}
