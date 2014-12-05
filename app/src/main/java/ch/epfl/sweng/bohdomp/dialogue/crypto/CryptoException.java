package ch.epfl.sweng.bohdomp.dialogue.crypto;

/**
 * Thrown when a high-level cryptographic function fails.
 */
public class CryptoException extends Exception {

    private static final long serialVersionUID = 1L;

    CryptoException() {
        super();
    }

    CryptoException(String message) {
        super(message);
    }

    CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    CryptoException(Throwable cause) {
        super(cause);
    }

}
