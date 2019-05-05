package communication;

import crypto.Crypto;
import crypto.CryptoException;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ByzantineRegularRegister {

    private static final int WRITE = 100;
    private static final int READ = 200;

    protected final String ID;


    //List of server replicas
    private final HashMap<String, Integer> servers;

    //Private key of the client
    private final PrivateKey privateKey;

    //Handler for the communication between processes
    private final Communication communicationHandler;

    //Handles the execution of async tasks
    private final ExecutorService executor;

    //Quorum
    private final int quorum;

    //Read timestamp
    private int rid = 0;

    //Write timestamp
    private int wts = 0;

    //Stores the write responses
    private List<Message> ackList = new ArrayList<Message>();

    //Stores the read responses
    private List<Message> readList = new ArrayList<Message>();


    public ByzantineRegularRegister(String id, HashMap<String, Integer> servers, PrivateKey privateKey,
                                    Communication communicationHandler, int faults) {
        ID = id;
        this.servers = servers;
        this.privateKey = privateKey;
        this.communicationHandler = communicationHandler;
        this.quorum = (servers.size() + faults) / 2;
        //Creates a thread pool with one thread for server replica
        this.executor = Executors.newFixedThreadPool(servers.size());
        System.out.println("Quorum = " + quorum);
    }


    /**
     * Sends a message that requests a write operation
     * @param msg message to send
     * @return server's response message
     */
    public Message write(Message msg, String goodID, String userID, boolean isForSale)
            throws CryptoException {

        //Update write timestamp
        wts ++;

        return write(msg, goodID, userID, isForSale, wts);
    }


    protected Message write(Message msg, String goodID, String userID, boolean isForSale, int wts) throws CryptoException {
        msg.setWts(wts);

        //Clear previous responses
        ackList.clear();

        //Sign the new value
        String value = goodID + "|" + userID + "|" + isForSale;
        msg.setValSignature(Crypto.sign(value.getBytes(), privateKey));

        //Send the message to all server replicas
        broadcast(msg, WRITE);

        //Wait until a majority servers have responded
        while(ackList.size() < quorum){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //TODO: find out which response should be returned
        return ackList.get(0);
    }


    /**
     * Sends a message that requests a read operation
     * @param msg message to send
     * @return server's response
     */
    public Message read(Message msg) throws CryptoException {
        //Update read timestamp
        rid ++;
        msg.setRid(rid);

        readList.clear();

        //Send the read request to all servers
        broadcast(msg, READ);

        //Wait until a majority of servers have replied
        while (readList.size() < quorum) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Quorum reads");
        return Collections.max(readList);
    }

    /**
     * Handles the response to a write request
     * Adds the message received to the list of acks
     * @param msg write response
     */
    private synchronized void handleWriteResponse(Message msg){
        System.out.println("Received -WRITE- " + msg.getWts() + "/" + wts);
        msg.getBytesToSign();
        if(wts != msg.getWts())
            return;
        ackList.add(msg);
    }


    /**
     * Handles the response to read request
     * Adds the message to the reads list
     * @param msg message received
     */
    private synchronized void handleReadResponse(Message msg){
        System.out.println("Received -READ- " + msg.getRid() + "/" + rid);
        msg.getBytesToSign();
        if(rid != msg.getRid())
            return;
        //TODO: verify value signature
        readList.add(msg);
    }

    private void broadcast(final Message msg, final int type) throws CryptoException {
        msg.setSignature(Crypto.sign(msg.getBytesToSign(), privateKey));

        for(Map.Entry<String, Integer> entry : servers.entrySet()) {
            final String host = entry.getKey();
            final int port = entry.getValue();

            executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws IOException, ClassNotFoundException {
                    Message response = null;
                    try {
                        response = communicationHandler.sendMessage(host, port, msg);
                        System.out.println("Sent wts=" + msg.getWts() + "/rid=" + msg.getRid());

                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("Failed to send message to " + host + ":" + port);
                        throw e;
                    }
                    if(type == WRITE)
                        handleWriteResponse(response);
                    else if(type == READ)
                        handleReadResponse(response);

                    return null;
                }
            });


        }
    }
}
