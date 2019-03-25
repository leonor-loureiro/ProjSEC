import crypto.Crypto;
import crypto.CryptoException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Random;

public class DigitalSignatureTest {
    KeyPair keyPair;
    byte[] dataBytes = new byte[20];
    Random random = new Random();

    @Before
    public void setUp() throws CryptoException {
        keyPair = Crypto.generateRSAKeys();
        random.nextBytes(dataBytes);
    }

    @Test
    public void success() throws CryptoException {
        String signature = Crypto.sign(dataBytes, keyPair.getPrivate());
        Assert.assertTrue(Crypto.verifySignature(signature, dataBytes, keyPair.getPublic()));
    }

    @Test
    public void failureCorrupted() throws CryptoException {
        String signature = Crypto.sign(dataBytes, keyPair.getPrivate());
        random.nextBytes(dataBytes);
        Assert.assertFalse(Crypto.verifySignature(signature, dataBytes, keyPair.getPublic()));
    }

    @Test
    public void failureDiffKey() throws CryptoException {
        String signature = Crypto.sign(dataBytes, keyPair.getPrivate());
        PublicKey key = Crypto.generateRSAKeys().getPublic();
        Assert.assertFalse(Crypto.verifySignature(signature, dataBytes, key));
    }

    @Test(expected = CryptoException.class)
    public void nullDataSign() throws CryptoException {
        String signature = Crypto.sign(null, keyPair.getPrivate());
    }

    @Test(expected = CryptoException.class)
    public void nullKeySign() throws CryptoException {
        String signature = Crypto.sign(dataBytes, null);
    }

    @Test(expected = CryptoException.class)
    public void nullDataVerify() throws CryptoException {
        String signature = Crypto.sign(dataBytes, keyPair.getPrivate());
        Assert.assertTrue(Crypto.verifySignature(signature, null, keyPair.getPublic()));
    }

    @Test(expected = CryptoException.class)
    public void nullKeyVerify() throws CryptoException {
        String signature = Crypto.sign(dataBytes, keyPair.getPrivate());
        Assert.assertTrue(Crypto.verifySignature(signature, dataBytes, null));
    }

    @Test(expected = CryptoException.class)
    public void nullSignatureVerify() throws CryptoException {
        String signature = Crypto.sign(dataBytes, keyPair.getPrivate());
        Assert.assertTrue(Crypto.verifySignature(null, dataBytes, keyPair.getPublic()));
    }


    @After
    public void cleanUp(){
        keyPair = null;
        dataBytes = null;
    }
}
