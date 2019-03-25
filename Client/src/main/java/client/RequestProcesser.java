package client;

import communication.*;

public class RequestProcesser implements IMessageProcess{


    public Message process(Message message) {

        switch (message.getOperation()){
            case "buygood":
                break;

            default:
                System.out.println("Unknown Operation");
        }

        return null;
    }
}
