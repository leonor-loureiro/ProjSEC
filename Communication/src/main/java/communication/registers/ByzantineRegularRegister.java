package communication.registers;

import commontypes.User;
import commontypes.Utils;
import communication.AuthenticatedPerfectLinks;
import communication.ByzantineSimulator;
import communication.data.Message;
import communication.data.ProcessInfo;
import communication.exception.AuthenticationException;
import communication.exception.NotFreshException;
import communication.exception.SaveNonceException;
import crypto.Crypto;
import crypto.CryptoException;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
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

    //List of writer clients
    private final List<User> writers;


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

    //Failed requests counter
    private int error;




    public ByzantineRegularRegister(String id, List<ProcessInfo> servers, List<User> writers, PrivateKey privateKey, int faults) {

        ID = id;
        this.servers = servers;
        this.writers = writers;
        this.privateKey = privateKey;
        this.quorum = (int) Math.ceil(((double)servers.size() + faults) / 2);
        //Creates a thread pool with one thread per server replica
        this.executor = Executors.newFixedThreadPool(servers.size());
        System.out.println("Quorum = " + quorum + " serverCount: " + servers.size());
        senderInfo = new ProcessInfo(id, privateKey);
    }


    /**
     * Sends a message that requests a write operation
     * @param msg message to send
     * @return server's response message
     */
    public Message write(Message msg, String seller)
            throws CryptoException {

        Message getTsMsg = new Message();
        getTsMsg.setOperation(Message.Operation.GET_STATE_OF_GOOD);
        getTsMsg.setGoodID(msg.getGoodID());
        //Set user ID
        getTsMsg.setBuyerID(ID);

        Message getTsResp = readImpl(getTsMsg);

        if(getTsResp == null){
            System.out.println("Failed to get current timestamp");
            return null;
        }


        //Increment write timestamp
        wts = getTsResp.getWts()+1;

        System.out.println(msg.getOperation() + "  Seller: " + seller);
        return writeImpl(msg, seller, wts);
    }


    Message writeImpl(Message msg, String seller, int wts) throws CryptoException {
        //Update last write timestamp seen
        this.wts = wts;
        msg.setWts(wts);

        //Clear previous responses
        ackList.clear();

        //Sign the new value
        if(msg.getValSignature() == null) {
            String value = getValueToSign(msg.getGoodID(), seller, msg.isForSale(), ID, wts);
            System.out.println(msg.getOperation() + "  -  Writing value: " + value);

            msg.setValSignature(Crypto.sign(value.getBytes(), privateKey));
            msg.setWriter(ID);
        }else{
            System.out.println(msg.getOperation() + "  -  Writing value: " + getValueToSign(msg.getGoodID(), msg.getSellerID(), msg.isForSale(), msg.getWriter(), msg.getWts()));
        }



        //Send the message to all server replicas
        broadcast(msg, WRITE);

        //Wait until a majority servers have responded
        while(ackList.size() < quorum && error < quorum){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(error >= quorum)
            return null;

        return ackList.get(0);
    }

    protected String getValueToSign(String goodID, String userID, boolean isForSale, String writer, int wts) {
        return "WRITE|brr|" + goodID + "|" + userID + "|" + isForSale + "|" + writer + "|" + wts;
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
        while (readList.size() < quorum && error < quorum) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(error >= quorum)
            return null;

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

        System.out.println("Writer = " + msg.getWriter());

        //TODO remove != null after signing initial resources
        if(msg.getOperation().equals(Message.Operation.ERROR) || msg.getWriter() != null) {
            PublicKey writerKey = getWriterPublicKey(msg.getWriter());
            String value = getValueToSign(
                    msg.getGoodID(), msg.getSellerID(), msg.isForSale(), msg.getWriter(), msg.getWts()
            );

            try {
                if(!Crypto.verifySignature(msg.getValSignature(), value.getBytes(), writerKey)) {
                    System.out.println("Value = " + value);
                    System.out.println("Value read is invalid: " + msg.getSender());
                    System.out.println(msg.getValSignature());
                    error++;
                    return;
                }
            } catch (CryptoException e) {
                error++;
            }
        }

        readList.add(msg);
    }

    private void broadcast( Message msg1, final int type) throws CryptoException {
        error = 0;
        int i = 0;
        for(final ProcessInfo serverInfo : servers) {
            //byzantine testing

            if(ByzantineSimulator.getByzantine() && ++i == servers.size()){
                msg1 = (Message) Utils.deepCopy(msg1);
                msg1.setGoodID("byzantineID");

            }
            final Message msg = msg1;
            final String host = serverInfo.getHost();
            final int port = serverInfo.getPort();

            executor.submit(new Callable<Void>() {
                @Override
                public Void call(){

                    Message response = null;
                    try {
                        System.out.println(serverInfo.getID() + " » " + msg.getGoodID());
                        response = AuthenticatedPerfectLinks.sendMessage(senderInfo, serverInfo, msg);

                        if(type == WRITE)
                            handleWriteResponse(response);
                        else if(type == READ)
                            handleReadResponse(response);


                    } catch (IOException | ClassNotFoundException e) {
                        error++;
                        System.out.println("Failed to send message to " + host + ":" + port);
                    } catch (NotFreshException e) {
                        error++;
                        System.out.println("Response is not fresh: " + e.getMessage());
                    } catch (AuthenticationException e) {
                        error++;
                        System.out.println("Authentication failed: " + e.getMessage());
                    } catch (SaveNonceException | CryptoException e) {
                        error++;
                        e.printStackTrace();
                     }

                    return null;
                }
            });


        }
    }

    public PublicKey getWriterPublicKey(String writerID){
        for(User u : writers)
            if(u.getUserID().equals(writerID))
                return u.getPublicKey();
        return null;
    }


}
