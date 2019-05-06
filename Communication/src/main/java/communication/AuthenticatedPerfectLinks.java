package communication;

import commontypes.AtomicFileManager;
import commontypes.Utils;
import communication.Communication;
import communication.data.Message;
import communication.data.ProcessInfo;
import communication.exception.AuthenticationException;
import communication.exception.NotFreshException;
import communication.exception.SaveNonceException;
import crypto.Crypto;
import crypto.CryptoException;

import java.io.IOException;
import java.util.ArrayList;

import static java.lang.System.currentTimeMillis;

public class AuthenticatedPerfectLinks {


    private static int validity;
    private static ArrayList<String> nonces;
    private static String noncesFile;
    private static boolean TESTING_ON = false;


    public static void initialize(int validityInt, ArrayList<String> noncesList, String noncesStorage){
        nonces = noncesList;
        noncesFile = noncesStorage;
        validity = validityInt;

    }
    public static Message sendMessage(ProcessInfo sender, ProcessInfo receiver, Message message)
            throws IOException, ClassNotFoundException,
            CryptoException, AuthenticationException, NotFreshException, SaveNonceException {

        message = (Message) Utils.deepCopy(message);

        authenticateMessage(sender, receiver, message);

        Message response = Communication.sendMessage(receiver.getHost(), receiver.getPort(), message);
        System.out.println(response.getOperation() + " - " + response.getNonce() + " -> " + response.getErrorMessage());

        validateResponse(sender, receiver, response);

        return response;
    }

    private static void validateResponse(ProcessInfo sender, ProcessInfo receiver, Message response)
            throws SaveNonceException, NotFreshException, AuthenticationException {

        //Verify response
        if(!isFresh(response)) {
            throw new NotFreshException();
        }

        //The request sender is the response receiver and vice-versa
        if(response.getSender() == null || response.getReceiver() == null ||
                !response.getSender().equals(receiver.getID()) || !response.getReceiver().equals(sender.getID())) {

            System.out.println("Authentication Exception");
            throw new AuthenticationException();
        }

        /*if(response.isSignatureValid(receiver.getPublicKey()))
            throw new AuthenticationException();*/
    }

    private static void authenticateMessage(ProcessInfo sender, ProcessInfo receiver, Message message) throws CryptoException {
        //Authenticate message
        message.setSender(sender.getID());
        message.setReceiver(receiver.getID());
        message.addFreshness(sender.getID());
        message.setSignature(Crypto.sign(message.getBytesToSign(), sender.getPrivateKey()));
    }

    /**
     * This method checks if a message is fresh
     * @param message message to be verified
     */
    private synchronized static boolean isFresh(Message message) throws SaveNonceException {
        if(nonces == null || noncesFile == null)
            return false;

        String nonce = message.getNonce();
        //Check freshness
        if((currentTimeMillis() - message.getTimestamp()) > validity ||
                nonces.contains(nonce))
            return false;
        nonces.add(nonce);

        //Store nonce
        if(!TESTING_ON) {
            try {

                AtomicFileManager.atomicWriteObjectToFile(noncesFile, nonces);
            } catch (IOException e) {
                e.printStackTrace();
                throw new SaveNonceException();
            }
        }

        return true;
    }
}
