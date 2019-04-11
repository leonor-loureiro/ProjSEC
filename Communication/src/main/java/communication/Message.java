package communication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

public class Message implements Serializable {

    public enum Operation {
        INTENTION_TO_SELL,
        BUY_GOOD,
        GET_STATE_OF_GOOD,
        TRANSFER_GOOD,
        LIST_GOODS,
        ERROR
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
                if (!field.getName().equals("signature") && obj != null) {
                    System.out.println(field.getName() + " = " + obj);
                    oos.writeObject(obj);
                    oos.flush();
                }
            }

            bytesToSign = bos.toByteArray();
            oos.close();
            bos.close();

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytesToSign;
    }
}