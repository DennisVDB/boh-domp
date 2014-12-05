package ch.epfl.sweng.bohdomp.dialogue.crypto;

import android.test.AndroidTestCase;

import junit.framework.Assert;

/**
 * Class creating a Tester for a Dialogue Incoming Dispatcher
 */
public final class CryptoTest extends AndroidTestCase {

    private String mMessage;

    public CryptoTest() {
        mMessage = "test message";
    }

    public void testEncryption() throws Exception {
        //this should succeed, the specific cryptographic functionality is tested in PrimitiveTest
        Crypto.encrypt(getContext(), mMessage, TestKeyData.FINGERPRINT);
    }

    public void testUnknownFingerprintEncryption() throws Exception {
        try {
            Crypto.encrypt(getContext(), mMessage, "0000 0000 0000 0000 0000 0000 0000 0000 0000 0000");
            fail("Expected crypto exception");
        } catch (CryptoException ex) {
            //all is well
        }
    }

    public void testDecryption() throws Exception {
        String encrypted = Crypto.encrypt(getContext(), mMessage, TestKeyData.FINGERPRINT);
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
