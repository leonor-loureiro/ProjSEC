package communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Example implements IMessageProcess {


    public Message process(Message message) {
        System.out.println("Processing message " + message.getContent());
        Message msg = new Message();
        msg.setContent("this is the response");
        return msg;
    }

    public static void main(String[] args) {
        final Example exampleClass = new Example();

        Thread serverThread = new Thread(){
            public void run(){

                Communication server = new Communication();
                try {
                    server.start(6666);
                    server.listenSocket(exampleClass);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        serverThread.start();

        Message message = new Message();
        message.setContent("Nice try!");



        try {
            Socket kkSocket = new Socket("localhost", 6666);

            ObjectOutputStream out2 = new ObjectOutputStream(kkSocket.getOutputStream());
            ObjectInputStream in2 = new ObjectInputStream(kkSocket.getInputStream());
            out2.writeObject(message);

            Message response = (Message) in2.readObject();
            System.out.println("response was: " + response.getContent());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
