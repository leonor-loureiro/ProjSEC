package communication;

import crypto.Crypto;
import crypto.CryptoException;
import javafx.util.Pair;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ByzantineRegularRegister {

    public static final int WRITE = 100;
    public static final int READ = 200;

    protected final String ID;


    //List of server replicas
    private final List<ServerInfo> servers;

    //Private key of the client
    public final PrivateKey privateKey;

    //Handler for the communication between processes
    private final Communication communicationHandler;

    //Handles the execution of async tasks
    private final ExecutorService executor;

    //Quorum
    public final int quorum;

    //Read timestamp
    public int rid = 0;

    //Write timestamp
    private int wts = 0;

    //Stores the write responses
    public List<Message> ackList = new ArrayList<Message>();

    //Stores the read responses
    public List<Message> readList = new ArrayList<Message>();



    public ByzantineRegularRegister(String id, List<ServerInfo> servers, PrivateKey privateKey,
                                    Communication communicationHandler, int faults) {
        ID = id;
        this.servers = servers;
        this.privateKey = privateKey;
        this.communicationHandler = communicationHandler;
        this.quorum = (int) Math.ceil(((double)servers.size() + faults) / 2);
        //Creates a thread pool with one thread for server replica
        this.executor = Executors.newFixedThreadPool(servers.size());
        System.out.println("Quorum = " + quorum + " serverCount: " + servers.size());
    }


    /**
     * Sends a message that requests a write operation
     * @param msg message to send
     * @return server's response message
     */
    public Message write(Message msg, String goodID, String userID, boolean isForSale)
            throws CryptoException {

        Message getTsMsg = new Message();
        getTsMsg.setOperation(Message.Operation.GET_STATE_OF_GOOD);
        getTsMsg.setGoodID(goodID);
        //Set user ID
        getTsMsg.setBuyerID(ID);
        getTsMsg.addFreshness(ID);

        Message getTsResp = readImpl(getTsMsg);
        //TODO validate Notary signature (Is necessary?)

        //Increment write timestamp
        wts = getTsResp.getWts()+1;

        return writeImpl(msg, goodID, userID, isForSale, wts);
    }


    Message writeImpl(Message msg, String goodID, String userID, boolean isForSale, int wts) throws CryptoException {
        System.out.println("Send " + msg.getOperation() + " wts=" +wts);
        //Update last write timestamp seen
        this.wts = wts;
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

    public Message read(Message msg) throws CryptoException {
        return readImpl(msg);
    }


    /**
     * Sends a message that requests a read operation
     * @param msg message to send
     * @return server's response
     */
    private Message readImpl(Message msg) throws CryptoException {
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

    public void broadcast(final Message msg, final int type) throws CryptoException {
        msg.setSignature(Crypto.sign(msg.getBytesToSign(), privateKey));

        for(ServerInfo server : servers) {
            final String host = server.getAddress();
            final int port = server.getPort();

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
