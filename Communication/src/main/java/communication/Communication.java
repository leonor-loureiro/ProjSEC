package communication;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class responsible for the message transmission
 */
public class Communication{

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;


    /**
     * Function that receives 1 message through socket and runs the method
     * process message according to the IMessageProcess's processMessage class
     * @param processMessage a class that implements a method to process the message
     * @throws IOException if an error happens during socket connection
     */
    public void listenAndProcess(IMessageProcess processMessage) throws IOException {
        clientSocket = serverSocket.accept();

        System.out.println("Running socket on port: " + serverSocket.getLocalPort());

        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
        Message request = null;

        System.out.println("Received message on port: " + serverSocket.getLocalPort());

        try {
            request = (Message) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        Message response = null;
        if (request != null) {
            response = processMessage.process(request);
        }

        if(response != null)
            out.writeObject(response);

        System.out.println("Processed request, closing connection");
        clientSocket.close();

    }

    public Message sendMessage(String host, int port, Message message) throws IOException, ClassNotFoundException {
        Socket msgSocket = new Socket(host, port);

        if(host == null || port == 0 || message == null) {
            System.out.println("Message is not correct");
            throw new IllegalArgumentException();
        }

        ObjectOutputStream out2 = new ObjectOutputStream(msgSocket.getOutputStream());
        ObjectInputStream in2 = new ObjectInputStream(msgSocket.getInputStream());
        out2.writeObject(message);

        Message response = (Message) in2.readObject();

        return response;
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);

    }


    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }




}

