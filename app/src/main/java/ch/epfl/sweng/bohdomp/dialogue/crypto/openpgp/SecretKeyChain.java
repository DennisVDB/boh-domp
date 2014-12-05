package ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * A collection of all secret keys.
 */
public class SecretKeyChain extends SecretKeyLike implements KeyChain<SecretKeyRing> {

    private final PGPSecretKeyRingCollection mUnderlying;

    public SecretKeyChain(PGPSecretKeyRingCollection underlying) {
        Contract.throwIfArgNull(underlying, "underlying");

        this.mUnderlying = underlying;
    }

    public List<SecretKeyRing> getKeyRings() {
        List<SecretKeyRing> rings = new ArrayList<SecretKeyRing>();

        @SuppressWarnings("unchecked") // bouncy castle returns a raw iterator
                Iterator<PGPSecretKeyRing> iterator = mUnderlying.getKeyRings();
        while (iterator.hasNext()) {
            rings.add(new SecretKeyRing(iterator.next()));
        }

        return rings;
    }

    public SecretKeyRing getKeyRing(String fingerprint) throws KeyNotFoundException {
        Contract.throwIfArgNull(fingerprint, "fingerprint");

        for (SecretKeyRing ring : getKeyRings()) {
            for (Key key : ring.getKeys()) {
                if (key.getFingerprint().equals(FingerprintUtils.fromString(fingerprint))) {
                    return ring;
                }
            }
        }
        throw new KeyNotFoundException("Key with fingerprint " + fingerprint + " not found");
    }

    @Override
    protected PGPPrivateKey extractPrivateKey(long id, char[] passphrase)
        throws PGPException, IncorrectPassphraseException {

        for (SecretKeyRing ring: getKeyRings()) {
            PGPPrivateKey p = ring.extractPrivateKey(id, passphrase);
            if (p != null) {
                return p;
            }
        }

        return null;
    }

    public SecretKeyChain add(SecretKeyRing ring) {
        Contract.throwIfArgNull(ring, "ring");

        PGPSecretKeyRingCollection chain =
                PGPSecretKeyRingCollection.addSecretKeyRing(mUnderlying, ring.getUnderlying());

        return new SecretKeyChain(chain);
    }

    private String toArmoredUnsafe() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ArmoredOutputStream armored = new ArmoredOutputStream(bytes);
        mUnderlying.encode(armored);
        armored.close(); //can't close this in finally as bouncy castle still does some manipulation on close
        bytes.close();
        return new String(bytes.toByteArray(), "UTF-8");
    }

    public String toArmored() {
        try {
            return toArmoredUnsafe();
        } catch (IOException ex) {
            throw new AssertionError("IOError while encoding string");
        }
    }

}

