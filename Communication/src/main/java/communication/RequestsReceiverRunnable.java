package communication;

import java.io.IOException;


/**
 * Class that runs the request receiver
 * Allows the launch of the receiver on a new thread
 */
public class RequestsReceiverRunnable implements Runnable {

    RequestsReceiver receiver;
    int port;
    IMessageProcess processor;

    RequestsReceiverRunnable(RequestsReceiver receiver, int port, IMessageProcess processor){
        this.receiver = receiver;
        this.port = port;
        this.processor = processor;
    }

    public void run() {
        try {
            receiver.initialize(port, processor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
