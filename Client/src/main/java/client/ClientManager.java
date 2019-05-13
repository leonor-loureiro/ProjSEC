package client;

import commontypes.AtomicFileManager;
import commontypes.Good;
import commontypes.User;
import commontypes.exception.GoodNotExistsException;
import commontypes.exception.PasswordIsWrongException;
import communication.*;
import commontypes.exception.UserNotExistException;
import communication.AuthenticatedPerfectLinks;
import communication.data.Message;
import communication.data.ProcessInfo;
import communication.exception.SaveNonceException;
import communication.interfaces.IMessageProcess;
import communication.registers.ByzantineAtomicRegister;
import crypto.Crypto;
import crypto.CryptoException;
import resourcesloader.ResourcesLoader;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

import static java.lang.System.currentTimeMillis;


public class ClientManager implements IMessageProcess {

    public static final String HOST = "localhost";
    private static boolean TESTING_ON = false;
    private static ClientManager clientManager = null;

    //Validity time
    private static final int VALIDITY = 900000;

    private String noncesFile = null;

    /*
    list of users in the system
     */
    private List<User> users;
    /*
    list of goods in the system
     */
    private List<Good> goods;

    /*
    user logged in the system
     */
    private User user;

    private Map<String, ByzantineAtomicRegister> goodsRegisters;
    /*
    Random to generate nonces
     */

    private PublicKey notaryPublicKey;
    private ArrayList<String> nonces = new ArrayList<>();
    private String logFile;

    private ArrayList<Message> log = new ArrayList<>();
    private ProcessInfo sender;

    public static ClientManager getInstance(){

        if(clientManager == null)
            clientManager = new ClientManager();
        return clientManager;
    }


    /**
     * initializes a client based on the login information
     */
    void startClient(Login login,Boolean mode) throws IOException, ClassNotFoundException, CertificateException, NoSuchAlgorithmException, KeyStoreException {

        //loads the goods from a file
        goods = ResourcesLoader.loadGoodsList();

        //(sets the current privatekey of the user
        notaryPublicKey = Crypto.getPublicKey("../Server/SEC-Keystore","notary","password".toCharArray());

        RequestsReceiver requestReceiver = new RequestsReceiver();

        //initliazes the receiver in a new thread
        requestReceiver.initializeInNewThread(findUser(login.getUsername()).getPort(), this);


        List<ProcessInfo> servers = ResourcesLoader.loadServersInfo();

        goodsRegisters = new HashMap<>();

        for(Good good: goods){
            goodsRegisters.put(good.getGoodID(), new ByzantineAtomicRegister(user.getUserID(), servers, users, user.getPrivateKey(), 1,mode));
        }


        logFile = "../resources/" + this.getUser().getUserID() + ".log";
    }


    /**
     * executes the intention to sell
     * @param goodID id of the good inserted by the user
     */
    public void intentionToSell(String goodID) throws CryptoException, GoodNotExistsException, SaveNonceException {

        //creates new message
        Message msg = new Message();

        //checks if the good exists
        Good good = findGood(goodID);
        if(good == null) {
            throw new GoodNotExistsException(goodID);
        }



        //sets the parameters of the massage
        msg.setSellerID(user.getUserID());
        msg.setGoodID(goodID);
        msg.setForSale(true);

        msg.setOperation(Message.Operation.INTENTION_TO_SELL);


        msg.addFreshness(user.getUserID());

        /*try {
            signMessage(msg, user.getPrivateKey());
        } catch (CryptoException e) {
            e.printStackTrace();
        }*/

        //Message response = sendMessage(msg, HOST, notaryPort);
        Message response = null;
        response = getGoodRegister(goodID).write(msg, user.getUserID());
        if(response == null) {
            System.out.println("Intention to sell " + goodID + " failed.");
            return;
        }

        if( response.getSellerID() == null || !response.getSellerID().equals(user.getUserID())){
            System.out.println("Invalid response");
            return;
        }


        if(response.getOperation().equals(Message.Operation.INTENTION_TO_SELL)){
            //Save operation in log
            saveResponse(response);

            System.out.println("Good " + response.getGoodID() + " is now " +
                    (response.isForSale()? "for sale." : "not for sale"));
        }
        else if(response.getOperation().equals(Message.Operation.ERROR)){
            System.out.println(response.getErrorMessage());
        }
    }


    /**
     * executes the getstateofgood operation
     */
    public void getStateOfGood(String goodID) throws CryptoException, GoodNotExistsException, SaveNonceException {

        Message msg = new Message();

        Good good = findGood(goodID);
        if(good == null) {
            throw new GoodNotExistsException(goodID);
        }

        msg.setGoodID(goodID);

        msg.setBuyerID(user.getUserID());;
        msg.setOperation(Message.Operation.GET_STATE_OF_GOOD);

        msg.addFreshness(user.getUserID());

        Message response = null;


        /*try {
            signMessage(msg, user.getPrivateKey());
            System.out.println(Crypto.verifySignature(msg.getSignature(), msg.getBytesToSign(), user.getPublicKey()));
        } catch (CryptoException e) {
            e.printStackTrace();
        }*/


        //response = sendMessage(msg, HOST, notaryPort);
        response = getGoodRegister(goodID).read(msg);
        if(response == null){
            System.out.println("Get state of good " + goodID + " failed.");
            return;
        }

        if(response.getBuyerID() == null || !response.getBuyerID().equals(user.getUserID())){
            System.out.println("Invalid response");
            return;
        }


        if(response.getOperation().equals(Message.Operation.GET_STATE_OF_GOOD)){
            System.out.println("Current owner" + " " + response.getSellerID() + " " + "for sale:" + " " + response.isForSale());

        }else if(response.getOperation().equals(Message.Operation.ERROR)){
            System.out.println(response.getErrorMessage());
        }
    }

    /**
     * executes the buygood operation
     * @param sellerID user that owns the good
     * @param goodID good we w
     */
    public void buyGood(String sellerID, String goodID) throws CryptoException, GoodNotExistsException, UserNotExistException, SaveNonceException {

        Message msg = new Message();

        Good good = findGood(goodID);
        if(good == null) {
            throw new GoodNotExistsException(goodID);
        }
        msg.setGoodID(goodID);

        User seller = findUser(sellerID);
        if(seller == null) {
            throw new UserNotExistException()
;        }
        msg.setSellerID(sellerID);

        msg.setBuyerID(user.getUserID());
        msg.setOperation(Message.Operation.BUY_GOOD);
        Message response = null;

        msg.addFreshness(user.getUserID());

        try {
            signMessage(msg, user.getPrivateKey());
        } catch (CryptoException e) {
            e.printStackTrace();
        }


        System.out.println("Sent buy good to " + seller.getPort());
        response = sendMessage(msg, HOST, seller.getPort());

        if(response == null) {
            System.out.println("Buy good " + goodID + " failed.");
            return;
        }

        if(response.getBuyerID() == null ||!response.getBuyerID().equals(user.getUserID())){
            System.out.println("Invalid response");
            return;
        }

        if(!isFresh(response)){
            System.out.println("Response is not fresh");
            return;
        }


        //if the code is transfer good it means the operation was successfull
        if(response.getOperation().equals(Message.Operation.TRANSFER_GOOD)){
            if (!isSignatureValid(response, notaryPublicKey)) {
                System.out.println("Notary validation failed");
                return;
            }
            //Save operation in log
            saveResponse(response);
            System.out.println("Successfully bought good");

        // if it failed it could have failed in the client that received the buy good operation or the notary that
        // received the transfer good
        }else if(response.getOperation().equals(Message.Operation.ERROR)){
            //if the intention to buy isn't null, it means it failed on the other client
            if(response.getIntentionToBuy() != null){
                if (!isSignatureValid(response, seller.getPublicKey())) {
                    System.out.println("Seller validation failed");
                    return;
                }
            }
            else{
                if (!isSignatureValid(response, notaryPublicKey)) {
                    System.out.println("Notary validation failed");
                    return;
                }
            }
            System.out.println(response.getErrorMessage());
        }
    }

    /**
     * executes the transfergood operation based on a previous buygood
     * @param message the received buygood
     * @return the response from the server, or a error message generated in the client.
     */
    private Message transferGood(Message message) throws CryptoException, SaveNonceException {

        Good good = findGood(message.getGoodID());
        if(good == null)
            return createErrorMessage("Good does not exist", message.getBuyerID());

        Message msg = new Message();
        msg.setBuyerID(message.getBuyerID());
        msg.setSellerID(message.getSellerID());
        msg.setGoodID(message.getGoodID());
        msg.setOperation(Message.Operation.TRANSFER_GOOD);

        msg.setIntentionToBuy(message);
        msg.addFreshness(user.getUserID());
        Message response = null;



        //response = sendMessage(msg, HOST, notaryPort);

        response = getGoodRegister(good.getGoodID()).write(msg, message.getBuyerID());
        if(response == null) {
            System.out.println("Transfer good " + good.getGoodID() + " to user " + message.getBuyerID() + ".");
            return createErrorMessage("Failed to send request to Notary", message.getBuyerID());
        }

        if(!response.getSellerID().equals(user.getUserID())){
            System.out.println("Invalid response");
            return response;
        }

        //TODO: Clean code
//        if(!isFresh(response)){
//            System.out.println("Notary response is not fresh");
//            return response;
//        }


//        else if (!isSignatureValid(response, notaryPublicKey)) {
//            System.out.println("Notary validation failed");
//            return response;
//
//        }

        if(response.getOperation().equals(Message.Operation.TRANSFER_GOOD)){
            //Save operation in log
            saveResponse(response);
            System.out.println("Successfully transferred good " + message.getGoodID() + " to " + message.getBuyerID());
            return response;


        } else if(response.getOperation().equals(Message.Operation.ERROR)){
            System.out.println(response.getErrorMessage());
            return response;

        }

        return response;
    }

    /**
     * list the goods in the system
     */
    void listGoods() {
        System.out.println("Goods in the system:");
        System.out.println();
        for (Good good : goods)
            System.out.println(good.getGoodID());

    }


    /**
     * function to receive the buygood operation from other clients
     * @param message the buygood message sent by another client
     * @return the result of the server execution, or a error message
     */
    private Message receiveBuyGood(Message message) throws CryptoException, SaveNonceException {

        Message response;

        User buyer = findUser(message.getBuyerID());

        if(buyer == null){
            System.out.println("Buyer user does not exist");
            return createErrorMessage("Buyer user does not exist", message.getBuyerID());
        }

        User seller = findUser(message.getSellerID());

        if(seller == null){
            System.out.println("Seller user does not exist");
            return createErrorMessage("Seller user does not exist", message.getBuyerID());
        }


        if(!user.getUserID().equals(message.getSellerID())) {
            System.out.println("Seller ID does not match my ID");
            return createErrorMessage("Seller ID does not match my ID", message.getBuyerID());
        }

        Good good = findGood(message.getGoodID());
        if(good == null) {
            System.out.println("Good does not exist");
            return createErrorMessage("Good does not exist", message.getBuyerID());
        }

        System.out.println(buyer.getUserID());
        PublicKey buyerKey = buyer.getPublicKey();

        try {
            if(!isSignatureValid(message, buyerKey)) {
                System.out.println("Authentication Failed.");
                response = createErrorMessage("Authentication Failed", message.getBuyerID());
                response.setIntentionToBuy(message);
                return response;
            }
        } catch (CryptoException e) {
            e.printStackTrace();
        }

        return transferGood(message);
    }

    /**
     * processes the received messages
     */
    public Message process(Message message) {

        if(message.getOperation().equals(Message.Operation.BUY_GOOD)) try {
            try {
                if (!isFresh(message))
                    return createErrorMessage("Request is not fresh", message.getBuyerID());

                //execute operation
                System.out.println("Received buy good");
                return receiveBuyGood(message);

            } catch (SaveNonceException e) {
                return createErrorMessage("Failed to process request", message.getBuyerID());
            }
        } catch (CryptoException e) {
            e.printStackTrace();
        }
        System.out.println("Operation Unknown!");
        return null;
    }


    /* ***************************************************************************************
     *                                  AUXILIARY FUNCTIONS
     * ***************************************************************************************/

    /**
     * This method returns the user with given user ID
     * */
    private User findUser(String userID){
        for (User user : users)
            if (user.getUserID().equals(userID))
                return user;
        return null;
    }

    /**
     * This method returns the good with given good ID
     */

    private Good findGood(String goodID){
        for(Good good : goods)
            if(good.getGoodID().equals(goodID))
                return good;
        return null;
    }

    private ByzantineAtomicRegister getGoodRegister(String goodID){
        return goodsRegisters.get(goodID);
    }

    /**
     * This method checks if a message is fresh
     * @param message message to be verified
     */
    private boolean isFresh(Message message) throws SaveNonceException {
        String nonce = message.getNonce();
        //Check freshness
        if((currentTimeMillis() - message.getTimestamp()) > VALIDITY ||
                nonces.contains(nonce))
            return false;
        nonces.add(nonce);

        //Store nonce
        if(!TESTING_ON) {
            try {
                AtomicFileManager.atomicWriteObjectToFile(noncesFile, nonces);
            } catch (IOException e) {
                e.printStackTrace();
                throw new SaveNonceException();
            }
        }

        return true;
    }

    /**
     * This method sends a request to the port at the host
     * @param msg request message
     * @param host host address
     * @param port port number
     * @return response
     */
    private Message sendMessage(Message msg, String host, int port) {
        try {
            return Communication.sendMessage(host, port, msg);

        } catch (IllegalArgumentException | ClassNotFoundException | IOException e) {
            System.out.println("Send request failed");
        }
        return null;
    }

    /**
     * This function is responsible for signing a message
     * @param message message to be signed
     * @return signed message
     */
    private static Message signMessage(Message message, PrivateKey privateKey) throws CryptoException {
        String signature = Crypto.sign(message.getBytesToSign(), privateKey);
        message.setSignature(signature);
        return message;
    }

    /**
     * This method is responsible for validating whether a message signature is valid
     * @param message signed message
     * @param publicKey public key
     */

    private boolean isSignatureValid(Message message, PublicKey  publicKey)
            throws CryptoException {


        if(publicKey.equals(notaryPublicKey)){
            System.out.println("Ignoring notary signature for tests");
            return true;
        }
        return Crypto.verifySignature(message.getSignature(), message.getBytesToSign(), publicKey);
    }


    /**
     * Creates an error message, adds freshness and signs it
     * @param errorMsg error message text
     * @return Signed, fresh message with operation set to ERROR,
     *         and the given error message
     */
    private Message createErrorMessage(String errorMsg, String  buyerID) throws CryptoException {
        Message message = new Message(errorMsg, null, buyerID);
        message.addFreshness(user.getUserID());
        return signMessage(message, user.getPrivateKey());
    }

    /**
     * Receives user information and atempts to login the user
     * @param login information of an attempted login
     * @return a boolean correspondent to the sucess of the operations
     */
    public boolean login(Login login) throws IOException, ClassNotFoundException, CryptoException, UserNotExistException, PasswordIsWrongException {

        //Load users
        if(!TESTING_ON)
            users = ResourcesLoader.loadUserList();

        //Check if user exists
        user = findUser(login.getUsername());
        if (user == null) {
            throw new UserNotExistException();
        }

        //Check if keystore password is valid
        if(!Crypto.checkPassword(login.getUsername(),login.getPassword())){
            throw new PasswordIsWrongException();
        }

        //Retrieve user's private key
        user.setPrivateKey((PrivateKey) ResourcesLoader.getPrivateKey(
                login.getUsername(),login.getUsername() + login.getUsername())
        );

        sender = new ProcessInfo(user.getUserID(),user.getPrivateKey());

        String NONCES_PREFIX = "../resources/nonces_";
        noncesFile = NONCES_PREFIX + user.getUserID();
        //Load nonces
        if(!TESTING_ON){
            if(new File(noncesFile).exists()) {
                nonces = (ArrayList<String>) ResourcesLoader.loadNonces(noncesFile);
            }else
                nonces = new ArrayList<>();
        }

        AuthenticatedPerfectLinks.initialize(VALIDITY, nonces, noncesFile);
        return true;

    }

    /**
     * Saves a response to the log
     * @param response
     */
    private void saveResponse(Message response) {
        log.add(response);
        try {
            AtomicFileManager.atomicWriteObjectToFile(logFile,log);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /* *************************************************************************************
     *                              AUX FUNCTIONS FOR TESTS
     * *************************************************************************************/

    public void dummyPopulate(ArrayList<User> users, ArrayList<Good> goods){
        TESTING_ON = true;
        this.users = users;
        this.goods = goods;
    }

    public User getUser() {
        return user;
    }

    /*
    TODO: Delete if no longer needed
    @Override
    public void authenticate(Message message) throws CryptoException {
        message.addFreshness(user.getUserID());
        signMessage(message, user.getPrivateKey());
    }

    @Override
    public synchronized boolean isValid(Message message) throws SaveNonceException, CryptoException {
        return isFresh(message) && isSignatureValid(message, notaryPublicKey);
    }*/
}
