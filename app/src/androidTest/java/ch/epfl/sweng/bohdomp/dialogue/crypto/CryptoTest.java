package ch.epfl.sweng.bohdomp.dialogue.crypto;

import android.os.Bundle;
import android.os.ResultReceiver;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Class creating a Tester for a Dialogue Incoming Dispatcher
 */
public final class CryptoTest extends AndroidTestCase {

    private String mMessage;    }

    public CryptoTest() {
        mMessage = "test message";
    }

    public void testEncryption() throws Exception {
        //this should succeed, the specific cryptographic functionality is tested in PrimitiveTest
        Crypto.encrypt(getContext(), TestKeyData.FINGERPRINT, mMessage);
    }

    public void testUnknownFingerprintEncryption() throws Exception {
        try {
            Crypto.encrypt(getContext(), "0000 0000 0000 0000 0000 0000 0000 0000 0000 0000", mMessage);
            fail("Expected crypto exception");
        } catch (CryptoException ex){
            //all is well
        }
    }

    public void testDecryption() throws Exception {
        String encrypted = Crypto.encrypt(getContext(), TestKeyData.FINGERPRINT, mMessage);
        String decrypted = Crypto.decrypt(getContext(), encrypted);
        Assert.assertEquals(mMessage, decrypted);
    }

    public void testInvalidDecryption() throws Exception {
        try {
            Crypto.decrypt(getContext(), "this is not encrypted");
            fail("Crypto exception expected");
        } catch (CryptoException ex) {
            //ok
        }
    }

}
