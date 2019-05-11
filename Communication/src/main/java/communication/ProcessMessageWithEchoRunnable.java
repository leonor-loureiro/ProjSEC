package communication;

import communication.data.Message;
import communication.data.ProcessInfo;
import communication.interfaces.IMessageProcess;
import crypto.CryptoException;

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

                } else if (isReadyRequest(request)){
                    System.out.println("READY received");

                    //TODO: if > faults readys received, but it hasn't broadcast yet, then broadcast the ready



1
                }else{ //non echo/ready message

                    if(!EchoHandler.wasEchoSent(request.getNonce()))
                        broadcast(request);

                    if(EchoHandler.enoughEchoes(request.getNonce(), quorum)){
                        System.out.println("Got enough echoes!!");

                        //Process message
                        response = processMessage.process(request);

                        // write response
                    }
                    else {
                        System.out.println("Too many error receives in echo");
                    }

                }

                out.writeObject(response);
            }

            System.out.println("Processed request, closing connection");
            clientSocket.close();

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }


    }


    private void broadcast(final Message msg){

        final Message wrapper = new Message(Message.Operation.ECHO);

        wrapper.setIntentionToBuy(msg);
        for(final ProcessInfo serverInfo : servers) {
            final String host = serverInfo.getHost();
            final int port = serverInfo.getPort();


            if(serverInfo.getID().equals(senderInfo.getID()))
                continue;

            executor.submit(new Callable<Void>() {
                @Override
                public Void call(){
                    try {

                        AuthenticatedPerfectLinks.sendOneWayMessage(senderInfo, serverInfo, wrapper);

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
