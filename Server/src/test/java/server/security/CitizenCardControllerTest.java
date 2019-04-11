package server.security;

import crypto.Crypto;
import crypto.CryptoException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pteidlib.PteidException;
import sun.security.pkcs11.wrapper.PKCS11Exception;

import java.security.cert.X509Certificate;
import java.util.Random;

public class CitizenCardControllerTest {

    private  CitizenCardController controller = new CitizenCardController();
    private byte[] data = new byte[20];
    private X509Certificate certificate;
    @Before
    public void setUp() throws Exception {
        (new Random()).nextBytes(data);
        controller.init();
        certificate = controller.getAuthenticationCertificate();
    }

    @Test
    public void signSuccess() throws PKCS11Exception, CryptoException {
        String signature = Crypto.toString(controller.sign(data));
        Assert.assertTrue(
            Crypto.verifySignature(signature, data, certificate.getPublicKey())
        );

    }

  @After
    public void cleanUp() throws PteidException {
        controller.exit();
    }

}