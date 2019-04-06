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

    List<User> users = Utils.initializeUsers();
    List<Good> goods = Utils.initializeGoods();

    Communication sendRequest = new Communication();

    private User user;

    Random random = new Random();

    private static int notaryPort = 8080;

    public static Manager getInstance(){


        if(manager == null)
            manager = new Manager();
        return manager;
    }


    private Manager(){
    }


    public void login(Login login){
        //throws BadArgument, InvalidUser {
        setUser(findUser(login.getUsername()));

    }


    public void setUser(User user) {this.user = user; }

    public void startClient(String username){
        RequestsReceiver requestReceiver = new RequestsReceiver();

            requestReceiver.initializeInNewThread(findUser(username).getPort(), this);
    }


    public boolean intentionToSell(String goodID){
        Message msg = new Message();

        msg.setSellerID(user.getUserID());

        msg.setOperation(Message.Operation.INTENTION_TO_SELL);

        Message response = null;

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
            System.out.println("Current owner" + " " + response.getSellerID() + "for sale:" + " " + response.isForSale());

        }
        if(response.getOperation().equals(Message.Operation.ERROR)){

        }

        response.setTimestamp(currentTimeMillis());
        response.setNonce(random.nextInt());

        return true;
    }

    public Message transferGood(String sellerName, String goodID){

        Message msg = new Message();
        msg.setBuyerID(user.getUserID());
        msg.setOperation(Message.Operation.INTENTION_TO_SELL);
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
            response2.setOperation(Message.Operation.ERROR);
            response2.setErrorMessage("Transfer Good failed");

        }

        return response2;
    }

    public void buyGood(String sellerID, String goodID) {
        Message msg = new Message();
        msg.setBuyerID(user.getUserID());
        msg.setGoodID(goodID);

        if(findUser(sellerID) == null)
            return;
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(response.getOperation().equals(Message.Operation.BUY_GOOD)){

        }
        if(response.getOperation().equals(Message.Operation.ERROR)){

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
        response.setNonce(random.nextInt());

    }

    private User findUser(String userID){
        for (User user : users)
            if(user.getUserID().equals(userID))
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


        // é necessario fazes mais verificacoes???

        User seller = findUser(message.getSellerID());
        PublicKey sellerKey = seller.getPublicKey();

        try {
            if(!isSignatureValid(message, sellerKey))
                return new Message("Authentication failed.");
        } catch (CryptoException e) {
            e.printStackTrace();
        }

        return transferGood(message.getSellerID(), message.getGoodID());


    }

    public Message process(Message message) {
        switch (message.getOperation()) {
            case BUY_GOOD:
                System.out.println("Received buygood");
                receiveBuyGood(message);
                return message;
            default:
                System.out.println("Operation Unknown!");
        }
        return null;
    }
}
