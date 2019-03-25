package communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Example implements IMessageProcess {


    public Message process(Message message) {
        System.out.println("Processing message " + message.getOperation());
        Message msg = new Message();
        msg.setOperation("this is the response");
        return msg;
    }

    public static void main(String[] args) {
        final Example exampleClass = new Example();

        Thread serverThread = new Thread(){
            public void run(){

                Communication server = new Communication();
                try {
                    server.start(6666);
                    server.listenAndProcess(exampleClass);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        serverThread.start();

        Message message = new Message();
        message.setOperation("Nice try!");



        try {

            Message response = new Communication().sendMessage("localhost", 6666, message);
            System.out.println("response was: " + response.getOperation());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
