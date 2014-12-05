package ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * A collection of all public keys.
 */
public class PublicKeyChain implements KeyChain<PublicKeyRing> {

    private final PGPPublicKeyRingCollection mUnderlying;

    public PublicKeyChain(PGPPublicKeyRingCollection underlying) {
        Contract.throwIfArgNull(underlying, "underlying");

        this.mUnderlying = underlying;
    }

    public List<PublicKeyRing> getKeyRings() {
        List<PublicKeyRing> rings = new ArrayList<PublicKeyRing>();

        @SuppressWarnings("unchecked") // bouncy castle returns a raw iterator
        Iterator<PGPPublicKeyRing> iterator = mUnderlying.getKeyRings();
        while (iterator.hasNext()) {
            rings.add(new PublicKeyRing(iterator.next()));
        }

        return rings;
    }

    public PublicKeyRing getKeyRing(String fingerprint) throws KeyNotFoundException {
        Contract.throwIfArgNull(fingerprint, "fingerprint");

        for (PublicKeyRing ring : getKeyRings()) {
            for (Key key : ring.getKeys()) {
                if (key.getFingerprint().equals(FingerprintUtils.fromString(fingerprint))) {
                    return ring;
                }
            }
        }
        throw new KeyNotFoundException("Key with fingerprint " + fingerprint + " not found");
    }

    public PublicKeyChain add(PublicKeyRing ring) {
        Contract.throwIfArgNull(ring, "ring");

        PGPPublicKeyRingCollection chain =
                PGPPublicKeyRingCollection.addPublicKeyRing(mUnderlying, ring.getUnderlying());

        return new PublicKeyChain(chain);
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
