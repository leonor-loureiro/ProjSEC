package communication;

import communication.data.Message;
import communication.data.ProcessInfo;
import communication.exception.AuthenticationException;
import communication.exception.NotFreshException;
import communication.exception.SaveNonceException;
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


    public ProcessMessageWithEchoRunnable(Socket clientSocket, IMessageProcess processMessage, List<ProcessInfo> servers, int faults, ProcessInfo serverInfo) {
        this.clientSocket = clientSocket;
        this.processMessage = processMessage;
        this.servers = servers;
        this.executor = Executors.newFixedThreadPool(servers.size());
        this.quorum = (int) Math.ceil(((double)servers.size() + faults) / 2);
        this.senderInfo = serverInfo;
    }

    /**
     * @param msg the message to check if it's an echo
     * @return true if the message was an ECHO message
     */
    public boolean isEcho(Message msg){

        if(msg.getOperation().equals(Message.Operation.ECHO))
            return true;
//        for(ProcessInfo server : servers){
//            if(server.getID().equals(msg.getSender()))
//                return true;
//        }

        return false;
    }

    @Override
    public void run() {
        ObjectOutputStream out = null;

        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            Message request;

            // Parse received message
            request = (Message) in.readObject();


            if (request != null){
                Message response = null;

                //Don't process echo messages
                if (isEcho(request)) {
                    System.out.println("ECHO RECEIVED");
                    //System.out.println("Received echo from " + request.getSender()+ " with nonce " + request.getNonce());
                    EchoHandler.addMessage(request.getIntentionToBuy().getNonce(), request.getIntentionToBuy());

                    out.writeObject(null);

                } else { //non echo message

                    broadcast(request);

                    if(validEchos(request.getNonce())){
                        System.out.println("Got enough echos!!");
                        response = processMessage.process(request);

                        // write response
                        out.writeObject(response);
                    }
                    else
                        System.out.println("Too many error receives in echo");

                }
            }

            System.out.println("Processed request, closing connection");
            clientSocket.close();

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }


    }

    private boolean validEchos(String nonce) {
        while(true){
            try {

                if (EchoHandler.getErrorCounter(nonce) >= quorum - 1)
                    return false;

                if (EchoHandler.getCounter(nonce) >= quorum - 1)
                    return true;

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
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

                    } catch (IOException | ClassNotFoundException e) {
                        EchoHandler.incrementErrorCounter(msg.getNonce());
                        System.out.println("Failed to send message to " + host + ":" + port);

                    } catch (CryptoException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            });
        }
    }
}
