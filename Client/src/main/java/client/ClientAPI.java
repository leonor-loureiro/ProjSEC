package client;

import communication.*;

import java.io.IOException;

public class ClientAPI implements IMessageProcess{
    private RequestsReceiver requestReceiver;

    public ClientAPI(int port){

        requestReceiver = new RequestsReceiver();
        try {
            requestReceiver.initialize(port, this);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public Message process(Message message) {

        switch (message.getOperation()){
            case "buygood":

                return buyGood(message);

            default:
                System.out.println("Unknown Operation");
        }

        return null;
    }


    public Message buyGood(Message message){
        // FIXME: process msg
        Message response = message;

        return response;
    }
}
