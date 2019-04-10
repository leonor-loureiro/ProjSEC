import commontypes.Good;
import commontypes.User;
import communication.Message;
import crypto.Crypto;
import crypto.CryptoException;
import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;

import static java.lang.System.currentTimeMillis;

public class GetStateOfGoodTests extends NotaryServerTests{

    @Test
    public void success() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();

        users.add(
                new User(userID, keyPair.getPublic())
        );

        goods.add(
                new Good(goodID, userID, false)
        );

        notary.dummyPopulate(users, goods);
        // Create intention to sell request
        Message request = createGetStateOfGoodRequest();
        //Add freshness
        request.setTimestamp(currentTimeMillis());
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.GET_STATE_OF_GOOD, response.getOperation());
        Assert.assertEquals(goodID, response.getGoodID());
        Assert.assertFalse(response.isForSale());
    }

    private Message createGetStateOfGoodRequest() {
        Message request = new Message();
        request.setOperation(Message.Operation.GET_STATE_OF_GOOD);
        //Set seller ID
        request.setBuyerID(userID);
        //Set goodID
        request.setGoodID(goodID);
        return request;
    }

}
