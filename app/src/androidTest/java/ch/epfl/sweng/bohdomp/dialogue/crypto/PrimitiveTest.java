package ch.epfl.sweng.bohdomp.dialogue.crypto;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import org.bouncycastle.openpgp.PGPException;

import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.IncorrectPassphraseException;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.KeyNotFoundException;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.PublicKey;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.PublicKeyChain;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.PublicKeyChainBuilder;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.PublicKeyRing;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.SecretKeyChain;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.SecretKeyChainBuilder;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.SecretKeyRing;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.NullArgumentException;


/**
 * Test pgp primitives on predefined keys.
 */
public class PrimitiveTest extends AndroidTestCase {

    //plain text of message that is encrypted
    private final static String PLAIN = "hello john\n";

    //encrypted message to john, not signed
    private final static String ENCRYPTED = "-----BEGIN PGP MESSAGE-----\n"
            + "Version: GnuPG v1\n"
            + "\n"
            + "hQEMAyufJbwG13z1AQgAtFa5Jwn1JTXmMmgmfyBk/qQDTpuVhej/SlJm/whQXSFb\n"
            + "iMz0OV80gsVJ7e80zGAC9MR0oqFnVLa4RNnkGRRb5UAb+bqtH27BZi9Z/ZCrzRwG\n"
            + "+93GaB1UIO++NnaCcyRi7vQ1o7uzQaBxquYjaZeIevsB5ugGcKMwmZkjdXIDMznh\n"
            + "lyEglMalkAuiCCkOINVrbpcONKF29H7BklTV4DGBZUB5d7548Fq3H+uurZo68RB3\n"
            + "tVYP789NVWEsrybHvkbBfhZzYUUMaXuzH3McCMdWW3blZzrNIeOZE0UjHTYUmOBS\n"
            + "Ah5v5YoABfmjtaaI1HPnnD1hvic2oKMrNXsWz4u4w9JGAeIvsbYBVXPyiep+PH2i\n"
            + "xVXhelBL+eC+RgZs3UsC7qRpQA/rJlNiVik1gfmZRQbsIXUeWVOFv26eJfHNs2O7\n"
            + "aGsNvQyAEQ==\n"
            + "=yqGd\n"
            + "-----END PGP MESSAGE-----";


    //encrypted message to someone else (debian-cd@lists.debian.org)
    private final static String ENCRYPTED_TO_OTHER = "-----BEGIN PGP MESSAGE-----\n"
            + "Version: GnuPG v1\n"
            + "\n"
            + "hQIMA2QqWsMRzZgZAQ//bEpF6v01zonsg2mzbov1j9qXBHEpi4lXKjECngFrhfGJ\n"
            + "Sz0Vx6PiGvkA04lJovM60bfZzAGp75BKf6zt3c7CAortcBNmpW+HynpzJ5qGDTxW\n"
            + "TDKTUY3E4KP/YP2VSuKZUc+pjHyLGvP5CEhOixBR7j7x43hGZqgMDsX72G8Ct323\n"
            + "r1MTd7Eq9G1Tpvmczk7odpwazZBbPKmGEKzS5qNq2MGdb/b26q/ijKEGmEcwxcQX\n"
            + "+tyifkLikRnH6v5k7JeTIR3biFrrMKjK2pHdfk++MuesUUDE0H4SyiKzBZJ+5jeD\n"
            + "BZaxqM6AEDuTXUk1KdD1UY9x48w5iPT498YabaKqixrJ88dyI5q7AwN8Wk3rARiC\n"
            + "O9kayuMmog1JwT3hWdlHo8r0b6bpRWgILfcwml5Jn7bnfhoXTKDOdgOVu3n+Qto9\n"
            + "YNchXhovIfsLkxyehD6va0rU4c+cKgo29HMji6sZ1OIShBcuGpv5zL7049dt6syY\n"
            + "/cr0mD4KssUF6GlABKP7uIora+NQA7QCi9fUEjg9SWEGRII5uAM3rapWK+uPUQPN\n"
            + "T57meDZaeIcjVIbbfCRIYIBOTTRTpKB/b2s/Vk03ehRyaBT2eGqMZr8YXPgo/akT\n"
            + "QtcCV76fl18GIAdg4s9jQvImbMXGev5CsFtyD7ic5oMqpt+If9Zx0jEPJIUJeGTS\n"
            + "RgEkTKBeDmD4Ijz0z0vCVV+GzhJBeJQ+1Ts13YrG8ljt3sKQPEobRZ7E6hIjD8cD\n"
            + "pXajDm4hTW6R4YmwebHyVg/KfHHMNqM=\n"
            + "=Bnam\n"
            + "-----END PGP MESSAGE-----";

    private SecretKeyRing secretKeyRing;

    private PublicKeyRing publicKeyRing;

    @Override
    public void setUp() throws Exception {
        secretKeyRing = new SecretKeyChainBuilder().fromString(TestKeyData.SECRET_KEY_RING).getKeyRings().get(0);
        publicKeyRing = new PublicKeyChainBuilder().fromString(TestKeyData.PUBLIC_KEY_RING).getKeyRings().get(0);
    }

    public void testNullDecryption() throws Exception {
        try {
            secretKeyRing.decrypt(null, TestKeyData.PASSPHRASE);
            Assert.fail("Null argument expected");
        } catch (NullArgumentException ex) {
            //all is well
        }
    }

    public void testNullPassDecryption() throws Exception {
        try {
            secretKeyRing.decrypt(PrimitiveTest.ENCRYPTED, null);
            Assert.fail("Null argument expected");
        } catch (NullArgumentException ex) {
            //all is well
        }
    }

    public void testCorrectDecryption() throws Exception {
        String decrypted = secretKeyRing.decrypt(PrimitiveTest.ENCRYPTED, TestKeyData.PASSPHRASE);
        Assert.assertEquals(PrimitiveTest.PLAIN, decrypted);
    }

    public void testIncorrectPassDecryption() throws Exception {
        try {
            secretKeyRing.decrypt(PrimitiveTest.ENCRYPTED, "wrong passphrase");
            Assert.fail("Expecting IncorrectPassphraseException but none was thrown.");
        } catch (IncorrectPassphraseException ex) {
            //correct, this exception should have been thrown
        }
    }

    public void testBadMessageDecryption() throws Exception {
        try {
            secretKeyRing.decrypt("invalid pgp encrypted message", TestKeyData.PASSPHRASE);
            Assert.fail("Expecting PGPException but none was thrown.");
        } catch (PGPException ex) {
            Assert.assertTrue(ex.getMessage().contains("pgp object stream"));
        }
    }

    public void testBadKeyDecryption() throws Exception {
        try {
            secretKeyRing.decrypt(PrimitiveTest.ENCRYPTED_TO_OTHER, TestKeyData.PASSPHRASE);
            Assert.fail("Expecting PGPException but none was thrown.");
        } catch (KeyNotFoundException ex) {
            Assert.assertTrue(ex.getMessage().contains("secret key"));
        }
    }

    /**
     * Since OpenPGP uses session keys, comparing encryption to a known result is hard. Thus this
     * test only checks if encryption followed by decryption yields the same result.
     */
    public void testEncryption() throws Exception {
        PublicKey pub = publicKeyRing.getEncryptionKeys().get(0);
        String encrypted = pub.encrypt(PLAIN);
        String decrypted = secretKeyRing.decrypt(encrypted, TestKeyData.PASSPHRASE);
        Assert.assertEquals(PLAIN, decrypted);
    }

    public void testNullEncryption() throws Exception {
        try {
            publicKeyRing.getEncryptionKeys().get(0).encrypt(null);
            Assert.fail("Null argument expected");
        } catch (NullArgumentException ex) {
            //all is well
        }
    }

    /**
     * Extract the encoded key data from an ascii-armored keychain. This method effectively
     * strips the headers and version field, thus enabling comparison of keys produced by different
     * implementations of OpenPGP
     */
    private String extractKeyData(String chain) {
        String noHeader = chain.replaceAll("-----BEGIN PGP PUBLIC KEY BLOCK-----\\n", "");
        String noVersion = noHeader.replaceAll("Version: .*\\n", "");
        String noFooter = noVersion.replaceAll("=.*\\n-----END PGP PUBLIC KEY BLOCK-----\n", "");
        return noFooter;
    }

    public void testPublicChainAddition() throws Exception {
        PublicKeyChain empty = new PublicKeyChainBuilder().empty();
        Assert.assertEquals(extractKeyData(TestKeyData.PUBLIC_KEY_RING),
                extractKeyData(empty.add(publicKeyRing).toArmored()));

    }

    public void testSecretChainAddition() throws Exception {
        SecretKeyChain empty = new SecretKeyChainBuilder().empty();
        Assert.assertEquals(extractKeyData(TestKeyData.SECRET_KEY_RING),
                extractKeyData(empty.add(secretKeyRing).toArmored()));

    }
}