package ch.epfl.sweng.bohdomp.dialogue.crypto;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.io.IOException;

import ch.epfl.sweng.bohdomp.dialogue.crypto.hkp.Client;
import ch.epfl.sweng.bohdomp.dialogue.crypto.hkp.KeyNotFoundException;

/**
 * Tests hkp key exchange functionality
 */
public class HkpTest extends AndroidTestCase {

    private final Client mClient = new Client("keyserver.ubuntu.com");

    public void testInvalidHost() throws Exception {
        Client invalid = new Client("www.invalid.example");
        try {
            invalid.lookupKeyChain(TestKeyData.FINGERPRINT);
            Assert.fail("IOException expected");
        } catch (IOException ex) {
           //ok
        }
    }

    public void testKeyNotFound() throws Exception {
        try {
            mClient.lookupKeyChain("0000 0000 0000 0000 0000 0000 0000 0000 0000 0000");
            Assert.fail("KeyNotFoundException expected");
        } catch (KeyNotFoundException ex) {
            //ok
        }
    }

    public void testLookupKey() throws Exception {
        mClient.lookupKeyChain(TestKeyData.FINGERPRINT);
    }
    
}