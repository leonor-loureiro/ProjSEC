package client;

import commontypes.Good;
import commontypes.User;
import communication.Communication;
import communication.IMessageProcess;
import communication.Message;
import communication.RequestsReceiver;
import crypto.Crypto;
import crypto.CryptoException;
import resourcesloader.ResourcesLoader;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.System.currentTimeMillis;


public class ClientManager implements IMessageProcess {

    static ClientManager clientManager = null;

    //Validity time
    private static final int VALIDITY = 900000;

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
     * @param login
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws CryptoException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    public void startClient(Login login) throws IOException, ClassNotFoundException, CryptoException, CertificateException, NoSuchAlgorithmException, KeyStoreException {

            //loads the goods from a file
            goods = ResourcesLoader.loadGoodsList();

            // sets the current user
            user = findUser(login.getUsername());

            //(sets the current privatekey of the user
            user.setPrivateKey((PrivateKey) ResourcesLoader.getPrivateKey(login.getUsername(),login.getUsername() + login.getUsername()));

            notaryPublicKey = Crypto.getPublicKey("../Server/SEC-Keystore","notary","password".toCharArray());

            RequestsReceiver requestReceiver = new RequestsReceiver();

            //initliazes the receiver in a new thread
            requestReceiver.initializeInNewThread(findUser(login.getUsername()).getPort(), this);
    }


    /**
     * executes the intention to sell
     * @param goodID id of the good inserted by the user
     * @throws CryptoException
     */
    public void intentionToSell(String goodID) throws CryptoException {

        //creates new message
        Message msg = new Message();

        //checks if the good exists
        Good good = findGood(goodID);
        if(good == null) {
            System.out.println("Good does not exist");
            return;
        }
        msg.setGoodID(goodID);


        //sets the parameters of the massage
        msg.setSellerID(user.getUserID());

        msg.setOperation(Message.Operation.INTENTION_TO_SELL);

        //object to receive response
        Message response = null;

        addFreshness(msg);

        try {
            signMessage(msg);
        } catch (CryptoException e) {
            e.printStackTrace();
        }


        try {
            response = sendRequest.sendMessage("localhost",notaryPort,msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            return;
        }

     /*   if (!isSignatureValid(response, notaryPublicKey)) {
            System.out.println("Notary validation failed");
        } */

        if(response.getOperation().equals(Message.Operation.INTENTION_TO_SELL)){
            System.out.println("State of good " + response.getGoodID() + " is now " + response.isForSale());
        }
        if(response.getOperation().equals(Message.Operation.ERROR)){
            System.out.println(response.getErrorMessage());
        }
    }

    /**
     * executes the getstateofgood operation
     * @param goodID
     * @throws CryptoException
     */
    public void getStateOfGood(String goodID) throws CryptoException {

        Message msg = new Message();

        Good good = findGood(goodID);
        if(good == null) {
            System.out.println("Good does not exist");
            return;
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

        try {
            response = sendRequest.sendMessage("localhost",notaryPort,msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch( IllegalArgumentException e){
            return;
        }


        /* if (!isSignatureValid(response, notaryPublicKey)) {
            System.out.println("Notary validation failed");
        } */

        if(response.getOperation().equals(Message.Operation.GET_STATE_OF_GOOD)){
            System.out.println("Current owner" + " " + response.getSellerID() + " " + "for sale:" + " " + response.isForSale());
            return;

        }
        if(response.getOperation().equals(Message.Operation.ERROR)){
            System.out.println(response.getErrorMessage());
        }
    }

    /**
     * executes the buygood operation
     * @param sellerID user that owns the good
     * @param goodID
     * @throws CryptoException
     */
    public void buyGood(String sellerID, String goodID) throws CryptoException {

        Message msg = new Message();

        Good good = findGood(goodID);
        if(good == null) {
            System.out.println("Good does not exist");
            return;
        }
        msg.setGoodID(goodID);

        if(findUser(sellerID) == null) {
            System.out.println("User does not exist");
            return;
        }
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

        try {
            System.out.println("Sent buygood to " + findUser(sellerID).getPort());
            response = sendRequest.sendMessage("localhost",findUser(sellerID).getPort(),msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch(IllegalArgumentException e){
            return;
        }


        //if the code is transfer good it means the operation was sucessfull
        if(response.getOperation().equals(Message.Operation.TRANSFER_GOOD)){
           /*  if (!isSignatureValid(response, notaryPublicKey)) {
                System.out.println("Notary validation failed");
                return;
                }*/
        }
            System.out.println("Successfully bought good");

        //if it failed it could have failed in the client that received the buy good operation or the notary that received the transfergood
        if(response.getOperation().equals(Message.Operation.ERROR)){

            //if the intention to buy isnt null, it means it failed on the other client
            if(response.getIntentionToBuy() != null){
                if (!isSignatureValid(response, findUser(sellerID).getPublicKey())) {
                    System.out.println("Seller validation failed");
                    return;
                }
                System.out.println(response.getErrorMessage());
            }
            else{
                /*if (!isSignatureValid(response,notaryPublicKey)) {
                    System.out.println("Notary validation failed");
                    return;
                    }*/
                }
                System.out.println(response.getErrorMessage());
        }
    }

    /**
     * executes the transfergood operation based on a previous buygood
     * @param message the received buygood
     * @return the response from the server, or a error message generated in the client.
     * @throws CryptoException
     */
    public Message transferGood(Message message) throws CryptoException {

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

        try {
            response = sendRequest.sendMessage("localhost",notaryPort,msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }catch(IllegalArgumentException e){
            return createErrorMessage("One of the arguments was in wrong form");
        }


       /* if (!isSignatureValid(response, notaryPublicKey)) {
            System.out.println("Notary validation failed");
        } */

        if(response.getOperation().equals(Message.Operation.TRANSFER_GOOD)){
            System.out.println("Sucessfully transfered good " + message.getGoodID() + " to " + message.getBuyerID());
        }
        if(response.getOperation().equals(Message.Operation.ERROR)){
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
    private Message receiveBuyGood(Message message) throws CryptoException, SignatureException {

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


        // é necessario fazes mais verificacoes???
        
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
        }catch (IllegalArgumentException e){
            response = createErrorMessage("Wrong arguments");
            response.setIntentionToBuy(message);
            return response;
        }

        response = transferGood(message);


        /* if (!isSignatureValid(response, notaryPublicKey)) {
            System.out.println("Notary validation failed");
        } */

        return response;


    }

    /**
     * processes the received messages
     * @param message
     * @return
     */
    public Message process(Message message) {
        String nonce = message.getNonce();

        switch (message.getOperation()) {
            case BUY_GOOD:
                try {
                    if((currentTimeMillis() - message.getTimestamp()) > VALIDITY ||
                            nonces.contains(nonce))
                        return createErrorMessage("Request is not fresh");
                    nonces.add(nonce);

                    System.out.println("Received buy good");
                    return receiveBuyGood(message);
                } catch (CryptoException e) {
                    e.printStackTrace();
                } catch (SignatureException e) {
                    e.printStackTrace();
                }
            default:
                System.out.println("Operation Unknown!");
        }
        return null;
    }

    private User findUser(String userID){
        for (User user : users)
            if (user.getUserID().equals(userID))
                return user;
        return null;
    }

    private Good findGood(String goodID){
        for(Good good : goods)
            if(good.getGoodID().equals(goodID))
                return good;
        return null;
    }


    private Message signMessage(Message message) throws CryptoException {
        String signature = Crypto.sign(message.getBytesToSign(), user.getPrivateKey());
        message.setSignature(signature);
        return message;
    }

    private boolean isSignatureValid(Message message, PublicKey  publicKey)
            throws CryptoException {
        if(message.getSignature() == null)
            return false;
        return Crypto.verifySignature(message.getSignature(), message.getBytesToSign(), publicKey);
    }

    private void addFreshness(Message response) {
        response.setTimestamp(currentTimeMillis());
        response.setNonce("server" + random.nextInt());
    }

    private Message createErrorMessage(String errorMsg) throws CryptoException {
        Message message = new Message(errorMsg);
        addFreshness(message);
        return signMessage(message);
    }

    public boolean login(Login login) throws IOException, ClassNotFoundException {

        users = ResourcesLoader.loadUserList();

        if (findUser(login.getUsername()) == null) {
            System.out.println("The username does not exist");
            return false;
        }
        if(!Crypto.checkPassword(login.getUsername(),login.getPassword())){
            return false;
        }

        return true;

    }

}
