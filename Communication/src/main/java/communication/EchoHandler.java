package communication;

import communication.data.Message;
import javafx.util.Pair;

import java.util.*;

public class EchoHandler {

    private static Map<String, Integer> echoCounter = new HashMap<>();
    private static Map<String, Integer> readyCounter = new HashMap<>();

    private static Map<String, Integer> errorEchoCounter = new HashMap<>();
    private static Map<String, Integer> errorReadyCounter = new HashMap<>();

    private static Map<String, List<Message>> echoes = new HashMap<>();
    private static Map<String, List<Message>> readys = new HashMap<>();

    private static Set<String> sentEcho = new HashSet<>();
    private static Set<String> sentReady = new HashSet<>();
    private static Set<String> msgDelivered = new HashSet<>();
    private static Set<String> readyToDeliver = new HashSet<>();

    private static Set<String> receivedOriginalRequest = new HashSet<>();

    /*
     * TODO: Implement sort of a total order that doesn't allow conflicting messages to be ECHOED
     * it's different from total order because there's no need to guarantee liveness
     * this can be achieved by locking or blocking a certain good as a punishment of byzantine client
     */


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


    /**
     * Add message to the list of echoes
     * @param id id of the message group
     * @param msg message to be added
     */
    public static synchronized void addEchoMessage(String id, Message msg){

        List<Message> list = echoes.get(id);

        if(list != null) {
            list.add(msg);
            echoCounter.put(id, echoCounter.get(id) + 1);
        }else{
            list = new ArrayList<Message>();
            list.add(msg);
            echoes.put(id, list);
            echoCounter.put(id, 1);
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
            readyCounter.put(id, readyCounter.get(id) + 1);
        }else{
            list = new ArrayList<Message>();
            list.add(msg);
            readys.put(id, list);
            readyCounter.put(id, 1);
        }
    }

    /**
     * increments the error counter for the id group of echoes
     * @param id id echoes
     */
    public synchronized static void incrementErrorCounter(String id){
        if(errorEchoCounter.containsKey(id))
            errorEchoCounter.put(id, echoCounter.get(id) + 1);
        else
            echoCounter.put(id, 1);
    }

    /**
     * find the number of msgs received so far
     * @param id of the message group
     * @return the number of echoes received
     */
    public static synchronized int  getCounter(String id){
        if(echoCounter.get(id) != null)
            return echoCounter.get(id);
        return 0;
    }

    /**
     * find the number of error in a message
     * @param id of the message group
     * @return the number of errors
     */
    public static synchronized int  getErrorCounter(String id){
        if(errorEchoCounter.get(id) != null)
            return errorEchoCounter.get(id);
        return 0;
    }

    /**
     * clears given entrance on the maps
     * @param id identifier of the message group
     */
    public static synchronized void removeById(String id){
        echoes.remove(id);
        echoCounter.remove(id);
        errorEchoCounter.remove(id);
    }


    public static synchronized Pair<Integer, Message> countMajorityEchoes (String nonce){

        return getCountAndMessage(nonce, echoes);
    }

    public static synchronized Pair<Integer, Message> countMajorityReadys (String nonce){

        return getCountAndMessage(nonce, readys);
    }

    private static Pair<Integer, Message> getCountAndMessage(String nonce, Map<String, List<Message>> msgMap) {
        try {
            List<Message> list = msgMap.get(nonce);

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
            e.printStackTrace();
            return new Pair<>(0, null);

        }
    }

}
