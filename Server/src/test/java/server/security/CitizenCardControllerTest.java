package server.security;

import crypto.Crypto;
import crypto.CryptoException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pteidlib.PteidException;
import sun.security.pkcs11.wrapper.PKCS11Exception;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;

public class CitizenCardControllerTest {

    private  CitizenCardController controller = new CitizenCardController();
    private byte[] data = new byte[20];
    private X509Certificate certificate;
    @Before
    public void setUp() throws CertificateException, PteidException {
        (new Random()).nextBytes(data);
        certificate = controller.getAuthenticationCertificate();
    }

    @Test
    public void signSuccess() throws PKCS11Exception, CryptoException {
        String signature = Crypto.toString(controller.sign(data));
        Assert.assertTrue(
            Crypto.verifySignature(signature, data, certificate.getPublicKey())
        );
        try {
            storeCertificate("SEC-keystore",
                    certificate, "notary", "password");
            loadCertificate("SEC-keystore", "notary", "password");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void storeCertificate(String keystore, X509Certificate certificate, String alias, String password)
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {

        //Initialize the keystore
        KeyStore keyStore=KeyStore.getInstance("jks");
        keyStore.load(null,null);

        //Add certificate
        keyStore.setCertificateEntry(alias, certificate);

        //Store keystore
        keyStore.store(new FileOutputStream(keystore),password.toCharArray());
    }

    public Certificate loadCertificate(String keystore, String alias, String password) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        //Load the keystore
        KeyStore keyStore=KeyStore.getInstance("jks");
        keyStore.load(new FileInputStream(keystore),password.toCharArray());

        //Retrieve the certificate
        Certificate cert = keyStore.getCertificate(alias);

        //Display the certificate
        System.out.println(cert.toString());

        return cert;

    }

    @After
    public void cleanUp() throws PteidException {
        controller.exit();
    }

}