package client;

import communication.Communication;

import java.io.IOException;


/**
 * Class that acts as the client's own server
 * Receives requests from other clients
 */
public class RequestsReceiver {

    private Communication server;

    public RequestsReceiver(){
        server = new Communication();
    }

    public void initialize(int port) throws IOException {
        Thread serverThread = new Thread(){
            public void run(){

                Communication server = new Communication();
                try {
                    server.start(port);
                    server.listenAndProcess(new ClientAPI());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

}
