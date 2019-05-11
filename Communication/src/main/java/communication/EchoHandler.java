package communication;

import communication.data.Message;

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

    /*
     * TODO: Implement sort of a total order that doesn't allow conflicting messages to be ECHOED
     * it's different from total order because there's no need to guarantee liveness
     * this can be achieved by locking or blocking a certain good as a punishment of byzantine client
     */


    public static synchronized void markEchoSent(String id){
        sentEcho.add(id);
    }

    public static synchronized void markReadySent(String id){
        sentReady.add(id);
    }

    public static synchronized boolean wasEchoSent(String id){
        return echoes.containsKey(id);
    }

    public static synchronized boolean wasReadySent(String id){
        return readys.containsKey(id);
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


    /**
     * Checks if enough valid echoes arrived
     * @param nonce message's unique identifier
     * @return true if enough valid echoes found
     */
    public static boolean enoughEchoes(String nonce, int quorum) {

        // Doesn't send to himself
        if (getErrorCounter(nonce) >= quorum - 1)
            return false;

        // TODO: Quorum of equal messages
        if (getCounter(nonce) >= quorum - 1)
            return true;

        return false;
    }



}
