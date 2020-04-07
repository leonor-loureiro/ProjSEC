package communication;

import commontypes.Utils;
import communication.data.Message;
import communication.data.ProcessInfo;
import communication.exception.MultipleWritesException;
import communication.interfaces.IMessageProcess;
import crypto.CryptoException;
import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessMessageWithEchoRunnable implements Runnable{


    private final IMessageProcess processMessage;
    private final Socket clientSocket;
    private final List<ProcessInfo> servers;
    private final ExecutorService executor;
    private ProcessInfo senderInfo;
    private final int quorum;
    private final int faults;


    public ProcessMessageWithEchoRunnable(Socket clientSocket, IMessageProcess processMessage,
                                          List<ProcessInfo> servers, int faults, ProcessInfo serverInfo) {
        this.clientSocket = clientSocket;
        this.processMessage = processMessage;
        this.servers = servers;
        this.executor = Executors.newFixedThreadPool(servers.size());
        this.quorum = (int) Math.ceil(((double)servers.size() + faults) / 2);
        this.senderInfo = serverInfo;
        this.faults = faults;
    }





    @Override
    public void run() {
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            // Parse received message
            Message request = (Message) in.readObject();


            if (request != null) {
                Message response = null;

                if (isEcho(request)) {
                    System.out.println("ECHO RECEIVED");
                    //System.out.println("Received echo from " + request.getSender()+ " with nonce " + request.getNonce());
                    EchoHandler.addEchoMessage(request.getIntentionToBuy().getNonce(), request.getIntentionToBuy());

                    // Get number of equal ECHOs received
                    Pair<Integer, Message> info = EchoHandler.countMajorityEchoes(request.getIntentionToBuy().getNonce());
                    System.out.println("Echo count : " + info.getKey());
                    if( info.getKey() >= quorum - 1 && !EchoHandler.wasReadySent(info.getValue().getNonce())){

                        EchoHandler.markReadySent((info.getValue().getNonce()));

                        Message wrapper = new Message(Message.Operation.READY);
                        wrapper.setIntentionToBuy(info.getValue());

                        broadcast(wrapper);
                    }

                } else if (isReadyRequest(request)){
                    System.out.println("READY received");

                    EchoHandler.addReadyMessage(request.getIntentionToBuy().getNonce(), request.getIntentionToBuy());

                    Pair<Integer, Message> info = EchoHandler.countMajorityReadys(request.getIntentionToBuy().getNonce());

                    System.out.println("Ready count : " + info.getKey());

                    // Amplification phase
                    if( info.getKey() > faults && !EchoHandler.wasReadySent(info.getValue().getNonce())){

                        EchoHandler.markReadySent((info.getValue().getNonce()));

                        Message wrapper = new Message(Message.Operation.READY);
                        wrapper.setIntentionToBuy(info.getValue());

                        broadcast(wrapper);
                    }

                    // mark ready to deliver
                    if(info.getValue()!= null && !EchoHandler.isReadyToDeliver(info.getValue().getNonce()) && info.getKey() >= quorum - 1){
                        EchoHandler.markReadyToDeliver((info.getValue().getNonce()));
                    }


                    // in case the original request was not received
                    if(info.getValue() != null && !EchoHandler.wasReceivedOriginalRequest(info.getValue().getNonce()) &&
                            EchoHandler.isReadyToDeliver(info.getValue().getNonce())) {

                        if (!EchoHandler.wasDelivered(info.getValue().getNonce())) {
                            System.out.println("DELIVERING not originally received request");

                            //Process message
                            EchoHandler.markDelivered(info.getValue().getNonce());
                            processMessage.process(info.getValue());
                        }
                    }
                }else{ //non echo/ready message
                    
                    EchoHandler.checkMultiWrites(request);

                    EchoHandler.markReceivedOriginalRequest(request.getNonce());

                    System.out.println(request.getGoodID());
                    // Retransmission Step
                    if(!EchoHandler.wasEchoSent(request.getNonce())){

                        EchoHandler.markEchoSent(request.getNonce());
                        Message wrapper = new Message(Message.Operation.ECHO);
                        wrapper.setIntentionToBuy(request);

                        broadcast(wrapper);
                    }

                    // wait until ready to deliver message
                    while(!EchoHandler.isReadyToDeliver(request.getNonce()))
                        Thread.sleep(10);

                    // Process
                    if(!EchoHandler.wasDelivered(request.getNonce())){
                        System.out.println("DELIVERING");

                        //Process message
                        EchoHandler.markDelivered(request.getNonce());
                        Pair<Integer, Message> info = EchoHandler.countMajorityReadys(request.getNonce());

                        if(ByzantineSimulator.getByzantine()) {
                            System.out.println("Byzantining the message");
                            info.getValue().setGoodID("byzantineID");
                            response = processMessage.process(info.getValue());
                        }
                        else
                            response = processMessage.process(info.getValue());


                    } else {
                        System.out.println("Already Processed Request");
                    }

                }

                out.writeObject(response);
            }

            System.out.println("Processed request, closing connection");
            clientSocket.close();

        } catch (ClassNotFoundException | IOException | InterruptedException | MultipleWritesException e) {
            e.printStackTrace();
        }


    }


    private void broadcast(final Message msg){
        //System.out.println("Sending " + msg1.getOperation().name());

        int i = 0;
        for(final ProcessInfo serverInfo : servers) {

         /*   if(!isEcho(msg1) && !isReadyRequest(msg1)&& ByzantineSimulator.getByzantine()) {
                if (++i == servers.size())
                {
                    System.out.println("Byzanting this message");
                    msg1 = (Message) Utils.deepCopy(msg1);
                    msg1.setGoodID("byzantineID");

                }

            } */
            final String host = serverInfo.getHost();
            final int port = serverInfo.getPort();

            //final Message msg = msg1;


            if(serverInfo.getID().equals(senderInfo.getID()))
                continue;

            executor.submit(new Callable<Void>() {
                @Override
                public Void call(){
                    try {

                        AuthenticatedPerfectLinks.sendOneWayMessage(senderInfo, serverInfo, msg);

                    } catch (IOException | ClassNotFoundException |CryptoException e) {
                        e.printStackTrace();
                        EchoHandler.incrementErrorCounter(msg.getNonce());
                        System.out.println("Failed to send message to " + host + ":" + port);
                    }

                    return null;
                }
            });
        }
    }



    /**
     * @param msg the message to check if it's an echo
     * @return true if the message was an ECHO message
     */
    private boolean isEcho(Message msg){
        return msg != null && msg.getOperation().equals(Message.Operation.ECHO) && validServer(msg);
    }


    /**
     *
     * @param msg the message to check if it's an ready message
     * @return true if the message was a READY message
     */
    private boolean isReadyRequest(Message msg) {
        return msg != null && msg.getOperation().equals(Message.Operation.READY) && validServer(msg);
    }

    private boolean validServer(Message msg){
        for(ProcessInfo server : servers){
            if(server.getID().equals(msg.getSender()))
                return true;
        }
        return false;
    }


}
