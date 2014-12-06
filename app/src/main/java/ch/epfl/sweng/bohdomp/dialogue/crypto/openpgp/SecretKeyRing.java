package ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.exceptions.IncorrectPassphraseException;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 *  Wraps an OpenPGP secret keyring.
 */
public class SecretKeyRing extends SecretKeyLike implements KeyRing<SecretKey> {
    
    private final PGPSecretKeyRing mUnderlying;

    SecretKeyRing(PGPSecretKeyRing underlyingKeyRing) {
        Contract.throwIfArgNull(underlyingKeyRing, "underlyingKeyRing");

        this.mUnderlying = underlyingKeyRing;
    }

    public List<SecretKey> getKeys() {
        ArrayList<SecretKey> keys = new ArrayList<SecretKey>();

        @SuppressWarnings("unchecked") // bouncy castle returns a raw iterator
        Iterator<PGPSecretKey> iterator = mUnderlying.getSecretKeys();

        while (iterator.hasNext()) {
            keys.add(new SecretKey(iterator.next()));
        }

        return keys;
    }

    @Override
    protected PGPPrivateKey extractPrivateKey(long id, char[] passphrase)
        throws PGPException, IncorrectPassphraseException {

        for (SecretKey key: getKeys()) {
            PGPPrivateKey p = key.extractPrivateKey(id, passphrase);
            if (p != null) {
                return p;
            }
        }

        return null;
    }

}
