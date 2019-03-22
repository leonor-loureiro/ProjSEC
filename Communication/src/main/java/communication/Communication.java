package communication;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Communication{

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;


    public void listenSocket(IMessageProcess processMessage) throws IOException {
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

