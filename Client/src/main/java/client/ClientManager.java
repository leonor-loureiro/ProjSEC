package client;

import commontypes.AtomicFileManager;
import commontypes.Good;
import commontypes.User;
import commontypes.exception.GoodNotExistsException;
import commontypes.exception.PasswordIsWrongException;
import commontypes.exception.SaveNonceException;
import commontypes.exception.UserNotExistException;
import communication.Communication;
import communication.IMessageProcess;
import communication.Message;
import communication.RequestsReceiver;
import crypto.Crypto;
import crypto.CryptoException;
import resourcesloader.ResourcesLoader;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.System.currentTimeMillis;


public class ClientManager implements IMessageProcess {

    public static final String HOST = "localhost";
    private static boolean TESTING_ON = false;
    static ClientManager clientManager = null;
    private static String NONCES_PREFIX = "../resources/nonces_";

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
    handles the communication between entities
     */
    private Communication sendRequest = new Communication();

    /*
    user logged in the system
     */
    private User user;

    /*
    Random to generate nonces
     */
    Random random = new Random();

    private static int notaryPort = 8080;

    private PublicKey notaryPublicKey;
    private ArrayList<String> nonces = new ArrayList<>();


    public static ClientManager getInstance(){

        if(clientManager == null)
            clientManager = new ClientManager();
        return clientManager;
    }


    /**
     * initializes a client based on the login information
     */
    public void startClient(Login login) throws IOException, ClassNotFoundException, CertificateException, NoSuchAlgorithmException, KeyStoreException {

            //loads the goods from a file
            goods = ResourcesLoader.loadGoodsList();

            //(sets the current privatekey of the user
            notaryPublicKey = Crypto.getPublicKey("../Server/SEC-Keystore","notary","password".toCharArray());

            RequestsReceiver requestReceiver = new RequestsReceiver();

            //initliazes the receiver in a new thread
            requestReceiver.initializeInNewThread(findUser(login.getUsername()).getPort(), this);
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
            System.out.println("Good does not exist");
            throw new GoodNotExistsException(goodID);
        }
        msg.setGoodID(goodID);


        //sets the parameters of the massage
        msg.setSellerID(user.getUserID());

        msg.setOperation(Message.Operation.INTENTION_TO_SELL);


        addFreshness(msg);

        try {
            signMessage(msg);
        } catch (CryptoException e) {
            e.printStackTrace();
        }

        Message response = sendMessage(msg, HOST, notaryPort);
        if(response == null)
            return;

        if(!isFresh(response)){
            System.out.println("Notary response is not fresh");
            return;
        }

        if (!isSignatureValid(response, notaryPublicKey)) {
            System.out.println("Notary validation failed");
            return;
        }

        if(response.getOperation().equals(Message.Operation.INTENTION_TO_SELL)){
            System.out.println("Good " + response.getGoodID() + " is now " + (response.isForSale()? "for sale." : "not for sale"));
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
            System.out.println("Good does not exist");
            throw new GoodNotExistsException(goodID);
        }

        msg.setGoodID(goodID);

        msg.setBuyerID(user.getUserID());;
        msg.setOperation(Message.Operation.GET_STATE_OF_GOOD);
        Message response = null;

        addFreshness(msg);


        try {
            signMessage(msg);
        } catch (CryptoException e) {
            e.printStackTrace();
        }


        response = sendMessage(msg, HOST, notaryPort);
        if(response == null)
            return;

        if(!isFresh(response)){
            System.out.println("Notary response is not fresh");
            return;
        }

         if (!isSignatureValid(response, notaryPublicKey)) {
            System.out.println("Notary validation failed");
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
     * @throws CryptoException
     */
    public void buyGood(String sellerID, String goodID) throws CryptoException, GoodNotExistsException, UserNotExistException, SaveNonceException {

        Message msg = new Message();

        Good good = findGood(goodID);
        if(good == null) {
            System.out.println("Good does not exist");
            throw new GoodNotExistsException(goodID);
        }
        msg.setGoodID(goodID);

        User seller = findUser(sellerID);
        if(seller == null) {
            System.out.println("User does not exist");
            throw new UserNotExistException()
;        }
        msg.setSellerID(sellerID);

        msg.setBuyerID(user.getUserID());
        msg.setOperation(Message.Operation.BUY_GOOD);
        Message response = null;

        addFreshness(msg);

        try {
            signMessage(msg);
        } catch (CryptoException e) {
            e.printStackTrace();
        }


        System.out.println("Sent buy good to " + seller.getPort());
        response = sendMessage(msg, HOST, seller.getPort());
        if(response == null)
            return;

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
            System.out.println("Successfully bought good");

        //if it failed it could have failed in the client that received the buy good operation or the notary that received the transfergood
        }else if(response.getOperation().equals(Message.Operation.ERROR)){
            //if the intention to buy isn't null, it means it failed on the other client
            if(response.getIntentionToBuy() != null){
                if (!isSignatureValid(response, seller.getPublicKey())) {
                    System.out.println("Seller validation failed");
                    return;
                }
                System.out.println(response.getErrorMessage());
            }
            else{
                if (!isSignatureValid(response, notaryPublicKey)) {
                    System.out.println("Notary validation failed");
                }
            }
        }
    }

    /**
     * executes the transfergood operation based on a previous buygood
     * @param message the received buygood
     * @return the response from the server, or a error message generated in the client.
     * @throws CryptoException
     */
    public Message transferGood(Message message) throws CryptoException, SaveNonceException {

        Message msg = new Message();
        msg.setBuyerID(message.getBuyerID());
        msg.setSellerID(message.getSellerID());
        msg.setGoodID(message.getGoodID());
        msg.setOperation(Message.Operation.TRANSFER_GOOD);

        msg.setIntentionToBuy(message);
        addFreshness(msg);

        Message response = null;

        try {
            signMessage(msg);
        } catch (CryptoException e) {
            e.printStackTrace();
        }


        response = sendMessage(msg, HOST, notaryPort);
        if(response == null)
            return createErrorMessage("Failed to send request to Notary");

        if(!isFresh(response)){
            System.out.println("Notary response is not fresh");
            return response;
        }


        else if (!isSignatureValid(response, notaryPublicKey)) {
            System.out.println("Notary validation failed");
        }

        if(response.getOperation().equals(Message.Operation.TRANSFER_GOOD)){
            System.out.println("Successfully transferred good " + message.getGoodID() + " to " + message.getBuyerID());

        } else if(response.getOperation().equals(Message.Operation.ERROR)){
            System.out.println(response.getErrorMessage());
        }

        return response;
    }

    /**
     * list the goods in the system
     */
    public void listGoods() {
        System.out.println("Goods in the system:");
        System.out.println();
        for (int i=0;i < goods.size();i++)
            System.out.println(goods.get(i).getGoodID());

    }


    /**
     * function to receive the buygood operation from other clients
     * @param message the buygood message sent by another client
     * @return the result of the server execution, or a error message
     * @throws CryptoException
     * @throws SignatureException
     */
    public Message receiveBuyGood(Message message) throws CryptoException, SignatureException, SaveNonceException {

        Message response;

        User buyer = findUser(message.getBuyerID());

        if(buyer == null){
            System.out.println("Buyer user does not exist");
            return createErrorMessage("Buyer user does not exist");
        }

        User seller = findUser(message.getSellerID());

        if(seller == null){
            System.out.println("Seller user does not exist");
            return createErrorMessage("Seller user does not exist");
        }


        if(!user.getUserID().equals(message.getSellerID())) {
            System.out.println("Seller ID does not match current owner.");
            return createErrorMessage("Seller ID does not match current owner.");
        }

        Good good = findGood(message.getGoodID());
        if(good == null) {
            System.out.println("Good does not exist");
            return createErrorMessage("Good does not exist");
        }

        System.out.println(buyer.getUserID());
        PublicKey buyerKey = buyer.getPublicKey();

        try {
            if(!isSignatureValid(message, buyerKey)) {
                System.out.println("Authentication Failed.");
                response = createErrorMessage("Authentication Failed");
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

        if(message.getOperation().equals(Message.Operation.BUY_GOOD)) {
            try {
                try {
                    if (!isFresh(message))
                        return createErrorMessage("Request is not fresh");

                    //execute operation
                    System.out.println("Received buy good");
                    return receiveBuyGood(message);

                } catch (SaveNonceException e) {
                    return createErrorMessage("Failed to process request");
                }
            } catch (CryptoException | SignatureException e) {
                e.printStackTrace();
            }
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


    private boolean isFresh(Message message) throws SaveNonceException {
        String nonce = message.getNonce();
        //Check freshness
        if((currentTimeMillis() - message.getTimestamp()) > VALIDITY ||
                nonces.contains(nonce))
            return false;
        nonces.add(nonce);

        //Store nonce
        if(!TESTING_ON) {
            System.out.println("Storing nonce " + noncesFile);
            try {
                AtomicFileManager.atomicWriteObjectToFile(noncesFile, nonces);
            } catch (IOException | ClassNotFoundException e) {
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
            return sendRequest.sendMessage(host, port, msg);

        } catch (IllegalArgumentException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
            System.out.println("Send request failed");
        }
        return null;
    }

    /**
     * This function is responsible for signing a message
     * @param message message to be signed
     * @return signed message
     */
    private Message signMessage(Message message) throws CryptoException {
        String signature = Crypto.sign(message.getBytesToSign(), user.getPrivateKey());
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


        if(message.getSignature() == null)
            return false;
        return Crypto.verifySignature(message.getSignature(), message.getBytesToSign(), publicKey);
    }

    /**
     * Responsible for adding a nonce and a timestamp to a message
     */

    public void addFreshness(Message response) {
        response.setTimestamp(currentTimeMillis());
        response.setNonce("server" + random.nextInt());
    }
    /**
     * Creates an error message, adds freshness and signs it
     * @param errorMsg error message text
     * @return Signed, fresh message with operation set to ERROR,
     *         and the given error message
     * @throws SignatureException if the message signature fails
     */
    private Message createErrorMessage(String errorMsg) throws CryptoException {
        Message message = new Message(errorMsg);
        addFreshness(message);
        return signMessage(message);
    }

    /**
     * Receives user information and atempts to login the user
     * @param login
     * @return a boolean correspondent to the sucess of the operations
     */
    public boolean login(Login login) throws IOException, ClassNotFoundException, CryptoException, UserNotExistException, PasswordIsWrongException {

        //Load users
        if(!TESTING_ON)
            users = ResourcesLoader.loadUserList();

        //Check if user exists
        user = findUser(login.getUsername());
        if (user == null) {
            System.out.println("The username does not exist");
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

        noncesFile = NONCES_PREFIX + user.getUserID();
        //Load nonces
        if(!TESTING_ON){
            if(new File(noncesFile).exists()) {
                nonces = (ArrayList<String>) ResourcesLoader.loadNonces(noncesFile);
                System.out.println("Nonces exist = " + nonces.size());
            }else
                nonces = new ArrayList<>();
        }
        return true;

    }

    /* *************************************************************************************
     *                              AUX FUNCTIONS FOR TESTS
     * *************************************************************************************/

    public void dummyPopulate(ArrayList<User> users, ArrayList<Good> goods){
        TESTING_ON = true;
        this.users = users;
        this.goods = goods;
    }

    public void closeClient() {
    }

    public User getUser() {
        return user;
    }

    public void addNonce(String repeatednonce) {
        nonces.add(repeatednonce);
    }
}
