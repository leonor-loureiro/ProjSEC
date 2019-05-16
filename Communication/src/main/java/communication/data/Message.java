package communication.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.err;

public class Message implements Serializable, Comparable {

    private static final Random random = new Random();

    public int getProofOfWork() {
        return proofOfWork;
    }

    public void setProofOfWork(int proofOfWork) {
        this.proofOfWork = proofOfWork;
    }

    public PublicKey getTempPubKey() {
        return tempPubKey;
    }

    public void setTempPubKey(PublicKey tempPubKey) {
        this.tempPubKey = tempPubKey;
    }

    public String getTempKeySignature() {
        return tempKeySignature;
    }

    public void setTempKeySignature(String tempKeySignature) {
        this.tempKeySignature = tempKeySignature;
    }

    public enum Operation {
        INTENTION_TO_SELL,
        BUY_GOOD,
        GET_STATE_OF_GOOD,
        TRANSFER_GOOD,
        ERROR,
        WRITE_BACK,
        ECHO,
        READY
    }

    private Operation operation;
    private String errorMessage;
    private String goodID;
    private String sellerID;
    private boolean isForSale;
    private String buyerID;
    private String nonce;
    private String signature;
    private Message intentionToBuy;
    private String sender;
    private String receiver;
    private int proofOfWork;

    //Byzantine Registers Variables
    private int wts;
    private int rid;
    private String valSignature;
    private String writer;


    private PublicKey tempPubKey;
    private String tempKeySignature;

    public Message() {

        this.setNonce(" "+random.nextInt());
    }

    public Message(Operation operation){
        this.operation = operation;

        this.setNonce(operation.name() + random.nextInt());
    }

    public Message(String errorMessage) {
        this.errorMessage = errorMessage;
        this.operation = Operation.ERROR;

        this.setNonce("err" + random.nextInt());
    }

    public Message(String errorMessage, String sellerID, String buyerID) {
        this(errorMessage);
        setBuyerID(buyerID);
        setSellerID(sellerID);
        this.setNonce(sellerID + buyerID+ random.nextInt());

    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isForSale() {
        return isForSale;
    }

    public void setForSale(boolean forSale) {
        isForSale = forSale;
    }

    public Message getIntentionToBuy() {
        return intentionToBuy;
    }

    public void setIntentionToBuy(Message intentionToBuy) {
        this.intentionToBuy = intentionToBuy;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getGoodID() {
        return goodID;
    }

    public void setGoodID(String goodID) {
        this.goodID = goodID;
    }

    public String getBuyerID() {
        return buyerID;
    }

    public void setBuyerID(String buyerID) {
        this.buyerID = buyerID;
    }

    public String getSellerID() {
        return sellerID;
    }

    public void setSellerID(String sellerID) {
        this.sellerID = sellerID;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public int getWts() {
        return wts;
    }

    public void setWts(int wts) {
        this.wts = wts;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public String getValSignature() {
        return valSignature;
    }

    public void setValSignature(String valSignature) {
        this.valSignature = valSignature;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getDataToChallenge(){
        return goodID + "|" + buyerID + "|" + sellerID + "|" + wts + "|" + nonce;
    }


    /**
     * Responsible for adding a nonce and a timestamp to the message
     */

    public void addFreshness(String ID) {
        //this.setTimestamp(currentTimeMillis());
        //this.setNonce(ID + random.nextInt());
    }


    public byte[] getBytesToSign(){
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;
        byte[] bytesToSign = null; //stores the bytes of the non-null attributes
        String str = "";

        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);

            //Iterate to all fields of the message
            Field[] fields = Message.class.getDeclaredFields();
            for (Field field : fields) {
                Object obj = field.get(this);
                if (!field.getName().equals("signature") && !field.getName().equals("random") && obj != null) {
                    //System.out.println(field.getName() + " = " + obj);
                    oos.writeObject(obj);
                    oos.flush();
                }
            }

            bytesToSign = bos.toByteArray();
            oos.close();
            bos.close();

        } catch (IllegalAccessException | IOException e) {
            e.printStackTrace();
        }

        return bytesToSign;
    }

    public void print(){
        System.out.println("START ############################################################");
      try {

        //Iterate to all fields of the message
        Field[] fields = Message.class.getDeclaredFields();
        for (Field field : fields) {
            Object obj = field.get(this);
            if (!field.getName().equals("random") && obj != null) {
                System.out.println(field.getName() + " = " + obj);
            }
        }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        System.out.println("############################################################  END");

    }


    public String getChallenge(){
        return getBuyerID() + getSellerID() + getGoodID() + getNonce() + getWriter() + getReceiver();
    }

    public int compareTo(Object o) {
        if(!(o instanceof Message))
            return -1;

        Message msg = (Message) o;
        return new Integer(wts).compareTo(msg.getWts());
    }


    private boolean equalsStrings(String one, String two){
        if(one == null || two == null)
            return one == two; // equals if both are null

        return one.equals(two);
    }

    @Override
    public boolean equals(Object o){

        // if the reference is the same, then it's the same object
        if(this == o)
            return true;

        // check null and class type
        if(o == null || o.getClass()!= this.getClass())
            return false;

        Message msg = (Message) o;

        if(! (( this.getOperation() == msg.getOperation()) ||
                this.getOperation() != null && msg.getOperation() != null &&
                        this.getOperation().equals(msg.getOperation())) ){
            return false;
        }

        if(!equalsStrings(errorMessage, msg.getErrorMessage()))
            return false;

        if(!equalsStrings(goodID, msg.getGoodID()))
            return false;

        if(!equalsStrings(sellerID, msg.getSellerID()))
            return false;

        if(!equalsStrings(buyerID, msg.getBuyerID()))
            return false;

        if(!equalsStrings(nonce, msg.getNonce()))
            return false;

        // Some fields are different so it's signature may not be equal
        //if(!equalsStrings(signature, msg.getSignature()))
        //    return false;

        if(intentionToBuy != null && !intentionToBuy.equals(msg.intentionToBuy))
            return false;

        if(!equalsStrings(sender, msg.getSender()))
            return false;

//        if(!equalsStrings(receiver, msg.getReceiver()))
//            return false;

        if(isForSale != msg.isForSale())
            return false;

        if(wts != msg.getWts())
            return false;

        if(rid != msg.getRid())
            return false;

        if(!equalsStrings(valSignature, msg.getValSignature()))
           return false;

        if(!equalsStrings(writer, msg.getWriter()))
            return false;

        if(!equalsStrings(tempKeySignature, msg.getTempKeySignature()))
            return false;

        if(tempPubKey != null && msg.getTempPubKey()!= null &&
                tempPubKey != msg.getTempPubKey() &&
                tempPubKey.equals(msg.getTempPubKey()))
            return false;

        return true;
    }

}