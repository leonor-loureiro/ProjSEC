package server;

import commontypes.Good;
import commontypes.User;
import communication.IMessageProcess;
import communication.Message;
import communication.RequestsReceiver;
import crypto.Crypto;
import crypto.CryptoException;
import pteidlib.PteidException;
import resourcesloader.ResourcesLoader;
import server.data.AtomicFileManager;
import server.security.CitizenCardController;
import sun.security.pkcs11.wrapper.PKCS11Exception;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.System.currentTimeMillis;

public class Manager implements IMessageProcess {

    private RequestsReceiver requestReceiver = new RequestsReceiver();

    //Testing mode (does not update real mapping)
    private static boolean TESTING_ON = false;

    //Name of the file where the users -> goods mapping is stored
    private static final String USERS_GOODS_MAPPING = "../resourcesServer/goods_users";

    //Validity time
    private static final int VALIDITY = 900000;

    //Singleton instance
    static Manager manager = null;

    //Handler for the cryptographic operations with the CC
    CitizenCardController ccController;

    //List of users
    private ArrayList<User> users;
    //List of goods
    private ArrayList<Good> goods;
    //Nonces received
    //TODO: make nonce strings
    private ArrayList<String> nonces = new ArrayList<>();
    //Nonces generator
    private Random random = new Random();

    public static Manager getInstance(){
        if(manager == null)
            manager = new Manager();
        return manager;
    }


    private Manager(){
        ccController = new CitizenCardController();
        try {
            ccController.init();
        } catch (Exception e) {
            System.out.println("Failed to initialize Citizen Card Controller");
            System.out.println(e.getMessage());
            //System.exit(0); //Comment for tests
            //If init failed, not CC available
            ccController = null;
        }
    }

    /**
     * This method is responsible for launching the notary server
     * @param port the port the service runs on
     */
    public void startServer(int port) throws IOException, ClassNotFoundException {
        System.out.println("Starting server...");
        loadResources();
        requestReceiver.initializeInNewThread(port, this);
    }

    public void closeServer() throws PteidException {
        if(ccController != null)
            ccController.exit();

        // stops running thread that's receiving requests
        if(requestReceiver.isRunning()) {
            System.out.println("IS running");
            requestReceiver.stop();
        }
    }

    /**
     * Responsible for loading the user's list and the goods -> user's mapping
     */
    public void loadResources() throws IOException, ClassNotFoundException {
        users = (ArrayList<User>) ResourcesLoader.loadUserList();

        if(new File(USERS_GOODS_MAPPING).exists())
            goods = (ArrayList<Good>) ResourcesLoader.loadNotaryGoodsList(USERS_GOODS_MAPPING);
        else
            goods = (ArrayList<Good>)ResourcesLoader.loadGoodsList();
    }

    /* **************************************************************************************
     *                      FUNCTIONS THAT PROCESS USER REQUESTS
     * ************************************************************************************/

    /**
     * This method is responsible for processing an intention to sell request
     */
    public Message intentionToSell(Message message) throws CryptoException, SignatureException {

        //Find good
        Good good = findGood(message.getGoodID());
        if(good == null)
            return createErrorMessage("Good does not exist.");

        //Check that good belong to the seller
        if(!good.getUserID().equals(message.getSellerID()))
            return createErrorMessage("Seller ID does not match current owner.");

        //Find seller
        User seller = findUser(message.getSellerID());
        if(seller == null)
            return createErrorMessage("User does not exist");
        PublicKey sellerKey = seller.getPublicKey();

        //Check if the message signature is valid
        if(!isSignatureValid(message, sellerKey))
            return createErrorMessage("Authentication failed.");

        //Update the good state
        if(!good.isForSale())
            if(!updateGood(good, good.getUserID(), true))
                return createErrorMessage("Failed to change good state.");

        //Build response message
        Message response = new Message(Message.Operation.INTENTION_TO_SELL);
        response.setGoodID(good.getGoodID());
        response.setForSale(true);

        addFreshness(response);

        //Sign the response
        return signMessage(response);
    }



    /**
     * This method is responsible for processing a get state of good request
     */
    private Message getStateOfGood(Message message) throws CryptoException, SignatureException {
        Good good = findGood(message.getGoodID());
        if(good == null)
            return createErrorMessage("Good does not exist.");

        User user = findUser(message.getBuyerID());
        if(user == null)
            return createErrorMessage("User does not exist.");

        if(!isSignatureValid(message, user.getPublicKey()))
            return createErrorMessage("Authentication failed.");

        Message response = new Message(Message.Operation.GET_STATE_OF_GOOD);
        response.setGoodID(good.getGoodID());
        response.setSellerID(good.getUserID());
        response.setForSale(good.isForSale());
        addFreshness(response);

        return signMessage(response);
    }

    /**
     * This method is responsible for processing a transfer good request
     */
    private Message transferGood(Message message) throws CryptoException, SignatureException {
        Good good = findGood(message.getGoodID());
        if(good == null)
            return createErrorMessage("Good does not exist.");

        // Check the good is for sale
        if(!good.isForSale())
            return createErrorMessage("Good is currently not for sale.");

        // Check the current owner is the seller
        if(!good.getUserID().equals(message.getSellerID()))
            return createErrorMessage("Seller ID does not match current owner.");

        // Validate the intention to buy
        if(message.getIntentionToBuy() == null)
            return createErrorMessage("Intention to buy does not exist.");

        //Intention to buy fresh
        if(!isFresh(message.getIntentionToBuy()))
            return createErrorMessage("Intention to buy is not fresh");

        //Buyer exists
        User buyer = findUser(message.getIntentionToBuy().getBuyerID());
        if(buyer == null)
            return createErrorMessage("Buyer does not exist");

        //Validate intention to buy
        if(!isSignatureValid(message.getIntentionToBuy(), buyer.getPublicKey()))
            return createErrorMessage("Intention to buy validation failed.");

        //Seller exists
        User seller = findUser(message.getSellerID());
        if(seller == null)
            return createErrorMessage("Seller does not exist");

        //Validate intention to sell
        if(!isSignatureValid(message, seller.getPublicKey()))
            return createErrorMessage("Intention to sell validation failed");

        //Alter internal mapping of Goods->Users
        if(!updateGood(good, buyer.getUserID(), false))
            return createErrorMessage("Failed to update good state");

        //Create response message
        Message response = new Message(Message.Operation.TRANSFER_GOOD);
        response.setSellerID(seller.getUserID());
        response.setBuyerID(buyer.getUserID());
        response.setGoodID(good.getGoodID());

        //Add node and timestamp
        addFreshness(response);

        //Sign response message
        return signMessage(response);
    }

    /**
     * This method returns the good with given good ID
     */
    private Good findGood(String goodID){
        if(goodID == null)
            return null;
        for(Good good : goods)
            if(good.getGoodID().equals(goodID))
                return good;
        return null;
    }

    /**
     * This method returns the user with given user ID
     * */
    private User findUser(String userID){
        if(userID == null)
            return null;
        for (User user : users)
            if(user.getUserID().equals(userID))
                return user;
        return null;
    }

    /**
     * This method is responsible for validating whether a message signature is valid
     * @param message signed message
     * @param publicKey public key
     * @return true if valid, false otherwise
     */
    private boolean isSignatureValid(Message message, PublicKey  publicKey)
            throws CryptoException {
        if(message.getSignature() == null)
            return false;
        return Crypto.verifySignature(message.getSignature(), message.getBytesToSign(), publicKey);
    }


    /**
     * This class is responsible for processing the Notary service requests
     * @param message with the request
     * @return response
     */
    public Message process(Message message) {
        try{
            try {
                if (!isFresh(message))
                    return createErrorMessage("Request is not fresh");

                switch (message.getOperation()) {
                    case INTENTION_TO_SELL:
                        return intentionToSell(message);

                    case GET_STATE_OF_GOOD:
                        return getStateOfGood(message);

                    case TRANSFER_GOOD:
                        return transferGood(message);

                    default:
                        System.out.println("Operation Unknown!");
                }
            } catch (CryptoException e) {
                return createErrorMessage("Failed to verify the signature");
            }
        }catch (SignatureException e) {
            return new Message("Failed to sign the message");
        }
        return null;
    }

    public boolean isFresh(Message message) throws SignatureException {
        String nonce = message.getNonce();

        //Check if request is fresh
        if((currentTimeMillis() - message.getTimestamp()) > VALIDITY ||
                nonces.contains(nonce))
            return false;

        nonces.add(nonce);
        return true;
    }

    /* ***************************************************************************************
     *                                  AUXILIARY FUNCTIONS
     * ***************************************************************************************/

    /**
     * Responsible for adding a nonce and a timestamp to a message
     */
    private void addFreshness(Message response) {
        response.setTimestamp(currentTimeMillis());
        response.setNonce("server" + random.nextInt());
    }

    /**
     * This function is responsible for signing a message
     * @param message message to be signed
     * @return signed message
     */
    private Message signMessage(Message message) throws SignatureException {
        //CC signature disabled (for test purposes)
        if(ccController == null)
            return message;
        try {
            String signature = Crypto.toString(ccController.sign(message.getBytesToSign()));
            message.setSignature(signature);
            return message;
        } catch (PKCS11Exception e) {
            System.out.println("Failed to sign: " + e.getMessage());
            e.printStackTrace();
            throw new SignatureException();
        }

    }

    /**
     * Creates an error message, adds freshness and signs it
     * @param errorMsg error message text
     * @return Signed, fresh message with operation set to ERROR,
     *         and the given error message
     * @throws SignatureException if the message signature fails
     */
    private Message createErrorMessage(String errorMsg) throws SignatureException {
        Message message = new Message(errorMsg);
        addFreshness(message);
        return signMessage(message);
    }

    /**
     * This function is responsible for updating the state of a good
     * and materializing the changes
     * @param good good to be updated
     * @param userID owner ID
     * @param isForSale whether its for sale or not
     * @return true if was successful, false otherwise
     */
    private boolean updateGood(Good good, String userID, boolean isForSale) {
        good.setForSale(isForSale);
        good.setUserID(userID);

        //For tests, don't update mapping
        if(TESTING_ON)
            return true;

        try {
            AtomicFileManager.atomicWriteObjectToFile(USERS_GOODS_MAPPING, goods);
            return true;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
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

}
