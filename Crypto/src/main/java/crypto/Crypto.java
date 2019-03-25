package crypto;


import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Crypto {


    public static String toString(byte[] input) {
        return new String(Base64.getEncoder().encode(input));
    }

    /**
     * Converts a byte array to a string
     *
     * @param input byte array
     * @return converted string
     */
    public static byte[] toByteArray(String input) {
        return Base64.getDecoder().decode(input);
    }


    /**
     * Signs data with a private key
     * @param data data to sign
     * @param signingKey private key used to sign
     * @return signature
     * @throws CryptoException
     */
    public static String sign(byte[] data, PrivateKey signingKey) throws CryptoException {

        if(data == null)
            throw  new CryptoException("Data is null");

        try {
            // Create RSA signature instance
            Signature signAlg = Signature.getInstance("SHA256withRSA");
            // Initialize the signature with the private key
            signAlg.initSign(signingKey);
            // Load the data
            signAlg.update(data);
            // Sign data
            return toString(signAlg.sign());


        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new CryptoException("Invalid Key");
        } catch (Exception e){
            e.printStackTrace();
            throw new CryptoException("Signing failed");
        }
    }

    /**
     * Verifying the signature
     * @param signature
     * @param data data signed
     * @param key public key
     * @return true if valid, false otherwise
     * @throws CryptoException
     */
    public static boolean verifySignature(String signature, byte[] data, PublicKey key) throws CryptoException {
        if(data == null)
            throw  new CryptoException("Data is null");
        if(signature == null)
            throw  new CryptoException("Signature is null");
        try {
            Signature signAlg = Signature.getInstance("SHA256withRSA");
            signAlg.initVerify(key);
            signAlg.update(data);
            return  signAlg.verify(toByteArray(signature));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new CryptoException("Invalid Key");
        } catch (Exception e){
            e.printStackTrace();
            throw new CryptoException("Verification failed");
        }

    }


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
