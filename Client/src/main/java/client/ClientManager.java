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
import java.util.List;
import java.util.Random;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.setOut;


public class ClientManager implements IMessageProcess {

    static ClientManager clientManager = null;

    List<User> users;
    List<Good> goods;

    Communication sendRequest = new Communication();

    private User user;

    Random random = new Random();

    private static int notaryPort = 8080;
    private PublicKey notaryPublicKey;

    public static ClientManager getInstance(){


        if(clientManager == null)
            clientManager = new ClientManager();
        return clientManager;
    }


    public void startClient(Login login) throws IOException, ClassNotFoundException, CryptoException, CertificateException, NoSuchAlgorithmException, KeyStoreException {

            users = ResourcesLoader.loadUserList();

            goods = ResourcesLoader.loadGoodsList();

            user = findUser(login.getUsername());

            user.setPrivateKey((PrivateKey) ResourcesLoader.getPrivateKey(login.getUsername(),login.getUsername() + login.getUsername()));

            notaryPublicKey = Crypto.getPublicKey("../Server/SEC-Keystore","notary","password".toCharArray());

            RequestsReceiver requestReceiver = new RequestsReceiver();

            requestReceiver.initializeInNewThread(findUser(login.getUsername()).getPort(), this);
    }


    public void intentionToSell(String goodID) throws CryptoException {
        Message msg = new Message();

        msg.setSellerID(user.getUserID());

        msg.setOperation(Message.Operation.INTENTION_TO_SELL);

        Message response = null;

        addFreshness(msg);

        //Find good
        Good good = findGood(goodID);
        if(good == null)
            System.out.println("Good does not exist");

        msg.setGoodID(goodID);

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

    public void getStateOfGood(String goodID) throws CryptoException {

        Message msg = new Message();
        msg.setBuyerID(user.getUserID());;
        msg.setOperation(Message.Operation.GET_STATE_OF_GOOD);
        Message response = null;

        addFreshness(msg);

        Good good = findGood(goodID);
        if(good == null)
            System.out.println("Good does not exist");

        msg.setGoodID(goodID);

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

        }
        if(response.getOperation().equals(Message.Operation.ERROR)){
            System.out.println(response.getErrorMessage());
        }
    }

    public Message transferGood(Message message) throws CryptoException {

        Message msg = new Message();
        msg.setBuyerID(message.getBuyerID());
        msg.setSellerID(message.getSellerID());
        msg.setOperation(Message.Operation.TRANSFER_GOOD);

        msg.setIntentionToBuy(message);
        addFreshness(msg);

        Message response = null;

        User buyer = findUser(message.getBuyerID());

        if (buyer == null)
            return null;

        Good good = findGood(message.getGoodID());
        if(good == null)
            return null;

        msg.setGoodID(message.getGoodID());

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

    public void buyGood(String sellerID, String goodID) throws CryptoException {

        Message msg = new Message();
        msg.setBuyerID(user.getUserID());
        msg.setGoodID(goodID);

        if(findUser(sellerID) == null) {
            System.out.println("Didnt find user");
            return;
        }
        msg.setSellerID(sellerID);
        msg.setOperation(Message.Operation.BUY_GOOD);
        //msg.setOperation(Message.Operation.TRANSFER_GOOD);
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


        if(response.getOperation().equals(Message.Operation.TRANSFER_GOOD)){
             if (!isSignatureValid(response, notaryPublicKey)) {
                System.out.println("Notary validation failed");
                return;
        }
            System.out.println("Successfully bought good");

        }
        if(response.getOperation().equals(Message.Operation.ERROR)){

            if(response.getIntentionToBuy() != null){
                if (!isSignatureValid(response, findUser(sellerID).getPublicKey())) {
                    System.out.println("Seller validation failed");
                    return;
                }
                System.out.println(response.getErrorMessage());
            }
            else{
                if (!isSignatureValid(response,notaryPublicKey)) {
                    System.out.println("Notary validation failed");
                    return;
                }
                System.out.println(response.getErrorMessage());
            }

        }
    }

    public void listGoods() {
        System.out.println("Goods in the system:");
        System.out.println();
        for (int i=0;i < goods.size();i++)
            System.out.println(goods.get(i).getGoodID());

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


    private Message receiveBuyGood(Message message) throws CryptoException, SignatureException {

        Message response;

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

        User buyer = findUser(message.getBuyerID());
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

    private void addFreshness(Message response) {
        response.setTimestamp(currentTimeMillis());
        response.setNonce("server" + random.nextInt());
    }

    private Message createErrorMessage(String errorMsg) throws CryptoException {
        Message message = new Message(errorMsg);
        addFreshness(message);
        return signMessage(message);
    }

    public Message process(Message message) {
        switch (message.getOperation()) {
            case BUY_GOOD:
                try {
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

    public boolean login(Login login) throws IOException, ClassNotFoundException {
        users = ResourcesLoader.loadUserList();

        goods = ResourcesLoader.loadGoodsList();

        return findUser(login.getUsername()) != null;

    }
}
