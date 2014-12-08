package ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp;

import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * A key chain is a collection of key rings, i.e. several master keys with
 * their sub-keys.
 * @param <T> type of key rings contained in this key chain
 */
public abstract class KeyChain<T extends KeyRing<?>> {

    /**
     * Retrieve a list of all key rings contained in this key chain.
     */
    abstract public List<T> getKeyRings();

    /**
     * Get a specific key ring whose master key matches the given fingerprint
     * @return a key ring or null if none matches the fingerprint
     */
    public T getKeyRing(String fingerprint) {
        Contract.throwIfArgNull(fingerprint, "fingerprint");

        for (T ring : getKeyRings()) {
            for (Key key : ring.getKeys()) {
                if (key.getFingerprint().equals(FingerprintUtils.fromString(fingerprint))) {
                    return ring;
                }
            }
        }
        return null;
    }

    /** Returns a new keychain with the given keyring added. */
    abstract public KeyChain<T> add(T ring);

    /** Returns an ascii-armored representation of this key chain. */
    abstract public String toArmored();

}
