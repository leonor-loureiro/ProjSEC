package communication;


import crypto.CryptoException;
import javafx.util.Pair;
import java.security.PrivateKey;
import java.util.List;


public class ByzantineAtomicRegister extends ByzantineRegularRegister {

    public ByzantineAtomicRegister(String id, List<Pair<String, Integer>> servers, PrivateKey privateKey,
                                   Communication communicationHandler, int faults) {
        super(id, servers, privateKey, communicationHandler, faults);

    }

    @Override
    public Message read(Message msg) throws CryptoException {

        Message response = super.read(msg);

        //Write-back phase
        Message writeBackMsg = new Message();
        writeBackMsg.setOperation(Message.Operation.WRITE_BACK);
        //Set state of good
        writeBackMsg.setGoodID(response.getGoodID());
        writeBackMsg.setSellerID(response.getSellerID());
        writeBackMsg.setForSale(response.isForSale());
        //Add ID of sender
        writeBackMsg.setBuyerID(ID);

        writeBackMsg.addFreshness(ID);

        Message writeBackRsp =
        super.write(writeBackMsg, response.getGoodID(), response.getSellerID(), response.isForSale(), response.getWts());

        //TODO: validate server response

        return response;
    }
}
