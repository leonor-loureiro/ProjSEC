package resourcesloader;

import commontypes.Good;
import commontypes.User;
import crypto.Crypto;
import crypto.CryptoException;

import java.io.*;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;


import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Date;

public class ResourcesLoader {

    public static final String KEYSTORE_TYPE = "JCEKS";
    final private static int startingPort = 8089;
    final private static String address = "localhost";
    final private static String resourcesPath = "../resources/";
    final public static String alias = "userCert";
    List<User> users = new ArrayList<User>();
    List<Good> goods = new ArrayList<Good>();


    public void createUser(String userID, int port, String address) throws IOException {
        User user = new User (userID, port);
        user.setAddress(address);
        users.add(user);

        KeyPair keyPair = null;
        //GenerateKeyStore with user's id
        try {
            keyPair = Crypto.generateRSAKeys();
            user.setPublicKey(keyPair.getPublic());

        } catch (CryptoException e) {
            e.printStackTrace();
            System.out.println("Unable to create " + userID + "'s keypair");
        }

        try {
            CreateAndStoreCertificate(keyPair, resourcesPath + userID, alias, (userID+userID).toCharArray() );
        } catch (CertificateException | OperatorCreationException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
            System.out.println("unable to create " + userID + "'s certificate");
        }


    }

    public void addItemToUser(String goodID, String userID, boolean onSale){
        Good good = new Good(goodID, userID, onSale);
        goods.add(good);
    }

    public static void storeUserList(List<User> users) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(resourcesPath+"userList.ser"));
        out.writeObject(users);
        out.flush();
        out.close();
    }

    public static List<User> loadUserList() throws IOException, ClassNotFoundException {
        // Deserialize the int[]
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(resourcesPath+"userList.ser"));
        List<User> users = (List<User>) in.readObject();
        in.close();

        return users;
    }

    public static void storeGoods(List<Good> goods) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(resourcesPath+"goodsList.ser"));
        out.writeObject(goods);
        out.flush();
        out.close();
    }

    public static List<Good> loadGoodsList() throws IOException, ClassNotFoundException {
        // Deserialize the int[]
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(resourcesPath+"goodsList.ser"));
        List<Good> goods = (List<Good>) in.readObject();
        in.close();

        return goods;
    }

    public static List<Good> loadNotaryGoodsList(String path) throws IOException, ClassNotFoundException {
        // Deserialize the int[]
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
        List<Good> goods = (List<Good>) in.readObject();
        in.close();

        return goods;
    }



    /**
     * Generates a self signed certificate
     * @param keyPair private and public key
     * @param subjectDN certified subject name
     * @return self signed certificate
     * @throws OperatorCreationException
     * @throws CertificateException
     */
    public static Certificate selfSign(KeyPair keyPair, String subjectDN)
            throws OperatorCreationException, CertificateException {
        Provider bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);

        long now = System.currentTimeMillis();
        Date startDate = new Date(now);

        String cn = "CN="+subjectDN;
        X500Name dnName = new X500Name(cn);

        // Using the current timestamp as the certificate serial number
        BigInteger certSerialNumber = new BigInteger(Long.toString(now));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        // 1 Yr validity
        calendar.add(Calendar.YEAR, 1);

        Date endDate = calendar.getTime();

        // Use appropriate signature algorithm based on your keyPair algorithm.
        String signatureAlgorithm = "SHA256WithRSA";

        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair
                .getPublic().getEncoded());

        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(dnName,
                certSerialNumber, startDate, endDate, dnName, subjectPublicKeyInfo);

        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).setProvider(
                bcProvider).build(keyPair.getPrivate());

        X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);

        Certificate selfSignedCert = new JcaX509CertificateConverter()
                .getCertificate(certificateHolder);

        return selfSignedCert;
    }

    public static Certificate CreateAndStoreCertificate(KeyPair keyPair, String keystoreFileName, String alias, char[] passwordArray)
            throws CertificateException, OperatorCreationException, IOException, KeyStoreException, NoSuchAlgorithmException {

        //Create a keystore
        KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
        ks.load(null,passwordArray);

        //Create self signed exception
        Certificate selfSignedCertificate = selfSign(keyPair, alias);

        //Create private key entry
        KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(keyPair.getPrivate(),new Certificate[] { selfSignedCertificate });

        //Create a protection parameter used to protect the contents of the keystore
        KeyStore.ProtectionParameter password = new KeyStore.PasswordProtection(passwordArray);
        ks.setEntry(alias + "-privateKey", privateKeyEntry, password);

        //Create certificate entry
        ks.setCertificateEntry(alias + "-certificate", selfSignedCertificate);


        //Stores the entry in the keystore
        try (FileOutputStream fos = new FileOutputStream( keystoreFileName + ".jceks")){
            ks.store(fos, passwordArray);
        }

        return selfSignedCertificate;
    }

    public Certificate loadUserCertificate(String user, String keystorePwd) throws CryptoException, KeyStoreException {


        KeyStore ks =  loadKeystore(resourcesPath+user + ".jceks", keystorePwd);

        return ks.getCertificate(alias+"-certificate");
    }

    /**
     * Loads the keystore from a file
     * @param keystoreFile file where the keystore is
     * @param keystorePwd keystore password
     * @return the keystore
     * @throws CryptoException
     */
    private static KeyStore loadKeystore(String keystoreFile, String keystorePwd) throws CryptoException {
        try {
            FileInputStream is = new FileInputStream(keystoreFile);

            KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
            keystore.load(is, keystorePwd.toCharArray());

            return keystore;

        } catch (IOException e) {
            e.printStackTrace();
            throw new CryptoException("File is invalid");
        } catch (Exception e) {
            throw new CryptoException("Failed to load the keystore.");
        }
    }

//    public static Key getPrivateKey(String keystoreFile, String keystorePwd, String alias, String keyPwd) throws CryptoException {
    public static Key getPrivateKey(String user, String keystorePwd) throws CryptoException {

        KeyStore ks =  loadKeystore(resourcesPath+user + ".jceks", keystorePwd);

        try {
            return ks.getKey(alias+"-privateKey", keystorePwd.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            e.printStackTrace();
            throw new CryptoException("Unable to load privateKey");
        }
    }




    public static void main(String[] args) {
        ResourcesLoader rsl = new ResourcesLoader();

        int usersCount = 3;
        int itemForSaleCount = 3;
        int itemNotForSaleCount = 3;

        if(args. length == 3){
            usersCount = Integer.parseInt(args[0]);
            itemForSaleCount = Integer.parseInt(args[1]);
            itemNotForSaleCount = Integer.parseInt(args[2]);
        }

        for(int i = 0; i < usersCount; i++){
            try {
                rsl.createUser("user"+i, startingPort + i, address);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(int j = 0;j<usersCount;j ++) {

            for (int i = 0; i < itemForSaleCount; i++) {
                rsl.addItemToUser("good" + i + j, rsl.users.get(j % usersCount).getUserID(), true);
            }

            for (int i = 0; i < itemNotForSaleCount; i++) {
                rsl.addItemToUser("good" + (itemForSaleCount + i) + j, rsl.users.get((itemForSaleCount + j) % usersCount).getUserID(), false);
            }

        }
        try {
            ResourcesLoader.storeUserList(rsl.users);
            ResourcesLoader.storeGoods(rsl.goods);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
