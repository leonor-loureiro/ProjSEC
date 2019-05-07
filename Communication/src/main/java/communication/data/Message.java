package communication.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Random;

import static java.lang.System.currentTimeMillis;

public class Message implements Serializable, Comparable {

    private static final Random random = new Random();

    public enum Operation {
        INTENTION_TO_SELL,
        BUY_GOOD,
        GET_STATE_OF_GOOD,
        TRANSFER_GOOD,
        ERROR,
        WRITE_BACK
    }

    private Operation operation;
    private String errorMessage;
    private String goodID;
    private String sellerID;
    private boolean isForSale;
    private String buyerID;
    private long timestamp;
    private String nonce;
    private String signature;
    private Message intentionToBuy;
    private String sender;
    private String receiver;

    //Byzantine Registers Variables
    private int wts;
    private int rid;
    private String valSignature;

    public Message() {
    }

    public Message(Operation operation){
        this.operation = operation;
    }

    public Message(String errorMessage) {
        this.errorMessage = errorMessage;
        this.operation = Operation.ERROR;
    }

    public Message(String errorMessage, String sellerID, String buyerID) {
        this(errorMessage);
        setBuyerID(buyerID);
        setSellerID(sellerID);
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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

    public String getDataToChallenge(){
        return goodID + "|" + buyerID + "|" + sellerID + "|" + wts + "|" + nonce;
    }

    /**
     * Responsible for adding a nonce and a timestamp to the message
     */

    public void addFreshness(String ID) {
        this.setTimestamp(currentTimeMillis());
        this.setNonce(ID + random.nextInt());
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


    public int compareTo(Object o) {
        if(!(o instanceof Message))
            return -1;

        Message msg = (Message) o;
        return new Integer(wts).compareTo(msg.getWts());
    }


}