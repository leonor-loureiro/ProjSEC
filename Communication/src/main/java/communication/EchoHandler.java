package communication;

import communication.data.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EchoHandler {

    public static Map<String, Integer> echoCounter = new HashMap<>();
    public static Map<String, List<Message>> messages = new HashMap<>();
    public static Map<String, Integer> errorCounter = new HashMap<>();

    /**
     * Add message to the list of messages
     * @param id id of the message group
     * @param msg message to be added
     */
    public static synchronized void addMessage(String id, Message msg){

        List<Message> list = messages.get(id);

        if(list != null) {
            list.add(msg);
            echoCounter.put(id, echoCounter.get(id) + 1);
        }else{
            list = new ArrayList<Message>();
            list.add(msg);
            messages.put(id, list);
            echoCounter.put(id, 1);
        }
    }

    /**
     * increments the error counter for the id group of messages
     * @param id id messages
     */
    public synchronized static void incrementErrorCounter(String id){
        if(errorCounter.containsKey(id))
            errorCounter.put(id, echoCounter.get(id) + 1);
        else
            echoCounter.put(id, 1);
    }

    /**
     * find the number of msgs received so far
     * @param id of the message group
     * @return the number of messages received
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
        if(errorCounter.get(id) != null)
            return errorCounter.get(id);
        return 0;
    }

    /**
     * clears given entrance on the maps
     * @param id identifier of the message group
     */
    public static synchronized void removeById(String id){
        messages.remove(id);
        echoCounter.remove(id);
        errorCounter.remove(id);
    }


}
