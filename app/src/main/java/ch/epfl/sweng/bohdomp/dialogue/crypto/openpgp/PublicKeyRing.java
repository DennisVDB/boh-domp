package ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp;


import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 *  Wraps an OpenPGP public keyring. A keyring is essentially a collection of keys. Even if a single
 *  key is exported, it is considered as a keyring (containing one key).
 */
public class PublicKeyRing implements KeyRing<PublicKey> {

    private final PGPPublicKeyRing mUnderlying;

    /**
     * Returns the underlying pgp keyring.
     * Since bouncy castle's public and secret keys have no common ancestors, and this project's
     * crypto API should not expose its internal dependencies, factoring this method out to a common
     * super-class is not possible.
     * */
    PGPPublicKeyRing getUnderlying() {
        return mUnderlying;
    }

    PublicKeyRing(PGPPublicKeyRing underlyingKeyRing) {
        Contract.throwIfArgNull(underlyingKeyRing, "underlyingKeyRing");

        this.mUnderlying = underlyingKeyRing;
    }

    /** Returns a list of any public keys contained in this keyring. */
    public List<PublicKey> getKeys() {
        ArrayList<PublicKey> keys = new ArrayList<PublicKey>();

        @SuppressWarnings("unchecked") // bouncy castle returns a raw iterator
        Iterator<PGPPublicKey> iterator = mUnderlying.getPublicKeys();

        while (iterator.hasNext()) {
            keys.add(new PublicKey(iterator.next()));
        }

        return keys;
    }

    /** Returns a list of any public keys capable of encrypting. */
    public List<PublicKey> getEncryptionKeys() {
        ArrayList<PublicKey> keys = new ArrayList<PublicKey>();
        for (PublicKey key : getKeys()) {
            if (key.isEncryptionKey()) {
                keys.add(key);
            }
        }
        return keys;
    }

}
