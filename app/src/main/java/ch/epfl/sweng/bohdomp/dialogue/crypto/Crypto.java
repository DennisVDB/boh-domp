package ch.epfl.sweng.bohdomp.dialogue.crypto;

import android.content.Context;

import org.bouncycastle.openpgp.PGPException;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import ch.epfl.sweng.bohdomp.dialogue.crypto.hkp.HkpServerException;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.IncorrectPassphraseException;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.KeyNotFoundException;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.PublicKey;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.PublicKeyRing;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Entry point for using cryptographic functions.
 */
public class Crypto {

    private static PublicKey getEncryptionKey(Context context, String fingerprint) throws IOException, PGPException,
        NoSuchElementException, HkpServerException, KeyNotFoundException {

        KeyManager manager = new KeyManager(context);
        PublicKeyRing ring = manager.getPublicKeyRing(fingerprint);

        List<PublicKey> encryptionKeys = ring.getEncryptionKeys();
        if (encryptionKeys.size() == 0) {
            throw new NoSuchElementException("No public keys of fingerprint \"" + fingerprint
                    + "\" support encryption.");
        }
        return encryptionKeys.get(0); //the first (default) encryption key is used
    }

    private static String decryptUnsafe(Context context, String message) throws IOException, PGPException,
            IncorrectPassphraseException, KeyNotFoundException {

        KeyManager manager = new KeyManager(context);
        String passphrase = KeyManager.PASSPHRASE; //TODO retrieve passphrase from account management
        return manager.getSecretKeyChain().decrypt(message, passphrase);
    }

    public static String encrypt(Context context, String message, String fingerprint) throws CryptoException {
        Contract.throwIfArgNull(context, "context");
        Contract.throwIfArgNull(message, "message");
        Contract.throwIfArgNull(fingerprint, "fingerprint");

        try {
            PublicKey key = Crypto.getEncryptionKey(context, fingerprint);
            return key.encrypt(message);
        } catch (IOException ex) {
            throw new CryptoException("Cannot encrypt message", ex);
        } catch (PGPException ex) {
            throw new CryptoException("Cannot encrypt message", ex);
        } catch (NoSuchElementException ex) {
            throw new CryptoException("Cannot encrypt message", ex);
        } catch (HkpServerException ex) {
            throw new CryptoException("Cannot encrypt message", ex);
        } catch (KeyNotFoundException ex) {
            throw new CryptoException("Cannot encrypt message, key not found", ex);
        }
    }

    public static boolean isEncrypted(String message) {
        Contract.throwIfArgNull(message, "message");

        return message.startsWith("-----BEGIN PGP MESSAGE-----\n") && message.endsWith("-----END PGP MESSAGE-----");
    }

    public static String decrypt(Context context, String message) throws CryptoException {
        Contract.throwIfArgNull(context, "context");
        Contract.throwIfArgNull(message, "mesage");

        try {
            return decryptUnsafe(context, message);
        } catch (IOException ex) {
            throw new CryptoException("Cannot decrypt message", ex);
        } catch (PGPException ex) {
            throw new CryptoException("Cannot decrypt message", ex);
        } catch (NoSuchElementException ex) {
            throw new CryptoException("Cannot decrypt message", ex);
        } catch (IncorrectPassphraseException ex) {
            throw new CryptoException("Cannot decrypt message, wrong password", ex);
        } catch (KeyNotFoundException ex) {
            throw new CryptoException("Cannot decrypt message, it was not encrypted to you", ex);
        }
    }

}
