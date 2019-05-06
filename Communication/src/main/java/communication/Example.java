package communication;

import communication.data.Message;
import communication.interfaces.IMessageProcess;

import java.io.IOException;

public class Example implements IMessageProcess {
    private int counter = 0;

    public Message process(Message message) {
        System.out.println("Processing message " + message.getOperation());
        Message msg = new Message();
       // msg.setOperation("this is the response " + counter++);
        return msg;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Example exampleClass = new Example();

        RequestsReceiver reqRec = new RequestsReceiver();
        reqRec.initializeInNewThread(6666, exampleClass);

        Message message = new Message();
        message.setOperation(Message.Operation.GET_STATE_OF_GOOD);
        //message.setOperation("Nice try!");

        Message response = Communication.sendMessage("localhost", 6666, message);
        System.out.println("response was: " + response.getOperation());

        response = Communication.sendMessage("localhost", 6666, message);
        System.out.println("response was: " + response.getOperation());

        response = Communication.sendMessage("localhost", 6666, message);
        System.out.println("response was: " + response.getOperation());

    }
}
