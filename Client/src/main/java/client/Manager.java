package client;

import commontypes.User;
import commontypes.Good;
import commontypes.Utils;
import communication.Communication;
import communication.IMessageProcess;
import communication.Message;
import communication.RequestsReceiver;
import crypto.Crypto;
import crypto.CryptoException;
import resourcesloader.ResourcesLoader;

import java.io.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Random;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.setOut;


public class Manager implements IMessageProcess {

    private static final String USERS_GOODS_MAPPING = "../../resources/goods_users";
    private static final String USERS_FILE = "../../resources/users_keys";

    static Manager manager = null;

    List<User> users;
    List<Good> goods;

    Communication sendRequest = new Communication();

    private User user;

    Random random = new Random();

    private static int notaryPort = 8080;

    public static Manager getInstance(){


        if(manager == null)
            manager = new Manager();
        return manager;
    }


    public void startClient(Login login) throws IOException, ClassNotFoundException, CryptoException {

            users = ResourcesLoader.loadUserList();

            goods = ResourcesLoader.loadGoodsList();

            user = findUser(login.getUsername());

            user.setPrivateKey((PrivateKey) ResourcesLoader.getPrivateKey(login.getUsername(),login.getUsername() + login.getUsername()));

            RequestsReceiver requestReceiver = new RequestsReceiver();

            requestReceiver.initializeInNewThread(findUser(login.getUsername()).getPort(), this);
    }


    public boolean intentionToSell(String goodID){
        Message msg = new Message();

        msg.setSellerID(user.getUserID());

        msg.setOperation(Message.Operation.INTENTION_TO_SELL);

        Message response = null;

        addFreshness(msg);

        //Find good
        Good good = findGood(goodID);
        if(good == null)
            return false; // should be exception

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
        }

        if(response.getOperation().equals(Message.Operation.INTENTION_TO_SELL)){
            System.out.println("State of good " + response.getGoodID() + " is now " + response.isForSale());
            return true;
        }
        if(response.getOperation().equals(Message.Operation.ERROR)){
            System.out.println(response.getErrorMessage());
            return false; // what to do ask collegues

        }

        return true;
    }

    public boolean getStateOfGood(String goodID){

        Message msg = new Message();
        msg.setBuyerID(user.getUserID());;
        msg.setOperation(Message.Operation.GET_STATE_OF_GOOD);
        Message response = null;

        addFreshness(msg);


        Good good = findGood(goodID);
        if(good == null)
            return false; // should be exception

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
        }

        if(response.getOperation().equals(Message.Operation.GET_STATE_OF_GOOD)){
            System.out.println("Current owner" + " " + response.getSellerID() + " " + "for sale:" + " " + response.isForSale());

        }
        if(response.getOperation().equals(Message.Operation.ERROR)){

        }


        return true;
    }

    public Message transferGood(String sellerName, String goodID){

        Message msg = new Message();
        msg.setBuyerID(user.getUserID());
        msg.setOperation(Message.Operation.TRANSFER_GOOD);

        addFreshness(msg);

        Message response = null;

        Message response2 = new Message();

        User buyer = findUser(sellerName);

        if (buyer == null)
            return null;

        Good good = findGood(goodID);
        if(good == null)
            return null;

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
        }

        if(response.getOperation().equals(Message.Operation.TRANSFER_GOOD)){
            response2.setOperation(Message.Operation.BUY_GOOD);

        }
        if(response.getOperation().equals(Message.Operation.ERROR)){
            System.out.println(response.getErrorMessage());
            response2.setOperation(Message.Operation.ERROR);
            response2.setErrorMessage("Transfer Good failed");

        }

        return response2;
    }

    public void buyGood(String sellerID, String goodID) {
        Message msg = new Message();
        msg.setBuyerID(user.getUserID());
        msg.setGoodID(goodID);

        if(findUser(sellerID) == null) {
            System.out.println("Didnt find user");
            return;
        }
        msg.setSellerID(sellerID);
        msg.setOperation(Message.Operation.BUY_GOOD);
        Message response = null;

        try {
            signMessage(msg);
        } catch (CryptoException e) {
            e.printStackTrace();
        }


        try {
            response = sendRequest.sendMessage("localhost",findUser(sellerID).getPort(),msg);
            System.out.println("Sent buygood to " + findUser(sellerID).getPort());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(response.getOperation().equals(Message.Operation.BUY_GOOD)){
            System.out.println("sucessfuly bought good");

        }
        if(response.getOperation().equals(Message.Operation.ERROR)){
            System.out.println(response.getErrorMessage());

        }
    }

    public void listGoods() throws IOException, ClassNotFoundException {

        Message msg = new Message();
        msg.setBuyerID(user.getUserID());
        msg.setOperation(Message.Operation.LIST_GOODS);
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
        }

        if(response.getOperation().equals(Message.Operation.LIST_GOODS)){

        }
        if(response.getOperation().equals(Message.Operation.ERROR)){

        }

        response.setTimestamp(currentTimeMillis());
        response.setNonce(user.getUserID() + random.nextInt());

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


    private Message receiveBuyGood(Message message) {

        Message response = new Message();

        if(!user.getUserID().equals(message.getSellerID())) {
            response.setOperation(Message.Operation.ERROR);
            response.setErrorMessage("Seller ID does not match current owner.");
        }

        Good good = findGood(message.getGoodID());
        if(good == null) {
            response.setOperation(Message.Operation.ERROR);
            response.setErrorMessage("Good does not exist.");
        }

        if(good.isForSale()) {
            response.setOperation(Message.Operation.ERROR);
            response.setErrorMessage("Good is currently not for sale.");
        }

        response.setIntentionToBuy(message);

        // é necessario fazes mais verificacoes???

        User seller = findUser(message.getSellerID());
        PublicKey sellerKey = seller.getPublicKey();

     /*   try {
            if(!isSignatureValid(message, sellerKey))
                return new Message("Authentication failed.");
        } catch (CryptoException e) {
            e.printStackTrace();
        } */

        return transferGood(message.getSellerID(), message.getGoodID());


    }

    private void addFreshness(Message response) {
        response.setTimestamp(currentTimeMillis());
        response.setNonce("server" + random.nextInt());
    }

    public Message process(Message message) {
        switch (message.getOperation()) {
            case BUY_GOOD:
                return receiveBuyGood(message);
            default:
                System.out.println("Operation Unknown!");
        }
        return null;
    }
}
