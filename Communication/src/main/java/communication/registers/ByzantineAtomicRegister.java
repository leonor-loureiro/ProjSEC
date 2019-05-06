package communication.registers;


import communication.data.Message;
import communication.data.ProcessInfo;
import crypto.CryptoException;
import java.security.PrivateKey;
import java.util.List;


public class ByzantineAtomicRegister extends ByzantineRegularRegister {


    public ByzantineAtomicRegister(String id, List<ProcessInfo> servers, PrivateKey privateKey, int faults) {
        super(id, servers, privateKey, faults);


    }

    @Override
    public Message read(Message msg) throws CryptoException {
        Message response = super.read(msg);

        if(response.getOperation().equals(Message.Operation.ERROR))
            return response;

        //Write-back phase
        Message writeBackMsg = new Message();
        writeBackMsg.setOperation(Message.Operation.WRITE_BACK);
        //Set state of good
        writeBackMsg.setGoodID(response.getGoodID());
        writeBackMsg.setSellerID(response.getSellerID());
        writeBackMsg.setForSale(response.isForSale());
        //Add ID of sender
        writeBackMsg.setBuyerID(ID);

        Message writeBackRsp =
        super.writeImpl(writeBackMsg, response.getGoodID(), response.getSellerID(), response.isForSale(), response.getWts());

        return response;
    }
}
