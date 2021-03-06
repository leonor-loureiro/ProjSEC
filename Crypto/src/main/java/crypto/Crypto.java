package crypto;


import javafx.util.Pair;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.cert.Certificate;

import static javax.crypto.Cipher.ENCRYPT_MODE;

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
     * @param signature the signature
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


    public static PublicKey getPublicKey(String keystoreFileName, String alias, char[] passwordArray)
            throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {

        //Load the keystore
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(new FileInputStream(keystoreFileName), passwordArray);

        Certificate cer = ks.getCertificate(alias);
        return cer.getPublicKey();
    }

    public static boolean checkPassword(String keystoreFileName,char[] passwordArray)  {

        try {
            KeyStore ks = KeyStore.getInstance("jceks");
            ks.load(new FileInputStream("../resources/" +keystoreFileName +".jceks"), passwordArray);

        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            return false;
        }
        return true;
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

    /**
     * Encrypts data with AES
     * @param key AES secret key
     * @param data data to cipher
     * @return ciphered data and IV
     */
    public static Pair<byte[], byte[]> encryptAES(byte[] key, byte[] data, byte[] iv) throws CryptoException {
        //Create secret key
        SecretKeySpec sKey = new SecretKeySpec(key,"AES");

        // Create IV param if not exits
        if(iv == null){
            iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
        }

        IvParameterSpec ivParam = new IvParameterSpec(iv);

        try {
            //Create cipher instance
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(ENCRYPT_MODE, sKey, ivParam);

            // Cipher data
            byte[] cipheredData = cipher.doFinal(data);

            return new Pair<>(cipheredData, cipher.getIV());


        } catch (InvalidKeyException e) {
            throw new CryptoException("Invalid key");
        } catch (Exception e) {
            e.printStackTrace();
            throw new CryptoException("Encryption failed");
        }
    }
}
