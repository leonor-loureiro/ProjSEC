package communication.registers;

import communication.AuthenticatedPerfectLinks;
import communication.data.Message;
import communication.data.ProcessInfo;
import communication.exception.AuthenticationException;
import communication.exception.NotFreshException;
import communication.exception.SaveNonceException;
import crypto.Crypto;
import crypto.CryptoException;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ByzantineRegularRegister {

    private static final int WRITE = 100;
    private static final int READ = 200;

    private final ProcessInfo senderInfo;
    protected final String ID;

    //List of server replicas
    private final List<ProcessInfo> servers;

    //Private key of the client
    private final PrivateKey privateKey;

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




    public ByzantineRegularRegister(String id, List<ProcessInfo> servers, PrivateKey privateKey, int faults) {

        ID = id;
        this.servers = servers;
        this.privateKey = privateKey;
        this.quorum = (int) Math.ceil(((double)servers.size() + faults) / 2);
        //Creates a thread pool with one thread for server replica
        this.executor = Executors.newFixedThreadPool(servers.size());
        System.out.println("Quorum = " + quorum + " serverCount: " + servers.size());
        senderInfo = new ProcessInfo(id, privateKey);
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

        Message getTsResp = readImpl(getTsMsg);

        //Increment write timestamp
        wts = getTsResp.getWts()+1;

        return writeImpl(msg, goodID, userID, isForSale, wts);
    }


    Message writeImpl(Message msg, String goodID, String userID, boolean isForSale, int wts) throws CryptoException {
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

        return Collections.max(readList);
    }

    /**
     * Handles the response to a write request
     * Adds the message received to the list of acks
     * @param msg write response
     */
    private synchronized void handleWriteResponse(Message msg){
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
        msg.getBytesToSign();
        if(rid != msg.getRid())
            return;

        //TODO: verify value signature
        readList.add(msg);
    }

    private void broadcast(final Message msg, final int type) throws CryptoException {


        for(final ProcessInfo serverInfo : servers) {
            final String host = serverInfo.getHost();
            final int port = serverInfo.getPort();

            executor.submit(new Callable<Void>() {
                @Override
                public Void call(){

                    Message response = null;
                    try {
                        //authenticator.authenticate(sent);

                        response = AuthenticatedPerfectLinks.sendMessage(senderInfo, serverInfo, msg);


                       /* if(!authenticator.isValid(response))
                            return null;*/

                        if(type == WRITE)
                            handleWriteResponse(response);
                        else if(type == READ)
                            handleReadResponse(response);


                    //TODO: Figure out what to do in these cases
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("Failed to send message to " + host + ":" + port);
                    } catch (NotFreshException e) {
                        System.out.println("Response is not fresh");
                    } catch (AuthenticationException e) {
                        System.out.println("Authentication failed");
                    } catch (SaveNonceException | CryptoException e) {
                        e.printStackTrace();
                     }
                    return null;
                }
            });


        }
    }
}
