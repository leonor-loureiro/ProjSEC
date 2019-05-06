package communication;

import crypto.Crypto;
import crypto.CryptoException;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class AuthenticatedPerfectLinks {


    public  Message sendMessage(String senderID, String host , int port, Message message)
            throws IOException, ClassNotFoundException, CryptoException, AuthenticationException {

        //Authenticate message
        message.addFreshness(senderID);
        PrivateKey senderKey = null;
        message.setSignature(Crypto.sign(message.getBytesToSign(), senderKey));

        Message response = Communication.sendMessage(host, port, message);

        //Verify response
        PublicKey receiverKey = null;
        if(message.isSignatureValid(receiverKey))
            throw new AuthenticationException();

        return response;



    }
}
