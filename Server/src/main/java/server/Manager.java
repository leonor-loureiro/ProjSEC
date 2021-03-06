package server;

import commontypes.Good;
import commontypes.User;
import commontypes.Utils;
import communication.data.ProcessInfo;
import communication.exception.SaveNonceException;
import communication.interfaces.IMessageProcess;
import communication.data.Message;
import communication.RequestsReceiver;
import crypto.Crypto;
import crypto.CryptoException;
import pteidlib.PteidException;
import resourcesloader.ResourcesLoader;
import commontypes.AtomicFileManager;
import communication.CitizenCardController;
import sun.security.pkcs11.wrapper.PKCS11Exception;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.setOut;

public class Manager implements IMessageProcess {

    private RequestsReceiver requestReceiver = new RequestsReceiver();

    //Testing mode (does not update real mapping)
    private static boolean TESTING_ON = false;

    //Byzantine Mode
    private static boolean BYZANTINE_ON = false;

    //Name of the file where the users -> goods mapping is stored
    private static final String USERS_GOODS_MAPPING = "../resourcesServer/goods_users";
    private static final String NONCES = "../resourcesServer/nonces";

    //Validity time
    private static final int VALIDITY = 900000;

    // TODO: is this necessary?
    private int wts;

    //Singleton instance
    private static Manager manager = null;

    private static boolean isNotary = false;

    //List of users
    private ArrayList<User> users;
    //List of goods
    private ArrayList<Good> goods;
    //Nonces received
    private ArrayList<String> nonces = new ArrayList<>();
    //Nonces generator
    private Random random = new Random();
    private int port;

    private List<ProcessInfo> serversInfo;

    ProcessInfo server = null;

    private KeyPair tempKeyPair = null;
    private String tempKeySignature = null;

    /**
     *
     * @return
     */
    public static Manager getInstance(){
        if(manager == null)
            manager = new Manager();
        return manager;
    }


    private String userGoodsPath(){
        return USERS_GOODS_MAPPING + port + ".ser";
    }

    private String getNoncesPath(){
        return NONCES + port + ".ser";
    }


    private Manager(){

    }

    public static void setByzantine(boolean mode) {
        BYZANTINE_ON = mode;
    }

    public static boolean getByzantine() {
        return BYZANTINE_ON;
    }

    /**
     * This method is responsible for launching the notary server
     * @param port the port the service runs on
     */
    public void startServer(int port, boolean isNotary) throws IOException, ClassNotFoundException {
        Manager.isNotary = isNotary;
        String host = "localhost";
        //if(Manager.isNotary)
        //    initCC();

        this.port = port;
        System.out.println("Starting server on port " + port + "...");
        loadResources();


        //requestReceiver.initializeInNewThread(port, this);

        try {
            server = new ProcessInfo(host + port, getPrivateKey());
            server.setPort(port);
            server.setHost(host);

            System.out.println(getPrivateKey());
        } catch (CryptoException e) {
            e.printStackTrace();
        }
        requestReceiver.initializeInNewThreadWithEcho(this, serversInfo, 1, server);


        if(isNotary){
            try {
                // generate tempKeyPair to prevent multiple pin requests
                tempKeyPair = Crypto.generateRSAKeys();

                // signed tempPublicKey
                tempKeySignature = Crypto.toString(CitizenCardController.getInstance().sign(tempKeyPair.getPublic().getEncoded()));
                server.setPrivateKey(tempKeyPair.getPrivate());
                server.setTempPubKey(tempKeyPair.getPublic());
                server.setTempKeySignature(tempKeySignature);

            } catch (CryptoException e) {
                e.printStackTrace();
            } catch (PKCS11Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeServer() throws PteidException {
        if(isNotary)
            CitizenCardController.getInstance().exit();

        // stops running thread that's receiving requests
        if(requestReceiver.isRunning()) {
            System.out.println("IS running");
            requestReceiver.stop();
        }
    }

    /**
     * Responsible for loading the user's list, the goods -> user's mapping,
     * and the nonces
     */
    public void loadResources() throws IOException, ClassNotFoundException {
        users = (ArrayList<User>) ResourcesLoader.loadUserList();

        if(new File(userGoodsPath()).exists())
            goods = (ArrayList<Good>) ResourcesLoader.loadNotaryGoodsList(userGoodsPath());
        else
            goods = (ArrayList<Good>)ResourcesLoader.loadGoodsList();

        if(new File(getNoncesPath()).exists()) {
            nonces = (ArrayList<String>) ResourcesLoader.loadNonces(getNoncesPath());
        }else
            nonces = new ArrayList<>();

        serversInfo = ResourcesLoader.loadServersInfo();
    }

    /* **************************************************************************************
     *                      FUNCTIONS THAT PROCESS USER REQUESTS
     * ************************************************************************************/

    /**
     * This method is responsible for processing an intention to sell request
     */
    private Message intentionToSell(Message message) throws CryptoException, SignatureException {

        //Find good
        Good good = findGood(message.getGoodID());
        if(good == null)
            return createErrorMessage("Good does not exist.", message.getSellerID(), null,
                    message.getWts(), message.getRid());

        //Check that good belong to the seller
        if(!good.getUserID().equals(message.getSellerID()))
            return createErrorMessage("Seller ID does not match current owner.",
                    message.getSellerID(), null, message.getWts(), message.getRid());

        //Find seller
        User seller = findUser(message.getSellerID());
        if(seller == null)
            return createErrorMessage("User does not exist", message.getSellerID(), null,
                    message.getWts(), message.getRid());
        PublicKey sellerKey = seller.getPublicKey();

        //Check if the message signature is valid
        if(!isSignatureValid(message, sellerKey))
            return createErrorMessage("Authentication failed.", message.getSellerID(), null,
                    message.getWts(), message.getRid());

        //Update the good state
        //if(!good.isForSale())
            if(!updateGood(good, good.getUserID(), true, message.getWts(), message.getValSignature(), message.getSender()))
                return createErrorMessage("Failed to change good state.", message.getSellerID(), null,
                        message.getWts(), message.getRid());

        //Build response message
        Message response = new Message(Message.Operation.INTENTION_TO_SELL);
        response.setGoodID(good.getGoodID());
        response.setSellerID(seller.getUserID());
        response.setForSale(true);
        response.setWts(message.getWts());

        return response;
    }



    /**
     * This method is responsible for processing a get state of good request
     */
    private Message getStateOfGood(Message message) throws CryptoException, SignatureException {
        Good good = findGood(message.getGoodID());
        if(good == null)
            return createErrorMessage("Good does not exist.", null, message.getBuyerID(),
                    message.getWts(), message.getRid());

        User buyer = findUser(message.getBuyerID());
        if(buyer == null)
            return createErrorMessage("User does not exist.", null, message.getBuyerID(),
                    message.getWts(), message.getRid());

        if(!isSignatureValid(message, buyer.getPublicKey()))
            return createErrorMessage("Authentication failed.", null, message.getBuyerID(),
                    message.getWts(), message.getRid());

        Message response = new Message(Message.Operation.GET_STATE_OF_GOOD);
        response.setGoodID(good.getGoodID());
        response.setSellerID(good.getUserID());
        response.setForSale(good.isForSale());
        response.setBuyerID(buyer.getUserID());
        //Send value write timestamp and signature
        response.setWts(good.getTs());
        response.setValSignature(good.getSignature());
        response.setWriter(good.getWriter());
        //Send read operation ID
        response.setRid(message.getRid());

        return response;
    }

    /**
     * This method is responsible for processing a transfer good request
     */
    private Message transferGood(Message message) throws CryptoException, SignatureException, SaveNonceException {
        Good good = findGood(message.getGoodID());

        // check proof of work
        try {
            if(!Utils.validProofOfWork(Utils.defaultPrefix, message.getDataToChallenge(), message.getProofOfWork()))
                return createErrorMessage("Invalid proof of work.",
                        message.getSellerID(),
                        message.getBuyerID(), message.getWts(), message.getRid());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if(good == null)
            return createErrorMessage("Good does not exist.",
                    message.getSellerID(),
                    message.getBuyerID(), message.getWts(), message.getRid());

        // Check the good is for sale
        if(!good.isForSale())
            return createErrorMessage("Good is currently not for sale.",
                    message.getSellerID(),
                    message.getBuyerID(),
                    message.getWts(),
                    message.getRid());

        // Check the current owner is the seller
        if(!good.getUserID().equals(message.getSellerID()))
            return createErrorMessage("Seller ID does not match current owner.",
                    message.getSellerID(),
                    message.getBuyerID(),
                    message.getWts(),
                    message.getRid());

        // Validate the intention to buy
        if(message.getIntentionToBuy() == null)
            return createErrorMessage("Intention to buy does not exist.",
                    message.getSellerID(),
                    message.getBuyerID(),
                    message.getWts(),
                    message.getRid());

        //Intention to buy fresh
        if(!isFresh(message.getIntentionToBuy()))
            return createErrorMessage("Intention to buy is not fresh",
                    message.getSellerID(),
                    message.getBuyerID(),
                    message.getWts(),
                    message.getRid());

        //Buyer exists
        User buyer = findUser(message.getIntentionToBuy().getBuyerID());
        if(buyer == null)
            return createErrorMessage("Buyer does not exist",
                    message.getSellerID(),
                    message.getBuyerID(),
                    message.getWts(),
                    message.getRid());

        //Validate intention to buy
        if(!isSignatureValid(message.getIntentionToBuy(), buyer.getPublicKey()))
            return createErrorMessage("Intention to buy validation failed.",
                    message.getSellerID(),
                    message.getBuyerID(),
                    message.getWts(),
                    message.getRid());

        //Seller exists
        User seller = findUser(message.getSellerID());
        if(seller == null)
            return createErrorMessage("Seller does not exist",
                    message.getSellerID(),
                    message.getBuyerID(),
                    message.getWts(),
                    message.getRid());

        //Validate intention to sell
        if(!isSignatureValid(message, seller.getPublicKey()))
            return createErrorMessage("Intention to sell validation failed",
                    message.getSellerID(),
                    message.getBuyerID(),
                    message.getWts(),
                    message.getRid());

        //Alter internal mapping of Goods->Users
        if(!updateGood(good, buyer.getUserID(), false, message.getWts(), message.getValSignature(), message.getSender()))
            return createErrorMessage("Failed to update good state",
                    message.getSellerID(),
                    message.getBuyerID(),
                    message.getWts(),
                    message.getRid());

        //Create response message
        Message response = new Message(Message.Operation.TRANSFER_GOOD);
        response.setSellerID(seller.getUserID());
        response.setBuyerID(buyer.getUserID());
        response.setGoodID(good.getGoodID());

        //Set write timestamp
        response.setWts(message.getWts());

        return response;
    }

    private Message writeBack(Message message) throws SignatureException, CryptoException {
        Good good = findGood(message.getGoodID());
        if(good == null)
            return createErrorMessage("Good does not exist", message.getSellerID(), null,
                    message.getWts(), message.getRid());

        User seller = findUser(message.getSellerID());
        if(seller == null)
            return createErrorMessage("Seller does not exist", message.getSellerID(), null,
                    message.getWts(), message.getRid());

        //Buyer is the user that sent the request
        User buyer = findUser(message.getBuyerID());
        if(buyer == null)
            return createErrorMessage("User does not exist", message.getSellerID(), null,
                    message.getWts(), message.getRid());


        if(!isSignatureValid(message, buyer.getPublicKey())){
            return createErrorMessage("Authentication failed", message.getSellerID(), null,
                    message.getWts(), message.getRid());
        }

        if(good.getTs() == message.getWts())
            System.out.println("Already have updated value");
        else
            updateGood(good, message.getSellerID(), message.isForSale(), message.getWts(), message.getValSignature(), message.getWriter());

        Message response = new Message();
        response.setOperation(Message.Operation.WRITE_BACK);
        response.setWts(message.getWts());
        //TODO: necessary? Buyer is the user that sent the request
        response.setSellerID(message.getBuyerID());

        return response;
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
        if(message.getSignature() == null) {
            return false;
        }
        return Crypto.verifySignature(message.getSignature(), message.getBytesToSign(), publicKey);
    }


    /**
     * This class is responsible for processing the Notary service requests
     * @param message with the request
     * @return response
     */
    public Message process(Message message) {
        message.print();
        Message response = null;

        try{
            try {
                if (!isFresh(message))
                    response = createErrorMessage("Request is not fresh",
                            null, null, message.getWts(), message.getRid());

                switch (message.getOperation()) {
                    case INTENTION_TO_SELL:
                        response = intentionToSell(message);
                        break;

                    case GET_STATE_OF_GOOD:
                        response = getStateOfGood(message);
                        break;

                    case TRANSFER_GOOD:
                        response = transferGood(message);
                        break;

                    case WRITE_BACK:
                        response = writeBack(message);
                        break;

                    default:
                        System.out.println("Operation Unknown!");
                }

            } catch (CryptoException e) {
                response = createErrorMessage("Failed to verify the signature",
                        message.getSellerID(), message.getBuyerID(), message.getWts(), message.getRid());

            } catch (SaveNonceException e) {
                response = createErrorMessage("Failed to process request",
                        message.getSellerID(), message.getBuyerID(), message.getWts(), message.getRid());
            }

            if(response != null) {
                response.setSender(server.getID());
                response.setReceiver(message.getSender());
                //Add nonce and timestamp
                addFreshness(response);
                //Sign message
                signMessage(response);

            }
        }catch (SignatureException e) {
            return new Message("Failed to sign the message", message.getSellerID(), message.getBuyerID());
        }

        return response;
    }


    /* ***************************************************************************************
     *                                  AUXILIARY FUNCTIONS
     * ***************************************************************************************/

    /**
     * Checks if a message is fresh and stores the nonce persistently
     */
    public synchronized boolean isFresh(Message message) throws SaveNonceException {
        String nonce = message.getNonce();

        //Check if request is fresh
        if(nonces.contains(nonce))
            return false;

        nonces.add(nonce);
        try {
            if(!TESTING_ON)
                AtomicFileManager.atomicWriteObjectToFile(getNoncesPath(), nonces);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to persistently store nonce");
            throw new SaveNonceException();
        }

        return true;
    }

    /**
     * Responsible for adding a nonce and a timestamp to a message
     */
    private void addFreshness(Message response) {
        response.addFreshness("server_" + port);
    }

     /**
     * This function is responsible for signing a message
     * @param message message to be signed
     * @return signed message
     */
    private Message signMessage(Message message) throws SignatureException {

        // Server's own key
        if(!isNotary){
            try {
                String signature = Crypto.sign(message.getBytesToSign(), getPrivateKey());
                message.setSignature(signature);

            } catch (CryptoException e) {
                e.printStackTrace();
            }

        }else{ // Notary's citizen card
            try {
                message.setTempPubKey(server.getTempPubKey());
                message.setTempKeySignature(server.getTempKeySignature());

                String signature = Crypto.sign(message.getBytesToSign(), tempKeyPair.getPrivate());
                message.setSignature(signature);
            } catch (CryptoException e) {
                System.out.println("Failed to sign: " + e.getMessage());
                e.printStackTrace();
                throw new SignatureException();
            }
        }

        return message;

    }


    /**
     * Creates an error message, adds freshness and signs it
     * @param errorMsg error message text
     * @param sellerID seller ID
     * @param buyerID buyer Id
     * @return Signed, fresh message with operation set to ERROR,
     *         and the given error message
     * @throws SignatureException if the message signature fails
     */
    private Message createErrorMessage(String errorMsg, String sellerID, String buyerID, int wts, int rid)
            throws SignatureException {
        Message message = new Message(errorMsg, sellerID, buyerID);
        message.setWts(wts);
        message.setRid(rid);

        return message;
    }

    /**
     * This function is responsible for updating the state of a good
     * and materializing the changes
     * @param good good to be updated
     * @param userID owner ID
     * @param isForSale whether its for sale or not
     * @param writer
     * @return true if was successful, false otherwise
     */
    private synchronized boolean updateGood(Good good, String userID, boolean isForSale, int ts, String signature, String writer) {

        System.out.println("signatureVal = ");
        System.out.println(signature);

        if(ts <= good.getTs()) {
            System.out.println("Old write " + ts + "/" + good.getTs());
            return false;
        }

        good.setForSale(isForSale);
        good.setUserID(userID);
        good.setTs(ts);
        good.setSignature(signature);
        System.out.println("Set writer: " + writer);
        good.setWriter(writer);

        //For tests, don't update mapping
        if(TESTING_ON)
            return true;

        try {
            AtomicFileManager.atomicWriteObjectToFile(userGoodsPath(), goods);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private PrivateKey getPrivateKey() throws CryptoException {
        if(!isNotary)
            return (PrivateKey) ResourcesLoader.getPrivateKey(port);
        return null;
    }

    private PublicKey getPublicKey(){
        for(ProcessInfo server : serversInfo)
            if(server.getPort() == port)
                return server.getPublicKey();
        return null;
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
