package communication.registers;


import commontypes.User;
import communication.data.Message;
import communication.data.ProcessInfo;
import crypto.CryptoException;
import java.security.PrivateKey;
import java.util.List;


public class ByzantineAtomicRegister extends ByzantineRegularRegister {


    public ByzantineAtomicRegister(String id, List<ProcessInfo> servers, List<User> writers, PrivateKey privateKey, int faults) {
        super(id, servers, writers, privateKey, faults);


    }

    @Override
    public Message read(Message msg) throws CryptoException {
        Message response = super.read(msg);

        if(response == null || response.getOperation().equals(Message.Operation.ERROR))
            return response;

        //Write-back phase
        Message writeBackMsg = new Message();
        writeBackMsg.setOperation(Message.Operation.WRITE_BACK);
        //Set state of good
        writeBackMsg.setGoodID(response.getGoodID());
        writeBackMsg.setSellerID(response.getSellerID());
        writeBackMsg.setForSale(response.isForSale());
        writeBackMsg.setWts(response.getWts());
        writeBackMsg.setWriter(response.getWriter());
        writeBackMsg.setValSignature(response.getValSignature());
        //Add ID of sender
        writeBackMsg.setBuyerID(ID);


        Message writeBackRsp =
        super.writeImpl(writeBackMsg, response.getSellerID(), response.getWts());

        if(writeBackRsp == null) {
            System.out.println("Write back failed");
            return null;
        }

        return response;
    }

}
