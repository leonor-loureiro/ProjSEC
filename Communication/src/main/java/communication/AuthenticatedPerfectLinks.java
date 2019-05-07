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
import java.security.Key;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;

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

        Message response = (Message) Utils.deepCopy(
                            Communication.sendMessage(receiver.getHost(), receiver.getPort(), message)
        );
        System.out.println(response.getOperation() + " - " + response.getNonce() + " -> " + response.getErrorMessage());

        validateResponse(sender, receiver, response);

        return response;
    }

    private synchronized static void validateResponse(ProcessInfo sender, ProcessInfo receiver, Message response)
            throws SaveNonceException, NotFreshException, AuthenticationException {

        //Verify response
        if(!isFresh(response)) {
            throw new NotFreshException(response.getSender());
        }

        //The request sender is the response receiver and vice-versa
        if(response.getSender() == null || response.getReceiver() == null ||
                !response.getSender().equals(receiver.getID()) || !response.getReceiver().equals(sender.getID())) {
            System.out.println("Sender/Receiver not valid");
            throw new AuthenticationException(response.getSender());
        }

        // verify signature
        try {
            if(!isSignatureValid(response, receiver.getPublicKey())){
                System.out.println("Invalid signature");
                throw new AuthenticationException(response.getSender());
            }
        } catch (CryptoException e) {
            e.printStackTrace();
        }
    }

    private synchronized static void authenticateMessage(ProcessInfo sender, ProcessInfo receiver, Message message) throws CryptoException {
        //Authenticate message
        message.setSender(sender.getID());
        message.setReceiver(receiver.getID());
        message.addFreshness(sender.getID());
        message.setSignature(Crypto.sign(message.getBytesToSign(), sender.getPrivateKey()));
        message.print();
    }

    /**
     * This method checks if a message is fresh
     * @param message message to be verified
     */
    private static boolean isFresh(Message message) throws SaveNonceException {
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


    /**
     * This method is responsible for validating whether a message signature is valid
     * @param message signed message
     * @param publicKey public key
     * @return true if valid, false otherwise
     */
    private static boolean  isSignatureValid(Message message, PublicKey publicKey)
            throws CryptoException {
        if(message.getSignature() == null) {
            System.out.println("Null signature");
            return false;
        }
        return Crypto.verifySignature(message.getSignature(), message.getBytesToSign(), publicKey);
    }
}
