package ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp;

import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPObjectFactory;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.util.encoders.DecoderException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import ch.epfl.sweng.bohdomp.dialogue.exceptions.IncorrectPassphraseException;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;
import ch.epfl.sweng.bohdomp.dialogue.utils.StreamUtils;

/**
 * Provides common cryptographic behaviour of secret keys.
 * The reason for creating another abstraction is that not only secret keys
 * provide decryption functionality, also other classes such as secret keyrings.
 */
public abstract class SecretKeyLike {

    /**
     * Extract the private key of the key matching the given id.
     */
    abstract protected PGPPrivateKey extractPrivateKey(long id, char[] passphrase)
        throws PGPException, IncorrectPassphraseException;

    /**
     * Extract PGP data from a decoded (but not decrypted) datastream
     */
    private static PGPEncryptedDataList extractPgpData(InputStream decoded) throws IOException, PGPException {
        BcPGPObjectFactory factory = new BcPGPObjectFactory(decoded);
        Object o = factory.nextObject();
        if (o == null) {
            throw new PGPException("The encrypted message is not a pgp object stream");
        } else if (o instanceof PGPEncryptedDataList) {
            return (PGPEncryptedDataList) o;
        } else {
            //the first object in the encrypted data stream might be a PGP marker packet
            return (PGPEncryptedDataList) factory.nextObject();
        }
    }

    /**
     * Extract literal message from an object provided by a PGPObjectFactory.
     * Unfortunately the first common ancestor returned by PGPObjectFactory is Object.
     */
    private static PGPLiteralData extractLiteral(Object plain) throws PGPException, IOException {
        if (plain instanceof PGPLiteralData) {
            return (PGPLiteralData) plain;
        } else if (plain instanceof PGPCompressedData) {
            PGPCompressedData compressed = (PGPCompressedData) plain;
            BcPGPObjectFactory fact = new BcPGPObjectFactory(compressed.getDataStream());

            return extractLiteral(fact.nextObject());
        } else if (plain instanceof PGPOnePassSignature) {
            throw new PGPException("Cannot extract message, it is a signature");
        } else {
            throw new PGPException("Message is of unsupported type");
        }
    }

    /** Decode an input stream into a collection of encypted data packets. */
    private static PGPEncryptedDataList decode(InputStream encrypted) throws IOException, PGPException {
        InputStream decoded = null;
        try {
            decoded = PGPUtil.getDecoderStream(encrypted);
            return extractPgpData(decoded);
        } catch (DecoderException ex) { //bouncy castle is inconsistent and throws a single unchecked exception
            throw new PGPException("Cannot decode message. Is it encrypted?", ex);
        } finally {
            if (decoded != null) {
                decoded.close();
            }
        }
    }

    /** Regroups a datapacket that can be decrypted and the matching private key. */
    private class Decryptable {
        private final PGPPublicKeyEncryptedData encrypted;
        private final PGPPrivateKey privateKey;

        Decryptable(PGPPublicKeyEncryptedData enc, PGPPrivateKey priv) {
            this.encrypted = enc;
            this.privateKey = priv;
        }

        PGPPublicKeyEncryptedData getEncrypted() {
            return encrypted;
        }

        PGPPrivateKey getPrivateKey() {
            return privateKey;
        }

    }

    /** Find the first packet that this SecretKeyLike can decrypt. */
    private Decryptable findDecryptable(PGPEncryptedDataList encryptedDataList, char[] passphrase)
        throws IncorrectPassphraseException, PGPException, KeyNotFoundException {

        @SuppressWarnings("unchecked") // bouncy castle returns a raw iterator
        Iterator<PGPPublicKeyEncryptedData> pgpDataIterator = encryptedDataList.getEncryptedDataObjects();

        PGPPrivateKey priv = null;
        PGPPublicKeyEncryptedData data = null;
        while (priv == null && pgpDataIterator.hasNext()) {
            data = pgpDataIterator.next();
            priv = extractPrivateKey(data.getKeyID(), passphrase);
        }

        if (priv == null) {
            throw new KeyNotFoundException("No secret key found that can decrypt this message");
        }

        return new Decryptable(data, priv);
    }

    private InputStream decrypt(Decryptable decryptable) throws IOException, PGPException {
        BcPublicKeyDataDecryptorFactory decryptor = new BcPublicKeyDataDecryptorFactory(decryptable.getPrivateKey());
        InputStream clear = null;

        try {
            clear = decryptable.getEncrypted().getDataStream(decryptor);
            return extractLiteral(new BcPGPObjectFactory(clear).nextObject()).getInputStream();
        } finally {
            //bouncy castle requires the client to close streams
            if (clear != null) {
                clear.close();
            }
        }
    }

    /**
     * Decrypts a generic binary message with the appropriate private key.
     *
     * @param encrypted  input stream containing encrypted data (the message to decrypt)
     * @param decrypted  output stream to which the decrypted message will be written
     * @param passphrase the passphrase of this secret key (use empty string in case the key is not protected by a
     *                   passphrase)
     */
    private void decrypt(InputStream encrypted, OutputStream decrypted, String passphrase)
        throws IOException, PGPException, IncorrectPassphraseException, KeyNotFoundException {

        PGPEncryptedDataList decoded = decode(encrypted);

        Decryptable decryptable = findDecryptable(decoded, passphrase.toCharArray());
        InputStream message = decrypt(decryptable);

        StreamUtils.pipe(message, decrypted);

        message.close();
    }

    private String decryptUnsafe(String message, String passphrase)
        throws PGPException, IncorrectPassphraseException, IOException, KeyNotFoundException {

        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(bytes);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        decrypt(in, out, passphrase);

        in.close();
        out.close();

        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Decrypt a message with this secret key
     */
    public String decrypt(String message, String passphrase)
        throws PGPException, IncorrectPassphraseException, KeyNotFoundException {

        Contract.throwIfArgNull(message, "message");
        Contract.throwIfArgNull(passphrase, "passphrase");

        try {
            return decryptUnsafe(message, passphrase);
        } catch (IOException ex) {
            throw new AssertionError("IOException thrown in impossible place", ex);
        }
    }
}
