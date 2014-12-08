package ch.epfl.sweng.bohdomp.dialogue.crypto;

import android.content.Context;

import org.bouncycastle.openpgp.PGPException;

import java.io.IOException;

import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.PublicKeyChain;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.PublicKeyChainBuilder;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.SecretKeyChain;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.SecretKeyChainBuilder;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Utility class to manage storage of keyrings.
 */
public class KeyManager {

    public final static String FINGERPRINT = "6551 C260 FF5C CEBB EE37 83D1 DE75 B27E 59C6 7511";

    public final static String PASSPHRASE = "demo";

    public final static String PUBLIC_KEY_RING = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n"
            + "Version: GnuPG v1\n"
            + "\n"
            + "mQENBFRqKgcBCAD2hCZDhrlHVGG2Rwtj9Q56aoUy/N7VMg4Dq1I8JpZBRNnlR5CM\n"
            + "t3TaJ94OZRjqpZawGWNms6sDKxI7u6KQAkuRedDpMNhql+7ULR9BWVOLD5g41MWa\n"
            + "scFz/UCISmxewQPnmHfJDYDVS1UPu3gr6W057hXF2rLV7+9yRxYW4mAY+KXDGjvV\n"
            + "6GQou0cmmeTZZYR8ZuJapCjBqJ7HUsa12Dg4T+z342U/OtBSfoWLMOOtet2LvOg0\n"
            + "nBSfFy01xIfEcdhfxBdwUvOBqJOt/e+RofaPB0mR2rT6/bBRIJIyY+pp/dkewPnP\n"
            + "XJd8mp60tlTT97Nw1Q3t1yj+p/290fyHB8UTABEBAAG0I0pvaG4gV2hpdGUgPGpv\n"
            + "aG4ud2hpdGVAZXhhbXBsZS5jb20+iQE4BBMBAgAiBQJUaioHAhsDBgsJCAcDAgYV\n"
            + "CAIJCgsEFgIDAQIeAQIXgAAKCRDedbJ+WcZ1ETypCACzRBZiA4ouIK7t17djQaxG\n"
            + "IXUgwopxABq6TthjcMdbQz2BDW1yQP9MjVjyEPTfWZ/lzW5EITQ5GR+dp8pc420P\n"
            + "QIisWNnE+NUH0eK5vWxke592TjLlvoojDF8HAaif+IjlXB5i2RkkgZOt6IYoUKu6\n"
            + "UCAvnS8SOoW0YecYxvmv9VE6xnd/Ufp1I5RdbZKgVH2YrWcQwLwzUWpSlJhHEhqC\n"
            + "hWgw/1q+tbdlwuPLNZyxZPZJAV8omqo4pU/Kf8dt8Kfmwqo9AUTWhX0wkXGkriN/\n"
            + "fMLFeDrG0aybGb2Ik8s97W3eQeigDCItKCTTxs3Wl6afwt6wjEGxlSlY2e3NYU5B\n"
            + "uQENBFRqKgcBCADi69fi2Uhxm4DRt3mSrvgevzY0/ophzwUcWsjaz17gAXG0LIfV\n"
            + "jsgHWo1Sml2/jB5N3BQmN/caE7Ockj1SVb/pK4yWAk/8DmHmrCPRtp/0laOgZD4e\n"
            + "XWoaBSnTprLXvNizaRWJTbdFJAJIP6XsiepPXTJUM8GQibSOCXC/YJNSD4ynvv0s\n"
            + "pbN/gbHobP87pYbBQcQJsut8NluyG9bk7C5IKZ3EcmReh/weQoXLAweCu0/M4r+C\n"
            + "ezOcS+FSK1+uLM92UB/XvUgeuyhwG3KcslT2Hl/snHAwdF0MOsMYj5c8I7raTQVJ\n"
            + "DzREzCYdl99jBdVXPI2Vk7qcPGWpL64H25iNABEBAAGJAR8EGAECAAkFAlRqKgcC\n"
            + "GwwACgkQ3nWyflnGdRGFSQf/RRQdroYDFkM3KrbRY9p0xNqyncxao8AQZqHCzV3D\n"
            + "qW7nUadGv2+2ugSLgPoEfrA7f+FLjIjBXZE0BciLHThw9m7lIexIf/USzB2tTqCA\n"
            + "3Nm+Rl8gCYHvVAulbjfweo5/dhfpiOIYZ/w2qD64oMBJpWO3RArc83goBmc4DS/a\n"
            + "vj1e7GQGR24n0yxSsteFzeVyQNnqJYIkFQLjNjp5q4HgGIljq4EJc6DoJD5lvAtV\n"
            + "HFmaP+XEH5QB8jEl//P13LyicOExJnmW7wbs3fba0uuq52vyEFiV6l39b13Eju9A\n"
            + "i58tXUiHcsLyAfkUA/+DJ47XTuQqykXrUWQPdstlDwMlPA==\n"
            + "=TnUp\n"
            + "-----END PGP PUBLIC KEY BLOCK-----\n";

    public final static String SECRET_KEY_RING = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n"
            + "Version: GnuPG v1\n"
            + "\n"
            + "lQO+BFRqKgcBCAD2hCZDhrlHVGG2Rwtj9Q56aoUy/N7VMg4Dq1I8JpZBRNnlR5CM\n"
            + "t3TaJ94OZRjqpZawGWNms6sDKxI7u6KQAkuRedDpMNhql+7ULR9BWVOLD5g41MWa\n"
            + "scFz/UCISmxewQPnmHfJDYDVS1UPu3gr6W057hXF2rLV7+9yRxYW4mAY+KXDGjvV\n"
            + "6GQou0cmmeTZZYR8ZuJapCjBqJ7HUsa12Dg4T+z342U/OtBSfoWLMOOtet2LvOg0\n"
            + "nBSfFy01xIfEcdhfxBdwUvOBqJOt/e+RofaPB0mR2rT6/bBRIJIyY+pp/dkewPnP\n"
            + "XJd8mp60tlTT97Nw1Q3t1yj+p/290fyHB8UTABEBAAH+AwMCVjvC2OHXT31gnG01\n"
            + "ghf+SSp76rsmgVt6bkK8a8psliuYDKCXF8OE2DSosKviyCN+ZE0R8bGNSVa2r5Nn\n"
            + "u+O+bVqNwWLn5wyWtv69jiVIVTvHWDG3P1XbBz8VNWETlOxUwM1Mi9e0QRBPWxx/\n"
            + "o9+newRYMjatvzqjdleEf1zimsqd7TH7GTPDarE2keMhDmvQmrXptttpmjvbi/+l\n"
            + "z0wP8A5If2EqQO8CSGWtSPmY4XNufL8Rk6RUgp3hcLhihwQkRhrNDshrP+gyqgtA\n"
            + "zyt3F2SCo56qCT5jS1C9QcD5Sxu31duiNG6BtneBZHTM02Z/wocWF2ruc81psHZG\n"
            + "El4BOuIY+WkA3woJBVc0VwZAsITZJHW1WB/EYv/x9+1TM+w4MWvRpXp/IMCeQ9kW\n"
            + "0w+4CPQtSnR6SVDGS30pQ6FtQuu3zNSly/Nr8ZFGVQIrY/GS+JixoLWG6q389kD2\n"
            + "6V8Fzl7wUvim90SMX8mu9fm7SMZnW6OGFARKU8qNt5GNyn4rtkPKTgvHgIEYmdba\n"
            + "OJuG2ywF1024GuWfSvEjQpBrH7XIvWehkz8zzjcbHDT3/kJGgqlGAODQnPpxbzxo\n"
            + "d8HWhWBNfCe3j39DpMKSXRY/z0M+N+Ys3glMKGFbRMWh0FbOxuGW8UWY1hyC8KDs\n"
            + "zMLhp6r/RPLSNDNdN1bU9FgXtKWxQ4wMca5P4wgOkCloTOwhCOgo+qx+rYj6c+NO\n"
            + "rMCczqX0f3CB8oruj4NEk8pxhtBXF+Fdif0P+CQjJdomWTjwIwKnvagd7hdYPtgI\n"
            + "+4vi1aZcQhP3WFZdkQfjEY7n0vZ0ySxikwQQN2mTIi4WNvvJ6VTDz0sPN8aWlFIs\n"
            + "QiPxBbHNDD0ctHM40bS6NqrFZvR0WMMPmrQVAmkwLMyC1z48ODENRt5kgWtYx5As\n"
            + "u7QjSm9obiBXaGl0ZSA8am9obi53aGl0ZUBleGFtcGxlLmNvbT6JATgEEwECACIF\n"
            + "AlRqKgcCGwMGCwkIBwMCBhUIAgkKCwQWAgMBAh4BAheAAAoJEN51sn5ZxnURPKkI\n"
            + "ALNEFmIDii4gru3Xt2NBrEYhdSDCinEAGrpO2GNwx1tDPYENbXJA/0yNWPIQ9N9Z\n"
            + "n+XNbkQhNDkZH52nylzjbQ9AiKxY2cT41QfR4rm9bGR7n3ZOMuW+iiMMXwcBqJ/4\n"
            + "iOVcHmLZGSSBk63ohihQq7pQIC+dLxI6hbRh5xjG+a/1UTrGd39R+nUjlF1tkqBU\n"
            + "fZitZxDAvDNRalKUmEcSGoKFaDD/Wr61t2XC48s1nLFk9kkBXyiaqjilT8p/x23w\n"
            + "p+bCqj0BRNaFfTCRcaSuI398wsV4OsbRrJsZvYiTyz3tbd5B6KAMIi0oJNPGzdaX\n"
            + "pp/C3rCMQbGVKVjZ7c1hTkGdA74EVGoqBwEIAOLr1+LZSHGbgNG3eZKu+B6/NjT+\n"
            + "imHPBRxayNrPXuABcbQsh9WOyAdajVKaXb+MHk3cFCY39xoTs5ySPVJVv+krjJYC\n"
            + "T/wOYeasI9G2n/SVo6BkPh5dahoFKdOmste82LNpFYlNt0UkAkg/peyJ6k9dMlQz\n"
            + "wZCJtI4JcL9gk1IPjKe+/Syls3+Bsehs/zulhsFBxAmy63w2W7Ib1uTsLkgpncRy\n"
            + "ZF6H/B5ChcsDB4K7T8ziv4J7M5xL4VIrX64sz3ZQH9e9SB67KHAbcpyyVPYeX+yc\n"
            + "cDB0XQw6wxiPlzwjutpNBUkPNETMJh2X32MF1Vc8jZWTupw8ZakvrgfbmI0AEQEA\n"
            + "Af4DAwJWO8LY4ddPfWDdx6cu1uVBl9nZVVlG5B06yMdAM/51v4Eshr24IJ7ElarH\n"
            + "SWiZinTsTR88CCU+2gqBs4FjFNXsetf48yUhGMM11B2/giWWU2eqgMPPz9vhilih\n"
            + "Em3l5vawGbqe6Km8gpckJ8PcAiXCOSt/2Qf5JIVhGOD2uzUNOC8mNj1wkPR/XQM3\n"
            + "EyVkFeNl0LPau6SNuIDd9xBzWPO4+KlqztwBoUlb255cM6g4YACC1jEJiEH8/xce\n"
            + "xOiUX1yc38P0jWcLSFj9IOpYx9aQy7WBxoFI5qhvUj+Cmjzt7RFihaxc0aAM/t8X\n"
            + "oWU9zeagkq8BcdwIbSQ0GqJ7nRfB/CJ8KOdwElxlF2iQZt032KHkwvRbxLYmdQ0y\n"
            + "lGFLhifsGbx8MgH+1B+OrVxkGpeYXpX4Tkb6p7NPxYGVgZRPiAC+nd319V722mbM\n"
            + "pzZKN37H/VoU5fNq+6xPynO2a3wioUS0NbTKmx0vMc49NVaUbgog6ekOQ+zLacJ+\n"
            + "uSHyP4ap3tZ1gpTCcpsK56F0DGMA1W0KXWQZAcb59YFbADliEC3KMqyY9PQt6Daf\n"
            + "AUIjEPSNe/yvdTudZvQbbofmRWqyCmjnzdL6LoUxMZzBASBrPwIaFKnSMhRWfCo+\n"
            + "ViG9332KncCvDcpRoBRXCAIm3sasZtlwACLanqhOSJKS/4VhEcS8Ehp39m9SAOnn\n"
            + "CaWIZv7gN6Wm3IkX3FV91yb+5FFc9/cChglrm29asXcbF8t4HaQjkwXEE9Z4pNZW\n"
            + "IKbSS2cReBJYZ0xG51dEQJwRHDxGK59rqMJrEGUzzNan3dVP2WBQv0zVgfG84SHB\n"
            + "9nZJu6QG3KeNGGtqzLEUfd2h6X2JRFfgovj4aecV2D8YyBMr6E+Yo9QOxMuiXdXh\n"
            + "IUuY4VsZVwjAzycwbON5omqaiQEfBBgBAgAJBQJUaioHAhsMAAoJEN51sn5ZxnUR\n"
            + "hUkH/0UUHa6GAxZDNyq20WPadMTasp3MWqPAEGahws1dw6lu51GnRr9vtroEi4D6\n"
            + "BH6wO3/hS4yIwV2RNAXIix04cPZu5SHsSH/1EswdrU6ggNzZvkZfIAmB71QLpW43\n"
            + "8HqOf3YX6YjiGGf8Nqg+uKDASaVjt0QK3PN4KAZnOA0v2r49XuxkBkduJ9MsUrLX\n"
            + "hc3lckDZ6iWCJBUC4zY6eauB4BiJY6uBCXOg6CQ+ZbwLVRxZmj/lxB+UAfIxJf/z\n"
            + "9dy8onDhMSZ5lu8G7N322tLrqudr8hBYlepd/W9dxI7vQIufLV1Ih3LC8gH5FAP/\n"
            + "gyeO107kKspF61FkD3bLZQ8DJTw=\n"
            + "=4Swq\n"
            + "-----END PGP PRIVATE KEY BLOCK-----\n";

    private Context mContext;

    public KeyManager(Context context) {
        Contract.throwIfArgNull(context, "context");

        mContext = context;
    }

    /*
    private <T extends KeyChain<?>> T getKeyChain(String path, KeyChainBuilder<T> builder)
        throws IOException, PGPException {

        File keyRingFile = new File(mContext.getExternalFilesDir(null), path);
        InputStream keyRingStream = null;
        try {
            keyRingStream = new FileInputStream(keyRingFile);
            return builder.fromStream(keyRingStream);
        } catch (FileNotFoundException ex) {
            return builder.empty();
        } finally {
            if (keyRingStream != null) {
                keyRingStream.close();
            }
        }
    }*/

    public PublicKeyChain getPublicKeyChain()
        throws IOException, PGPException {
        //return getKeyChain(PUBLIC_KEY_RING, new PublicKeyChainBuilder());
        return new PublicKeyChainBuilder().fromString(PUBLIC_KEY_RING);
    }

    public SecretKeyChain getSecretKeyChain()
        throws IOException, PGPException {
        //return getKeyChain(SECRET_KEY_RING, new SecretKeyChainBuilder());
        return new SecretKeyChainBuilder().fromString(SECRET_KEY_RING);
    }

}
