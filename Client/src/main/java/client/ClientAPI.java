package client;

import communication.*;

public class ClientAPI implements IMessageProcess{


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
