package communication;

import crypto.Crypto;
import crypto.CryptoException;
import javafx.util.Pair;

import java.security.PrivateKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ByzantineAtomicRegister extends ByzantineRegularRegister{

    public ByzantineAtomicRegister(List<Pair<String, Integer>> servers, PrivateKey privateKey,
                                   Communication communicationHandler, int faults){
        super(servers,privateKey,communicationHandler,faults);
    }

    /**
     * Sends a message that requests a write operation
     * @param msg message to send
     * @return server's response message
     */
    public Message write(Message msg, String goodID, String userID, boolean isForSale)
            throws CryptoException {
        return super.write(msg,goodID,userID,isForSale);
    }

    public void writeBack(Message msg, String goodID, String userID, boolean isForSale)
            throws CryptoException {

        //Clear previous responses
        ackList.clear();

        //Sign the new value
        String value = goodID + "|" + userID + "|" + isForSale;
        msg.setValSignature(Crypto.sign(value.getBytes(), privateKey));

        //Send the message to all server replicas
        broadcast(msg, WRITE);

        //Wait until a majority servers have responded
        while(ackList.size() < quorum){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Message read(Message msg) throws CryptoException {
        //Update read timestamp

        //TODO: changed this atributes to public, leonor if you want can make them private and do set(get()+1) etc
        rid ++;
        msg.setRid(rid);

        readList.clear();

        //Send the read request to all servers
        super.broadcast(msg, READ);

        //Wait until a majority of servers have replied
        while (readList.size() < quorum) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Quorum reads");

        //Atomic Read needs writeback

        //save response
        Message response = Collections.max(readList);


        //TODO what strings and parameters does one put here lads?
        this.writeBack(response,"","",false);

        return response;
    }
}
