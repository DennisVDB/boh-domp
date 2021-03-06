package ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;

import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;
import ch.epfl.sweng.bohdomp.dialogue.utils.StreamUtils;

/**
 * Wraps an OpenPGP public key. This can either be the master key or any sub key.
 * <p/>
 * Functionality and design of cryptographic methods if inspired by the SBT PGP plugin.
 * (https://github.com/sbt/sbt-pgp)
 */
public class PublicKey implements Key {
    
    private final PGPPublicKey mUnderlying;

    /**
     * Size of any temporary buffers used in crypto methods
     */
    private static final int CRYPTO_BUFFER_SIZE = 4096;

    PublicKey(PGPPublicKey underlyingKey) {
        Contract.throwIfArgNull(underlyingKey, "underlyingKey");

        this.mUnderlying = underlyingKey;
    }

    public boolean isMasterKey() {
        return mUnderlying.isMasterKey();
    }

    public String getFingerprint() {
        return FingerprintUtils.fromBytes(mUnderlying.getFingerprint());
    }

    /**
     * Can this key be used to encrypt messages?
     */
    public boolean isEncryptionKey() {
        return mUnderlying.isEncryptionKey();
    }

    /**
     * Create a new pgp encryptor from preferences of this key.
     */
    private PGPEncryptedDataGenerator newEncryptor() {
        int algorithm = mUnderlying.getAlgorithm();
        SecureRandom rand = new SecureRandom();
        PGPDataEncryptorBuilder encryptorBuilder = new BcPGPDataEncryptorBuilder(algorithm)
                .setWithIntegrityPacket(true)
                .setSecureRandom(rand);
        PGPEncryptedDataGenerator encryptor = new PGPEncryptedDataGenerator(encryptorBuilder);
        encryptor.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(mUnderlying).setSecureRandom(rand));
        return encryptor;
    }

    /**
     * Encrypts a generic binary message with this public key. The resulting output will only be
     * readable by someone with the corresponding private key.
     *
     * @param message plaintext message to encrypt
     * @param output  output stream to which the encrypted message will be written
     * @param size    length of the message in bytes
     */
    private void encrypt(InputStream message, OutputStream output, Long size) throws IOException, PGPException {
        Contract.throwIfArg(size < 0, "size must be zero or greater");

        OutputStream armored = null;
        OutputStream encrypted = null;
        OutputStream literal = null;
        try {
            PGPEncryptedDataGenerator encryptor = newEncryptor();

            armored = new ArmoredOutputStream(output);
            encrypted = encryptor.open(armored, new byte[CRYPTO_BUFFER_SIZE]);
            literal = new PGPLiteralDataGenerator()
                    .open(encrypted, PGPLiteralDataGenerator.BINARY, "", size, new Date());

            StreamUtils.pipe(message, literal);
        } finally {
            if (literal != null) {
                literal.close();
            }
            if (encrypted != null) {
                encrypted.close();
            }
            if (armored != null) {
                armored.close();
            }
        }
    }

    private String encryptUnsafe(String message) throws PGPException, IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(bytes);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        encrypt(in, out, (long) bytes.length);

        in.close();
        out.close();

        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Encrypt a given message with this public key.
     */
    public String encrypt(String message) throws PGPException {
        Contract.throwIfArgNull(message, "message");

        try {
            return encryptUnsafe(message);
        } catch (IOException ex) {
            throw new AssertionError("IOException thrown in impossible place", ex);
        }
    }

}
