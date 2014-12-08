package ch.epfl.sweng.bohdomp.dialogue.crypto.hkp;

/**
 * Thrown when a key cannot be found.
 */
public class KeyNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    KeyNotFoundException() {
        super();
    }

    KeyNotFoundException(String message) {
        super(message);
    }

    KeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    KeyNotFoundException(Throwable cause) {
        super(cause);
    }

}
