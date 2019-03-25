package communication;

import java.io.IOException;


/**
 * Receives requests from clients
 */
public class RequestsReceiver{

    private Communication server;
    private boolean running;
    private Thread thread = null;

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

            Communication server = new Communication();
            server.start(port);
        while (running){
            server.listenAndProcess(processor);
        }
    }

    /**
     *
     * @param port
     * @param processor
     */
    public void initializeInNewThread(int port,IMessageProcess processor){
        Runnable r = new RequestsReceiverRunnable(this, port, processor);
        new Thread(r).start();
    }

    /**
     * stops the reception of messages
     */
    public void stop(){
        running = false;
    }
}
