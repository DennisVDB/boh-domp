package ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp;

import java.util.Locale;

import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Utility methods for converting and checking fingerprints
 */
public class FingerprintUtils {

    /**
     * Create a pretty fingerprint string from a raw byte array.
     *
     * @see FingerprintUtils#fromString
     */
    public static String fromBytes(byte[] fingerprint) {
        Contract.throwIfArgNull(fingerprint, "fingerprint");

        StringBuilder builder = new StringBuilder(fingerprint.length * 2);
        for (byte b : fingerprint) {
            builder.append(String.format("%02x", b & 0xff));
        }
        return fromString(builder.toString());
    }

    /**
     * Create a pretty fingerprint from a fingerprint in hex string format.
     * A pretty fingerprint is divided into groups of four, uppercase hex digits, i.e
     * AAAA BBBB 0000 0000 0000 0000 0000 0000 0000 0000
     */
    public static String fromString(String fingerprint) {
        Contract.throwIfArgNull(fingerprint, "fingerprint");

        String cleaned = fingerprint.replaceAll("\\s+", "");
        String upper = cleaned.toUpperCase(Locale.US);
        String grouped = upper.replaceAll("(.{4}?)", "$0 "); //add space after every 4 characters
        return grouped.trim();
    }

    /**
     * Format a fingerprint to a key server query format. Such a format
     * consists of the pretty fingerprint without spaces, preceded by "0x".
     */
    public static String toQuery(String fingerprint) {
        Contract.throwIfArgNull(fingerprint, "fingerprint");

        String pretty = fromString(fingerprint);
        String noSpace = pretty.replaceAll("\\s+", "");
        return "0x" + noSpace;
    }

    /**
     * Checks whether a given fingerprint is in a valid format.
     * This is equivalent to creating a pretty fingerprint and checking
     * that it is composed of 10 groups of 4 hex digits.
     */
    public static boolean isValidFingerPrint(String fingerprint) {
        Contract.throwIfArgNull(fingerprint, "fingerprint");

        String pretty = fromString(fingerprint);
        return pretty.matches("([0-9A-F]{4} ){9}[0-9A-F]{4}");
    }

}
