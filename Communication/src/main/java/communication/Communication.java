package communication;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Class responsible for the message transmission
 */
public class Communication{

    private static ServerSocket serverSocket;
    private static Socket clientSocket;



    /**
     * Function that receives 1 message through socket and runs the method
     * process message according to the IMessageProcess's processMessage class
     * @param processMessage a class that implements a method to process the message
     * @throws IOException if an error happens during socket connection
     */
    public void listenAndProcess(IMessageProcess processMessage) throws IOException {

        try {
            clientSocket = serverSocket.accept();
        }catch(SocketException e){
            System.out.println("Closing socket...");
            return;
        }

        System.out.println("Received request, running socket on port: " + serverSocket.getLocalPort());


        // set receiving stream and output stream; according to java documentation, out must exist first.
        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

        Message request = null;

        System.out.println("Received message on port: " + serverSocket.getLocalPort());

        // Parse received message
        try {
            request = (Message) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        // write response
        Message response = null;
        if (request != null) {
            response = processMessage.process(request);
            response.setSender(request.getReceiver());
            response.setReceiver(request.getSender());
            out.writeObject(response);
        }


        System.out.println("Processed request, closing connection");
        clientSocket.close();
    }

    /**
     * Sends given message to host's link and port
     * @param host the url or link to the host
     * @param port host's post on given url
     * @param message the message to be sent
     * @return the response from the host, if such exists
     * @throws IOException if an issue occurred while communicating with host
     * @throws ClassNotFoundException if the response wasn't a message
     */
    public static Message sendMessage(String host, int port, Message message) throws IOException, ClassNotFoundException {
        Socket msgSocket = new Socket(host, port);

        if(host == null || port == 0 || message == null) {
            System.out.println("Message is not correct");
            throw new IllegalArgumentException();
        }

        ObjectOutputStream out2 = null;
        ObjectInputStream in2 = null;
        Message response;

        try {
            //Send
            out2 = new ObjectOutputStream(msgSocket.getOutputStream());
            out2.writeObject(message);


            //Receive
            in2 = new ObjectInputStream(msgSocket.getInputStream());
            response = (Message) in2.readObject();

        }finally {
            if(out2 != null)
                out2.close();

            if(in2 != null)
                in2.close();
        }

        return response;

    }

    /**
     * initializes the server socket
     * @param port port at which the server will start/receive requests
     * @throws IOException if an error occurs while starting the server socket
     */
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);

    }

    /**
     * Stops the reception of messages
     * @throws IOException if an error happens while trying to close the sockets
     */
    public void stop() throws IOException {
        System.out.println("Stopping communication...");

        try{
            serverSocket.close();;
            clientSocket.close();

        }catch (NullPointerException np){
        }
    }





}

