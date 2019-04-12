package server.security;
import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;
import sun.security.pkcs11.wrapper.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


public class CitizenCardController {

    //PKCS11
    private PKCS11 pkcs11;
    private long p11_session;
    //Library for windows
    String libName = "pteidpkcs11.dll";


    public CitizenCardController(){
    }

    public void init() throws Exception {
        System.loadLibrary("pteidlibj");

        try {
            //Initialize the eID lib
            pteid.Init("");
            // Don't check the integrity of the ID, address and photo (!)
            pteid.SetSODChecking(false);


        //Get PKCS11 instance

            Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");

            Method getInstanceMethod = pkcs11Class.getDeclaredMethod("getInstance",
                    String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class);
            //new Class[]{String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class});

            pkcs11 = (PKCS11) getInstanceMethod.invoke(null,
                    new Object[]{libName, "C_GetFunctionList", null, false});

        // PKCS11 Reference Guide
        // https://docs.oracle.com/en/java/javase/11/security/pkcs11-reference-guide1.html#GUID-F068390B-EB41-48A0-A713-B4CBCC72285D
        // https://metacpan.org/pod/distribution/Crypt-PKCS11/lib/Crypt/PKCS11/Session.pod

            //Open the PKCS11 session
            p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);
            //Token login
            pkcs11.C_Login(p11_session, 1, null);


        } catch (Exception e) {
            //e.printStackTrace();
            exit();
            throw e;
        }
    }


    private void initializeSignatureMethod(long signatureKey) throws PKCS11Exception {
        //Mechanism specifies how the signature is processed
        //CKM_SHA256_RSA_PKCS = RSA signature with SHA-256

        CK_MECHANISM mechanism = new CK_MECHANISM();
        mechanism.mechanism = PKCS11Constants.CKM_SHA256_RSA_PKCS;
        mechanism.pParameter = null;

        pkcs11.C_SignInit(p11_session, mechanism, signatureKey);
    }

    private long getSignatureKey() throws PKCS11Exception {
        //Search for all private keys by calling C_FindObjects
        //Template must include attribute: CKA_CLASS = CKO_PRIVATE_KEY
        CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
        attributes[0] = new CK_ATTRIBUTE();
        attributes[0].type = PKCS11Constants.CKA_CLASS;
        attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

        pkcs11.C_FindObjectsInit(p11_session, attributes);

        //FindObjects ($maxObjectCount = maximum number of objects to be returned)
        long key = pkcs11.C_FindObjects(p11_session, 5)[0];

        //Terminates the search
        pkcs11.C_FindObjectsFinal(p11_session);

        return key;
    }

    public X509Certificate getAuthenticationCertificate() throws PteidException, CertificateException {
        //Get all certificates
        PTEID_Certif[] certs = pteid.GetCertificates();

        // Get bytes of authentication certificate
        byte[] certificate_bytes = certs[0].certif;

        //Get X509Certificate
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(certificate_bytes);
        return (X509Certificate)f.generateCertificate(in);
    }

    public byte[] sign(byte[] data) throws PKCS11Exception {
        //Get signature key
        long signatureKey = getSignatureKey();

        // Initialize signature method
        initializeSignatureMethod(signatureKey);

        return pkcs11.C_Sign(p11_session, data);
    }


    /**
     * Terminates the eID Lib
     * !!IMPORTANT!! obligatory - must always be called at the end
     */
    public void exit() throws PteidException {
        pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); //Obligatory: terminates the eID Lib
    }

    public static void main(String[] args) {
        CitizenCardController controller = new CitizenCardController();
        try {
            controller.init();
            X509Certificate certificate = controller.getAuthenticationCertificate();
            System.out.println(certificate.getSubjectX500Principal());

            controller.sign("test message".getBytes());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                controller.exit();
            } catch (PteidException e) {
                e.printStackTrace();
            }
        }
    }
}
