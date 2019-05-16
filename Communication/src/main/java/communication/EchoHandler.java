package communication;

import communication.data.Message;
import communication.exception.MultipleWritesException;
import javafx.util.Pair;

import java.util.*;

public class EchoHandler {

    private static Map<String, Integer> errorEchoCounter = new HashMap<>();

    private static Map<String, List<Message>> echoes = new HashMap<>();
    private static Map<String, List<Message>> readys = new HashMap<>();

    private static Set<String> sentEcho = new HashSet<>();
    private static Set<String> sentReady = new HashSet<>();
    private static Set<String> msgDelivered = new HashSet<>();
    private static Set<String> readyToDeliver = new HashSet<>();

    private static Set<String> receivedOriginalRequest = new HashSet<>();

    private static Map <String, String> writes = new HashMap<>();

    public static synchronized void markEchoSent(String id){
        sentEcho.add(id);
    }

    public static synchronized boolean wasEchoSent(String id){
        return sentEcho.contains(id);
    }


    public static synchronized void markReadySent(String id){
        sentReady.add(id);
    }

    public static synchronized boolean wasReadySent(String id){
        return sentReady.contains(id);
    }


    public static synchronized void markDelivered(String id){
        msgDelivered.add(id);
    }

    public static synchronized boolean wasDelivered(String id){
        return msgDelivered.contains(id);
    }

    public static synchronized void markReadyToDeliver(String id){
        readyToDeliver.add(id);
    }

    public static synchronized boolean isReadyToDeliver(String id){
        return readyToDeliver.contains(id);
    }

    public static synchronized void markReceivedOriginalRequest(String id){
        receivedOriginalRequest.add(id);
    }

    public static synchronized boolean wasReceivedOriginalRequest(String id){
        return receivedOriginalRequest.contains(id);
    }


    public static boolean isWriterOperation(Message msg){
        return !(
                msg.getOperation().equals(Message.Operation.TRANSFER_GOOD) ||
                msg.getOperation().equals(Message.Operation.INTENTION_TO_SELL)
        );
    }


    public static void checkMultiWrites(Message msg) throws MultipleWritesException {
        // Prevents byzantine user from sending 2 concurrent writes to mess with execution order
        if(isWriterOperation(msg)){
            String msgID = writes.get(msg.getGoodID());

            if(msgID == null){
                writes.put(msg.getGoodID(), msg.getNonce());
            }else{
                if(!wasDelivered(msgID) && !msg.getNonce().equals(msgID)){
                    System.out.println("Byzantine client detected, attempt to multiWrite!");
                    throw new MultipleWritesException();
                }
            }
        }
    }

    /**
     * Add message to the list of echoes
     * @param id id of the message group
     * @param msg message to be added
     */
    public static synchronized void addEchoMessage(String id, Message msg) throws MultipleWritesException {
        List<Message> list = echoes.get(id);

        checkMultiWrites(msg);

        if(list != null) {
            list.add(msg);
        }else{
            list = new ArrayList<Message>();
            list.add(msg);
            echoes.put(id, list);
        }
    }

    /**
     * Add message to the list of readys
     * @param id id of the message group
     * @param msg message to be added
     */
    public static synchronized void addReadyMessage(String id, Message msg){

        List<Message> list = readys.get(id);

        if(list != null) {
            list.add(msg);
        }else{
            list = new ArrayList<Message>();
            list.add(msg);
            readys.put(id, list);
        }
    }

    /**
     * increments the error counter for the id group of echoes
     * @param id id echoes
     */
    public synchronized static void incrementErrorCounter(String id){
        if(errorEchoCounter.containsKey(id))
            errorEchoCounter.put(id, errorEchoCounter.get(id) + 1);
        else
            errorEchoCounter.put(id, 1);
    }


    /**
     * Finds the most occurred Ready Message with given nonce
     * @param nonce message's unique identifier
     * @return the occurrence and max occurred echo message
     */
    public static synchronized Pair<Integer, Message> countMajorityEchoes (String nonce){

        return getCountAndMessage(echoes.get(nonce));
    }

    /**
     * Finds the most occurred Ready Message with given nonce
     * @param nonce message's unique identifier
     * @return the occurrence and max occurred ready message
     */
    public static synchronized Pair<Integer, Message> countMajorityReadys (String nonce){

        return getCountAndMessage(readys.get(nonce));
    }

    /**
     * Finds the message with highest count in the given list and the number of this message's occurence
     * @param list list of messages
     * @return the occurrence and the max occurred message
     */
    private static Pair<Integer, Message> getCountAndMessage(List<Message> list) {
        try {

            if (list == null) {
                System.out.println("LIST was null");
                return new Pair<>(0, null);
            }

            int counter;
            int previousCounter = 0;

            Message highestMessage = null;


            for (Message msg : list) {
                counter = 0;
                for (Message msg2 : list) {
                    if (msg.equals(msg2)) {
                        counter++;
                        if (counter >= previousCounter) {
                            previousCounter = counter;
                            highestMessage = msg2;
                        }
                    }
                }
            }

            return new Pair<>(previousCounter, highestMessage);
        }catch (Exception e){
            //TODO: stop catching exceptions
            e.printStackTrace();
            return new Pair<>(0, null);

        }
    }

}
