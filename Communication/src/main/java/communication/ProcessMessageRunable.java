package communication;

import communication.data.Message;
import communication.interfaces.IMessageProcess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ProcessMessageRunable implements Runnable{


    private final IMessageProcess processMessage;
    private final Socket clientSocket;

    public ProcessMessageRunable(Socket clientSocket, IMessageProcess processMessage) {
        this.clientSocket = clientSocket;
        this.processMessage   = processMessage;
    }

    @Override
    public void run() {
        ObjectOutputStream out = null;

        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            Message request = null;

            // Parse received message
            request = (Message) in.readObject();


            // write response
            Message response = null;
            if (request != null) {
                response = processMessage.process(request);
                out.writeObject(response);
            }


            System.out.println("Processed request, closing connection");
            clientSocket.close();

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }


    }
}
