package ch.epfl.sweng.bohdomp.dialogue.crypto;

import android.content.Context;

import org.bouncycastle.openpgp.PGPException;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.PublicKey;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.PublicKeyRing;
import ch.epfl.sweng.bohdomp.dialogue.messaging.TextMessageBody;

/**
 * Created by dennis on 05/12/14.
 */
public class Crypto {
    public static TextMessageBody encrypt(Context context, String message, String fingerprint) {
        KeyManager keyManager = new KeyManager(context);

        PublicKeyRing keyRing = null;
        try {
            keyRing = keyManager.getPublicKeyChain().getKeyRing(fingerprint);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }

        if (keyRing == null) {
                throw new NoSuchElementException("No public key matching the fingerprint \"" + fingerprint + "\" can be found.");
            }

            List<PublicKey> encryptionKeys = keyRing.getEncryptionKeys();
            if (encryptionKeys.size() == 0) {
                throw new NoSuchElementException("No public keys of fingerprint \"" + fingerprint + "\" support encryption.");
            }

        String encryptedMessage = null;
        try {
            encryptedMessage = encryptionKeys.get(0).encrypt(message);
        } catch (PGPException e) {
            e.printStackTrace();
        }

        return new TextMessageBody(encryptedMessage);
    }
}
