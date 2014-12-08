package ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp;

import java.util.List;

/**
 * A key chain is a collection of key rings, i.e. several master keys with
 * their sub-keys.
 * @param <T> type of key rings contained in this key chain
 */
public interface KeyChain<T extends KeyRing<?>> {

    /**
     * Retrieve a list of all key rings contained in this key chain.
     */
    List<T> getKeyRings();

    /**
     * Get a specific key ring whose master key matches the given fingerprint
     * @return a key ring or null if none matches the fingerprint
     */
    T getKeyRing(String fingerprint);

    /** Returns a new keychain with the given keyring added. */
    KeyChain<T> add(T ring);

    /** Returns an ascii-armored representation of this key chain. */
    String toArmored();

}
