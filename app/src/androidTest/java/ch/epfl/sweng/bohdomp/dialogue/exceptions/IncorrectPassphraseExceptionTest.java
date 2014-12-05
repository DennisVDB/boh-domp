package ch.epfl.sweng.bohdomp.dialogue.exceptions;

import android.test.AndroidTestCase;

import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.IncorrectPassphraseException;

/**
 * tests for FingerprintInsertionExceptions
 */
public class IncorrectPassphraseExceptionTest extends AndroidTestCase {

    public void testConstrNullString() {
        try {
            new IncorrectPassphraseException((String) null);
            fail("Exception should have been thrown");
        } catch (NullArgumentException e) {
            //Everything is ok
        }
    }

    public void testConstrNullThrowable() {
        try {
            new IncorrectPassphraseException((Throwable) null);
            fail("Exception should have been thrown");
        } catch (NullArgumentException e) {
            //Everything is ok
        }
    }

    public void testConstrNullStringValidThrowable() {
        try {
            new IncorrectPassphraseException(null, new Throwable());
            fail("Exception should have been thrown");
        } catch (NullArgumentException e) {
            //Everything is ok
        }
    }

    public void testConstrValidStringNullThrowable() {
        try {
            new IncorrectPassphraseException("message", null);
            fail("Exception should have been thrown");
        } catch (NullArgumentException e) {
            //Everything is ok
        }
    }

    public void testConstrNullStringNullThrowable() {
        try {
            new IncorrectPassphraseException(null, null);
            fail("Exception should have been thrown");
        } catch (NullArgumentException e) {
            //Everything is ok
        }
    }

    public void testValidConstr() {
        new IncorrectPassphraseException();
    }

    public void testValidConstrString() {
        new IncorrectPassphraseException("message");
    }


    public void testValidConstrThrowable() {
        new IncorrectPassphraseException(new Throwable());
    }


    public void testValidConstrStringThrowable() {
        new IncorrectPassphraseException("message", new Throwable());
    }
}
