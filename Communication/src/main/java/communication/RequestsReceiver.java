package communication;

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
        server.start(port);
        server.listenAndProcess(processor);
    }

}
