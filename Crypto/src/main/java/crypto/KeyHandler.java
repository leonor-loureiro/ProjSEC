package crypto;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class KeyHandler {


    public static KeyPair generateRSAKeys() throws CryptoException {
        return generateRSAKeys(2048);
    }
    public static KeyPair generateRSAKeys(int blockSize) throws CryptoException {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Failed to generate RSA key pair");
        }
        kpg.initialize(blockSize);
        KeyPair kp = kpg.generateKeyPair();
        return  kp;
    }

    /**
     * Converts the public key bytes to a PublicKey instance
     * @param encoded
     * @return PublicKey instance
     * @throws CryptoException
     */
    public static PublicKey recoverPublicKey(byte[] encoded) throws CryptoException {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        } catch (InvalidKeySpecException e) {
            throw new CryptoException("Invalid key");
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Failed to recover public key");
        }
    }
}