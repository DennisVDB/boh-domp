package ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp;

import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Thrown when a required password was incorrect.
 */
public class IncorrectPassphraseException extends Exception {
    private static final long serialVersionUID = 1L;

    public IncorrectPassphraseException() {
        super();
    }

    public IncorrectPassphraseException(String message) {
        super(Contract.throwIfArgNull(message, "message"));
    }

    public IncorrectPassphraseException(Throwable throwable) {
        super(Contract.throwIfArgNull(throwable, "throwable"));
    }

    public IncorrectPassphraseException(String message, Throwable throwable) {
        super(Contract.throwIfArgNull(message, "message"), Contract.throwIfArgNull(throwable, "throwable"));
    }
}