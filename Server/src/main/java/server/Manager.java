package server;

import commontypes.Good;
import commontypes.User;
import commontypes.Utils;
import communication.IMessageProcess;
import communication.Message;
import communication.RequestsReceiver;
import crypto.Crypto;
import crypto.CryptoException;
import server.data.AtomicFileManager;
import server.security.CitizenCardController;
import sun.security.pkcs11.wrapper.PKCS11Exception;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.System.currentTimeMillis;

public class Manager implements IMessageProcess {

    //Name of the file where the users -> goods mapping is stored
    private static final String USERS_GOODS_MAPPING = "../../resources/goods_users";
    //Name of the file where the users public keys are stored
    private static final String USERS_FILE = "../../resources/users_keys";

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
    private ArrayList<Integer> nonces = new ArrayList<>();
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
            //If init failed, not CC available
            ccController = null;
        }
    }

    /**
     * This method is responsible for launching the notary server
     * @param port the port the service runs on
     */
    public void startServer(int port){
        RequestsReceiver requestReceiver = new RequestsReceiver();

        try {
            requestReceiver.initialize(port, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method is responsible for processing an intention to sell request
     */
    public Message intentionToSell(Message message) throws CryptoException, IOException, PKCS11Exception {

        //Find good
        Good good = findGood(message.getGoodID());
        if(good == null)
            return new Message("Good does not exist.");

        //Check that good belong to the seller
        if(!good.getUserID().equals(message.getSellerID()))
            return new Message("Seller ID does not match current owner.");

        //Find seller
        User seller = findUser(message.getSellerID());
        PublicKey sellerKey = seller.getPublicKey();

        //Check if the message signature is valid
        if(!isSignatureValid(message, sellerKey))
            return new Message("Authentication failed.");

        //Update the good state
        if(!good.isForSale())
            if(!updateGood(good, good.getUserID(), true))
                return new Message("Failed to change good state.");

        //Build response message
        Message response = new Message(Message.Operation.INTENTION_TO_SELL);
        response.setTimestamp(currentTimeMillis());
        response.setNonce(random.nextInt());

        //Sign the response
        return signMessage(response);
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
        try {
            AtomicFileManager.atomicWriteObjectToFile(USERS_GOODS_MAPPING, goods);
            return true;

        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
    }


    /**
     * This method is responsible for processing a get state of good request
     */
    private Message getStateOfGood(Message message) throws CryptoException, PKCS11Exception {
        Good good = findGood(message.getGoodID());
        if(good == null)
            return new Message("Good does not exist.");

        User user = findUser(message.getBuyerID());
        if(user == null)
            return new Message("User does not exist.");

        if(!isSignatureValid(message, user.getPublicKey()))
            return new Message("Authentication failed.");

        Message response = new Message(Message.Operation.GET_STATE_OF_GOOD);
        response.setGoodID(good.getGoodID());
        response.setSellerID(good.getUserID());
        response.setForSale(good.isForSale());

        return signMessage(response);
    }

    /**
     * This method is responsible for processing a transfer good request
     */
    private Message transferGood(Message message) throws CryptoException, PKCS11Exception {
        Good good = findGood(message.getGoodID());
        if(good == null)
            return new Message("Good does not exist.");

        // Check the good is for sale
        if(!good.isForSale())
            return new Message("Good is currently not for sale.");

        // Check the current owner is the seller
        if(!good.getUserID().equals(message.getSellerID()))
            return new Message("Seller ID does not match current owner.");

        // Validate the intention to buy
        User buyer = findUser(message.getIntentionToBuy().getBuyerID());
        if(!isSignatureValid(message.getIntentionToBuy(), buyer.getPublicKey()))
            return new Message("Intention to buy validation failed.");

        // Validate the intention to sell
        User seller = findUser(message.getSellerID());
        if(!isSignatureValid(message, seller.getPublicKey()))
            return new Message("Intention to sell validation failed");


        //Alter internal mapping of Goods->Users
        if(!updateGood(good, buyer.getUserID(), false))
            return new Message("Failed to update good state");

        //Create response message
        Message response = new Message(Message.Operation.TRANSFER_GOOD);
        response.setSellerID(seller.getUserID());
        response.setBuyerID(buyer.getUserID());
        response.setTimestamp(currentTimeMillis());
        response.setNonce(random.nextInt());

        //Sign response message
        return signMessage(response);
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

    /**
     * This method returns the user with given user ID
     * */
    private User findUser(String userID){
        for (User user : users)
            if(user.getUserID().equals(userID))
                return user;
        return null;
    }

    /**
     * This method is responsible for validating whether a message signature is valid
     * @param message signed message
     * @param publicKey public key
     * @return
     * @throws CryptoException
     */
    private boolean isSignatureValid(Message message, PublicKey  publicKey)
            throws CryptoException {
        if(message.getSignature() == null)
            return false;
        return Crypto.verifySignature(message.getSignature(), message.getBytesToSign(), publicKey);
    }

    /**
     * This function is responsible for signing a message
     * @param message
     * @return signed message
     * @throws CryptoException
     */
    private Message signMessage(Message message) throws PKCS11Exception {
        //CC signature disabled (for test purposes)
        if(ccController == null)
            return message;

        String signature = Crypto.toString(ccController.sign(message.getBytesToSign()));
        message.setSignature(signature);
        return message;
    }


    /**
     * This class is responsible for processing the Notary service requests
     * @param message with the request
     * @return response
     */
    public Message process(Message message) {
        int nonce = message.getNonce();
        nonces.add(nonce);
        try {
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
            return new Message("Failed to verify the signature");
        } catch (IOException e) {
            return new Message("Failed to update good state");
        } catch (PKCS11Exception e) {
            return new Message("Failed to sign");        }
        return null;
    }
}
