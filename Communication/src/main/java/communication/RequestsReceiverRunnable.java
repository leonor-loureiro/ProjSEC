package communication;

import java.io.IOException;


/**
 * Class that runs the request receiver
 * Allows the launch of the receiver on a new thread
 */
public class RequestsReceiverRunnable implements Runnable {

    private RequestsReceiver receiver;
    private int port;
    private IMessageProcess processor;

    /**
     * Attributes that are required for request reception
     * @param receiver the request receiver class
     * @param port port in which the requests will be received
     * @param processor the processor that will process the messages
     */
    RequestsReceiverRunnable(RequestsReceiver receiver, int port, IMessageProcess processor){
        this.receiver = receiver;
        this.port = port;
        this.processor = processor;
    }

    /**
     * initializes the server request reception in a new thread
     * each message is processed with the object attributed to processor attribute
     */
    public void run() {
        try {
            receiver.initialize(port, processor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
