package resourcesloader;

import commontypes.Good;
import communication.data.ProcessInfo;
import commontypes.User;
import crypto.Crypto;
import crypto.CryptoException;

import java.io.*;
import java.security.KeyPair;
import java.util.*;


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


/**
 * Class responsible for loading resources and auto generating the static information
 */
public class ResourcesLoader {

    final private static int startingServerPort = 8080;
    final private static int startingPort = 8089;
    private static int notaryPort = 8088;


    private static final String KEYSTORE_TYPE = "JCEKS";
    final private static String address = "localhost";
    final private static String resourcesPath = "../resources/";
    private final static String alias = "userCert";

    private List<User> users = new ArrayList<User>();
    private List<Good> goods = new ArrayList<Good>();
    private List<ProcessInfo> servers = new ArrayList<>();
    private static boolean userNotary = false;


    /**
     * Creates the map with the servers' ports and address
     * @param initialPort the starting port for the servers
     */
    private void createServers(int initialPort, int numberOfServers){

        try {
            // one server is notary
            if(userNotary)
                numberOfServers--;

            for(int i = 0; i < numberOfServers; i++){

                KeyPair keyPair;
                //GenerateKeyStore with user's id
                keyPair = Crypto.generateRSAKeys();

                //Stores public key
                ProcessInfo serverInf = new ProcessInfo(address, initialPort + i, keyPair.getPublic());
                servers.add(serverInf);

                CreateAndStoreCertificate(keyPair, resourcesPath + serverInf.getID(),
                        alias, ("password"+serverInf.getPort()).toCharArray() );
            }

            if(userNotary){
                PublicKey notaryPublicKey = Crypto.
                        getPublicKey("../Server/SEC-Keystore","notary",("password").toCharArray());

                ProcessInfo serverInf = new ProcessInfo(address, notaryPort, notaryPublicKey);
                servers.add(serverInf);
            }

        } catch (CertificateException | OperatorCreationException | NoSuchAlgorithmException | KeyStoreException |
                IOException | CryptoException e) {
            e.printStackTrace();
        }

    }

    /**
     * Stores the given lists of servers
     * @param servers the list to be stored
     * @throws IOException if an error happens
     */
    public static void storeServers(List<ProcessInfo> servers) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(resourcesPath+"serverInfo.ser"));
        out.writeObject(servers);
        out.flush();
        out.close();
    }

    /**
     * loads and deserializes the map containing the serverInformation
     * @return a map with the server info
     */
    public static List<ProcessInfo> loadServersInfo() throws IOException, ClassNotFoundException {
        // Deserialize
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(resourcesPath+"serverInfo.ser"));
        List<ProcessInfo> users = (List<ProcessInfo>) in.readObject();
        in.close();

        return users;
    }


    /**
     * Creates a user and adds it to the list
     * When a user is created a keystore is generated with his name containing his public and private key
     * @param userID user's unique identifier
     * @param port user's port where it will run
     * @param address link or url where the user is found
     * @throws IOException if there are issues with creating a keystore for the use
     */
    private void createUser(String userID, int port, String address) throws IOException {
        User user = new User (userID, port);
        user.setAddress(address);
        users.add(user);

        KeyPair keyPair = null;
        //GenerateKeyStore with user's id
        try {
            keyPair = Crypto.generateRSAKeys();
            user.setPublicKey(keyPair.getPublic());
            user.setPrivateKey(keyPair.getPrivate());

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


    private User getUser(String userID){
        for(User usr : users){
            if (usr.getUserID().equals(userID))
                    return usr;
        }
        return null;
    }

    private String getValueToSign(String goodID, String userID, boolean isForSale, String writer, int wts) {
        return "WRITE|brr|" + goodID + "|" + userID + "|" + isForSale + "|" + writer + "|" + wts;
    }

    /**
     * adds an good/item to a user
     * @param goodID the id of the good to be added
     * @param userID the user who will own the good
     * @param onSale information of whether the good starts for sale or not
     */
    private void addItemToUser(String goodID, String userID, boolean onSale) throws CryptoException {
        Good good = new Good(goodID, userID, onSale);

        String value = getValueToSign(goodID, userID, onSale, userID, 0);
        User user = getUser(userID);
        good.setSignature(Crypto.sign(value.getBytes(), user.getPrivateKey()));
        good.setWriter(userID);

        goods.add(good);
    }

    /**
     * Serializes and stores the userlist so it can be loaded when needed
     * @param users the list of users to be stored
     * @throws IOException when serializing the userlist is impossible
     */
    public static void storeUserList(List<User> users) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(resourcesPath+"userList.ser"));
        out.writeObject(users);
        out.flush();
        out.close();
    }

    /**
     * loads the userList previously serialized and store
     * @return the user list
     * @throws IOException if the file doesn't exist or an issue happened while loading
     * @throws ClassNotFoundException if the serialized class doesn't match user
     */
    public static List<User> loadUserList() throws IOException, ClassNotFoundException {
        // Deserialize
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(resourcesPath+"userList.ser"));
        List<User> users = (List<User>) in.readObject();
        in.close();

        return users;
    }

    /**
     * Serializes and stores the goodsList so it can be loaded when needed
     * @param goods the list of goods
     * @throws IOException when issues arise with the list's storage
     */
    public static void storeGoods(List<Good> goods) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(resourcesPath+"goodsList.ser"));
        out.writeObject(goods);
        out.flush();
        out.close();
    }

    /**
     * loads the previously store goods list
     * @return the list previously stored
     * @throws IOException when IO exception turns upon loading the list
     * @throws ClassNotFoundException when the class loaded has issues or doesn't match Good class
     */
    public static List<Good> loadGoodsList() throws IOException, ClassNotFoundException {

        ObjectInputStream in = new ObjectInputStream(new FileInputStream(resourcesPath+"goodsList.ser"));
        List<Good> goods = (List<Good>) in.readObject();
        in.close();

        return goods;
    }

    /**
     * Loads the serielized list of the notary's good
     * @param path to the notary's goodsList
     * @return the list of goods
     * @throws IOException if error arises while loading
     * @throws ClassNotFoundException if error arises when converting class'
     */
    public static List<Good> loadNotaryGoodsList(String path) throws IOException, ClassNotFoundException {

        ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
        List<Good> goods = (List<Good>) in.readObject();
        in.close();

        return goods;
    }

    public static List<String> loadNonces (String path) throws IOException, ClassNotFoundException {
        ObjectInputStream in = null;
        List<String> nonces = new ArrayList<>();
        try{
            in = new ObjectInputStream(new FileInputStream(path));
            nonces = (List<String>) in.readObject();

        }finally {
            if(in != null){
                in.close();
            }
        }
        return nonces;
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

    /**
     * Creates and stores a self signed certificate
     * @param keyPair the key pair to be stored
     * @param keystoreFileName the name of the keystore file
     * @param alias the alias
     * @param passwordArray the password to protect the key
     * @return the certificate
     */
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

    /**
     * loads the given user's certificate
     * @param user username of the user
     * @param keystorePwd the password storing the certificate
     * @return the certificate
     */
    public Certificate loadUserCertificate(String user, String keystorePwd) throws CryptoException, KeyStoreException {


        KeyStore ks =  loadKeystore(resourcesPath+user + ".jceks", keystorePwd);

        return ks.getCertificate(alias+"-certificate");
    }

    /**
     * Loads the keystore from a file
     * @param keystoreFile file where the keystore is
     * @param keystorePwd keystore password
     * @return the keystore
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

    /**
     * Retrieves the private key for a certain user
     * @param user the user's name
     * @param keystorePwd the password
     * @return the user's privateKey
     */
    public static Key getPrivateKey(String user, String keystorePwd) throws CryptoException {

        KeyStore ks =  loadKeystore(resourcesPath+user + ".jceks", keystorePwd);

        try {
            return ks.getKey(alias+"-privateKey", keystorePwd.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            e.printStackTrace();
            throw new CryptoException("Unable to load privateKey");
        }
    }

    /**
     * gets the privateKey for a server
     * @param port the server's running port
     */
    public static Key getPrivateKey(int port) throws CryptoException {

        KeyStore ks =  loadKeystore(resourcesPath + address+port + ".jceks", ("password"+port));

        try {
            return ks.getKey(alias+"-privateKey", ("password"+port).toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            e.printStackTrace();
            throw new CryptoException("Unable to load privateKey");
        }
    }


    public static void main(String[] args) {
        try {
            ResourcesLoader rsl = new ResourcesLoader();

            int usersCount = 3;
            int itemForSaleCount = 3;
            int itemNotForSaleCount = 3;
            int serverCount = 4;

            if(args.length == 1)
                userNotary = Integer.parseInt(args[0]) != 0;

            if(args. length == 5){
                userNotary = Integer.parseInt(args[0]) != 0;
                usersCount = Integer.parseInt(args[1]);
                itemForSaleCount = Integer.parseInt(args[2]);
                itemNotForSaleCount = Integer.parseInt(args[3]);
                serverCount = Integer.parseInt(args[4]);
            }

            // create all servers and the serverListInfo
            rsl.createServers(startingServerPort, serverCount);


            // create each user
            for(int i = 0; i < usersCount; i++){
                try {
                    rsl.createUser("user"+i, startingPort + i, address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // give 3 items to each user
            for(int j = 0;j<usersCount;j ++) {

                for (int i = 0; i < itemForSaleCount; i++) {
                    rsl.addItemToUser("good" + i + j, rsl.users.get(j % usersCount).getUserID(), true);
                }

                for (int i = 0; i < itemNotForSaleCount; i++) {
                    rsl.addItemToUser("good" + (itemForSaleCount + i) + j,
                            rsl.users.get((itemForSaleCount + j) % usersCount).getUserID(), false);
                }

            }


            // ensure user's private key is not passed
            for(User usr : rsl.users)
                usr.setPrivateKey(null);

            ResourcesLoader.storeUserList(rsl.users);
            ResourcesLoader.storeGoods(rsl.goods);
            ResourcesLoader.storeServers(rsl.servers);
        } catch (IOException | CryptoException e) {
            e.printStackTrace();
            System.out.println("Unable to generate resources");
        }


    }
}
