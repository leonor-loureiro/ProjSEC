package communication;

import communication.data.ProcessInfo;
import communication.interfaces.IMessageProcess;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Class that runs the request receiver
 * Allows the launch of the receiver on a new thread
 */
public class RequestsReceiverRunnable implements Runnable {

    private boolean byzantine;
    private int faults;
    private RequestsReceiver receiver;
    private int port;
    private IMessageProcess processor;
    private boolean echo;
    private  List<ProcessInfo> servers;
    private ProcessInfo serverInfo;

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
        this.echo = false;
    }

    public RequestsReceiverRunnable(RequestsReceiver requestsReceiver, IMessageProcess processMessage, List<ProcessInfo> servers, int faults, ProcessInfo serverInfo) {

        this.receiver = requestsReceiver;
        this.processor = processMessage;
        this.servers = servers;
        this.serverInfo = serverInfo;
        this.echo = true;
        this.faults = faults;
    }

    /**
     * initializes the server request reception in a new thread
     * each message is processed with the object attributed to processor attribute
     */
    public void run() {
        try {
            if(echo){
                receiver.initializeWithEchos(processor, servers, faults, serverInfo);
                System.out.println("Starting request receiver with echo mode on...");
            }
            else{
                receiver.initialize(port, processor);
                System.out.println("Starting request receiver with echo mode off...");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
