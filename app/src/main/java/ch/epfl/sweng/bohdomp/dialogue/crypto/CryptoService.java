package ch.epfl.sweng.bohdomp.dialogue.crypto;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.util.encoders.DecoderException;

import java.io.IOException;
import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.IncorrectPassphraseException;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.PublicKey;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.PublicKeyRing;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.SecretKeyRing;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * An {@link IntentService} exposing the OpenPGP crypto API.
 */
public class CryptoService extends IntentService {

    private static final String ACTION_ENCRYPT =
            "ch.epfl.sweng.bohdomp.dialogue.crypto.action.ENCRYPT";
    private static final String ACTION_DECRYPT =
            "ch.epfl.sweng.bohdomp.dialogue.crypto.action.DECRYPT";
    private static final String ACTION_ENCRYPTION_STATUS =
            "ch.epfl.sweng.bohdomp.dialogue.crypto.action.ENCRYPTION_STATUS";

    public static final String EXTRA_FINGERPRINT =
            "ch.epfl.sweng.bohdomp.dialogue.crypto.extra.RECEIVER_FINGERPRINT";
    public static final String EXTRA_CLEAR_TEXT =
            "ch.epfl.sweng.bohdomp.dialogue.crypto.extra.EXTRA_CLEAR_TEXT";
    public static final String EXTRA_ENCRYPTED_TEXT =
            "ch.epfl.sweng.bohdomp.dialogue.crypto.extra.EXTRA_ENCRYPTED_TEXT";
    public static final String EXTRA_FAILURE_MESSAGE =
            "ch.epfl.sweng.bohdomp.dialogue.crypto.extra.FAILURE_MESSAGE";
    public static final String EXTRA_RECEIVER =
            "ch.epfl.sweng.bohdomp.dialogue.crypto.extra.RECEIVER";

    public static final int RESULT_SUCCESS = 0;

    public static final int RESULT_FAILURE = 1;

    private KeyManager mKeyManager;

    public CryptoService() {
        super("CryptoService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        Contract.throwIfArgNull(context, "Cannot create a CryptoService with an empty context.");
        mKeyManager = new KeyManager(context);
    }

    /**
     * Encrypt a message with the given fingerprint's public key.
     *
     * @param context     android context
     * @param fingerprint public key's fingerprint
     * @param message     the message to encrypt
     * @param receiver    a receiver that will be notified once the message is encrypted
     */
    public static void startActionEncrypt(Context context, String fingerprint, String message,
                                          ResultReceiver receiver) {
        Contract.throwIfArgNull(context, "context");
        Contract.throwIfArgNull(fingerprint, "fingerprint");
        Contract.throwIfArgNull(message, "message");
        Contract.throwIfArgNull(receiver, "receiver");

        Intent intent = new Intent(context, CryptoService.class);
        intent.setAction(ACTION_ENCRYPT);
        intent.putExtra(EXTRA_FINGERPRINT, fingerprint);
        intent.putExtra(EXTRA_CLEAR_TEXT, message);
        intent.putExtra(EXTRA_RECEIVER, receiver);
        context.startService(intent);
    }

    /**
     * Decrypt a message.
     *
     * @param context  android context
     * @param message  the message to decrypt
     * @param receiver a receiver that will be notified once the message is decrypted
     */
    public static void startActionDecrypt(Context context, String message, ResultReceiver receiver) {
        Contract.throwIfArgNull(context, "context");
        Contract.throwIfArgNull(message, "message");
        Contract.throwIfArgNull(receiver, "receiver");

        Intent intent = new Intent(context, CryptoService.class);
        intent.setAction(ACTION_DECRYPT);
        intent.putExtra(EXTRA_ENCRYPTED_TEXT, message);
        intent.putExtra(EXTRA_RECEIVER, receiver);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Contract.throwIfArgNull(intent, "intent");

        String action = intent.getAction();
        ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);

        if (action.equals(ACTION_ENCRYPT)) {
            String fingerprint = intent.getStringExtra(EXTRA_FINGERPRINT);
            String message = intent.getStringExtra(EXTRA_CLEAR_TEXT);
            handleActionEncrypt(fingerprint, message, receiver);
        } else if (action.equals(ACTION_DECRYPT)) {
            String message = intent.getStringExtra(EXTRA_ENCRYPTED_TEXT);
            handleActionDecrypt(message, receiver);
        }

    }

    public void handleActionEncrypt(String fingerprint, String message, ResultReceiver receiver) {
        Contract.throwIfArgNull(fingerprint, "fingerprint");
        Contract.throwIfArgNull(message, "message");
        Contract.throwIfArgNull(receiver, "receiver");

        Bundle bundle = new Bundle();
        int ret = doEncryption(bundle, fingerprint, message);
        bundle.putString(EXTRA_FINGERPRINT, fingerprint);
        receiver.send(ret, bundle);
    }

    private void handleActionDecrypt(String message, ResultReceiver receiver) {
        Contract.throwIfArgNull(message, "message");
        Contract.throwIfArgNull(receiver, "receiver");

        Bundle bundle = new Bundle();
        int ret = doDecryption(bundle, message);
        receiver.send(ret, bundle);
    }

    /** Utility method for filling in a bundle with a failure that may happen under normal operation. */
    private int failure(Bundle bundle, String message) {
        bundle.putString(EXTRA_FAILURE_MESSAGE, message);
        return RESULT_FAILURE;
    }

    /** Utility method for filling in a bundle with a failure that should never occur. */
    private int hardFailure(Bundle bundle, String action, Exception ex) {
        String message = "Unexpected exception occurred during " + action;
        return failure(bundle, message);
    }

    /**
     * Fill a bundle with an encrypted message and return status code.
     */
    private int doEncryption(Bundle bundle, String fingerprint, String message) {
        try {
            PublicKeyRing keyRing = mKeyManager.getPublicKeyChain().getKeyRing(fingerprint);

            if (keyRing == null) {
                return failure(bundle, "No public key matching the fingerprint \"" + fingerprint + "\" can be found.");
            }

            List<PublicKey> encryptionKeys = keyRing.getEncryptionKeys();
            if (encryptionKeys.size() == 0) {
                return failure(bundle, "No public keys of fingerprint \"" + fingerprint + "\" support encryption.");
            }

            String encrypted = encryptionKeys.get(0).encrypt(message);
            bundle.putString(EXTRA_ENCRYPTED_TEXT, encrypted);

            return RESULT_SUCCESS;
        } catch (IOException ex) {
            return hardFailure(bundle, "encryption", ex);
        } catch (PGPException ex) {
            return hardFailure(bundle, "encryption", ex);
        }
    }

    /**
     * Fill a bundle with an decrypted message and return status code.
     */
    private int doDecryption(Bundle bundle, String message) {
        try {
            String fingerprint = KeyManager.FINGERPRINT; //TODO retrieve fingerprint associated to private key
            String passphrase = KeyManager.PASSPHRASE; //TODO retrieve passphrase from account management

            SecretKeyRing keyRing = mKeyManager.getSecretKeyChain().getKeyRing(fingerprint);

            if (keyRing == null) {
                return failure(bundle, "No secret key matching the fingerprint \"" + fingerprint + "\" can be found"
                    + "on the device.");
            }

            String decrypted = keyRing.decrypt(message, passphrase);
            bundle.putString(EXTRA_CLEAR_TEXT, decrypted);

            return RESULT_SUCCESS;
            //FIXME IF YOU CAN NOT DECRYPT RETURN PLAIN TEXT
        } catch (PGPException ex) {
            bundle.putString(EXTRA_CLEAR_TEXT, message);
            return RESULT_SUCCESS;
        } catch (DecoderException ex) {
            bundle.putString(EXTRA_CLEAR_TEXT, message);
            return RESULT_SUCCESS;
        } catch (IncorrectPassphraseException ex) {
            return failure(bundle, "Incorrect passphrase to the private key, please try again.");
        } catch (IOException ex) {
            return hardFailure(bundle, "decryption", ex);
        }
    }

}
