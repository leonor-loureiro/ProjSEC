package server;

import communication.Communication;
import communication.IMessageProcess;

import java.io.IOException;


/**
 * Receives requests from clients
 */
public class RequestsReceiver {

    private Communication server;

    public RequestsReceiver(){
        server = new Communication();
    }

    public void initialize(int port, IMessageProcess processor) throws IOException {
        Communication server = new Communication();
        try {
            server.start(port);
            server.listenAndProcess(processor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
