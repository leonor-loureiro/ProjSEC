package communication;

import communication.data.ProcessInfo;
import communication.interfaces.IMessageProcess;

import java.io.IOException;
import java.util.List;


/**
 * Receives requests from clients
 */
public class RequestsReceiver{

    private Communication server;
    private boolean running;

    /**
     * Simple constructor that sets values
     * To start the requests reception, go to {@link #initialize(int, IMessageProcess)}method
     */
    public RequestsReceiver(){
        server = new Communication();
        running = false;
    }

    /**
     * initializes the reception of requests, using the given processor to process messages
     * @param port the port to receive requests on
     * @param processor the class containing the function process that will process the messages
     * @throws IOException if there is a problem with the channels receiving messages
     */
    public void initialize(int port, IMessageProcess processor) throws IOException {
        running = true;

        server.start(port);
        while (running){
            server.listenAndProcess(processor);
        }
    }

    /**
     * initializes the reception of requests but with echo-ing system.
     * @param processMessage processes the messages
     * @param servers list of servers
     * @param faults number of faults allowed
     * @param serverInfo info for the server running
     */
    public void initializeWithEchos(IMessageProcess processMessage, List<ProcessInfo> servers, int faults, ProcessInfo serverInfo) throws IOException {
        running = true;

        server.start(serverInfo.getPort());
        while (running){
            server.listenAndProcessWithEcho(processMessage, servers, faults, serverInfo);
        }
    }


    /**
     * runs the function {@link #initialize(int, IMessageProcess)}  in a new thread
     * @param port the port to run initialize the request receiver on
     * @param processor the function which processes the messages
     */
    public void initializeInNewThread(int port,IMessageProcess processor){
        Runnable r = new RequestsReceiverRunnable(this, port, processor);
        new Thread(r).start();
    }


    public void initializeInNewThreadWithEcho(IMessageProcess processMessage, List<ProcessInfo> servers, int faults, ProcessInfo serverInfo){
        Runnable r = new RequestsReceiverRunnable(this, processMessage, servers, faults, serverInfo);
        new Thread(r).start();
    }

    /**
     * stops the reception of messages
     */
    public void stop(){
        System.out.println("Turning down requests reception...");

        // Allow server start before starting shutdown
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) { e.printStackTrace(); }

        // break message reception loop
        running = false;

        try {
            server.stop();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean isRunning() {
        return running;
    }
}
