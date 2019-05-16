package services;

import commontypes.Good;
import commontypes.User;
import communication.data.Message;
import crypto.Crypto;
import crypto.CryptoException;
import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;

import static java.lang.System.currentTimeMillis;

public class GetStateOfGoodTest extends NotaryServerTest {

    /**
     * User sends a fresh, authenticated get state of good request.
     * The operation is successful.
     */
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
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.GET_STATE_OF_GOOD, response.getOperation());
        Assert.assertEquals(goodID, response.getGoodID());
        Assert.assertFalse(response.isForSale());
        Assert.assertTrue(
                Crypto.verifySignature(response.getSignature(), response.getBytesToSign(), notaryPublicKey)
        );
    }

    /**
     * Request state of a good that not exists.
     */
    @Test
    public void goodNotExists() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();

        users.add(
                new User(userID, keyPair.getPublic())
        );

        notary.dummyPopulate(users, goods);
        // Create intention to sell request
        Message request = createGetStateOfGoodRequest();
        //Add freshness
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
    }

    /**
     * A user, not in the system, request the state of a good.
     */
    @Test
    public void userNotExists() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();

        goods.add(
                new Good(goodID, userID, false)
        );

        notary.dummyPopulate(users, goods);
        // Create intention to sell request
        Message request = createGetStateOfGoodRequest();
        //Add freshness
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);
        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
    }

    /**
     * Signature of the request is not valid.
     */
    @Test
    public void invalidSignature() throws CryptoException {
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
        request.setNonce(userID + random.nextInt());

        Message response = notary.process(request);
        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
    }

    /**
     * User is changed after the request has been signed.
     */
    @Test
    public void corrupted() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        String user2 = "user2";
        users.add(
                new User(userID, keyPair.getPublic())
        );
        users.add(
                new User(user2, keyPair.getPublic())
        );

        goods.add(
                new Good(goodID, userID, false)
        );

        notary.dummyPopulate(users, goods);
        // Create intention to sell request
        Message request = createGetStateOfGoodRequest();
        //Add freshness
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        //Change user
        request.setBuyerID(user2);

        Message response = notary.process(request);
        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
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
