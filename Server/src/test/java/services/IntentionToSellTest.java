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


/**
 * This class tests the process of an intention to sell
 * request received by the notary server
 */
public class IntentionToSellTest extends NotaryServerTest {


    /**
     * The user that owns the good sends an authenticated, fresh request
     * to change the good status to for sale.
     * The request is successful.
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
        Message request = createIntentionToSellRequest();
        //Add freshness
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.INTENTION_TO_SELL, response.getOperation());
        Assert.assertEquals(goodID, response.getGoodID());
        Assert.assertTrue(response.isForSale());
        Assert.assertTrue(
                Crypto.verifySignature(response.getSignature(), response.getBytesToSign(), notaryPublicKey)
        );

    }


    /**
     * Send an intention to sell request for a good that doesn't exist.
     * The server returns an error message.
     */
    @Test
    public void goodNotExists() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        users.add(
                new User(userID, keyPair.getPublic())
        );
        notary.dummyPopulate(users, goods);

        // Create intention to sell request
        Message request = createIntentionToSellRequest();

        //Add freshness
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);
        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
        /*Assert.assertTrue(
                Crypto.verifySignature(response.getSignature(), response.getBytesToSign(), notaryPublicKey)
        );*/


    }

    /**
     * The user that sends the intention to sell request doesn't exist
     * The server returns an error message.
     */
    @Test
    public void userNotExists() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        goods.add(
                new Good(goodID, userID, false)
        );

        notary.dummyPopulate(users, goods);

        // Create intention to sell request
        Message request = createIntentionToSellRequest();

        //Add freshness
        request.setNonce(userID + random.nextInt());

        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
        Assert.assertTrue(
                Crypto.verifySignature(response.getSignature(), response.getBytesToSign(), notaryPublicKey)
        );

    }

    /**
     * The user that sends the intention to sell request is not it's current owner.
     * The server returns an error message.
     */
    @Test
    public void notTheSeller() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        String user2 = "user2ID";
        users.add(
                new User(userID, keyPair.getPublic())
        );
        users.add(
                new User(user2, Crypto.generateRSAKeys().getPublic())
        );

        goods.add(
                new Good(goodID, user2, false)
        );

        notary.dummyPopulate(users, goods);

        // Create intention to sell request
        Message request = createIntentionToSellRequest();
        //Add freshness
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
        System.out.println(response.getErrorMessage());
        Assert.assertTrue(
                Crypto.verifySignature(response.getSignature(), response.getBytesToSign(), notaryPublicKey)
        );

    }

    /**
     * Signature is not valid
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
        Message request = createIntentionToSellRequest();
        //Add freshness
        request.setNonce(userID + random.nextInt());


        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
        Assert.assertTrue(
                Crypto.verifySignature(response.getSignature(), response.getBytesToSign(), notaryPublicKey)
        );

    }

    /**
     * Change the seller in the request after signing it.
     */
    @Test
    public void corrupted() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        String user2ID = "user2";
        users.add(
                new User(userID, keyPair.getPublic())
        );
        users.add(
                new User(user2ID, keyPair.getPublic())
        );

        goods.add(
                new Good(goodID, userID, false)
        );

        notary.dummyPopulate(users, goods);

        // Create intention to sell request
        Message request = createIntentionToSellRequest();
        //Add freshness
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        //Change seller
        request.setSellerID(user2ID);
        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
        Assert.assertTrue(
                Crypto.verifySignature(response.getSignature(), response.getBytesToSign(), notaryPublicKey)
        );

    }



    /**
     * Send same request twice
     */
    @Test
    public void reusedNonce() throws CryptoException {
        notary.dummyPopulate(users, goods);
        KeyPair keyPair = Crypto.generateRSAKeys();
        // Create intention to sell request
        Message request = createIntentionToSellRequest();
        //Add freshness
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        //Send message once
        notary.process(request);
        //Re-send same message
        Message response = notary.process(request);


        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
        System.out.println(response.getErrorMessage());
        Assert.assertTrue(
                Crypto.verifySignature(response.getSignature(), response.getBytesToSign(), notaryPublicKey)
        );
    }




    private Message createIntentionToSellRequest() {
        Message request = new Message();
        request.setOperation(Message.Operation.INTENTION_TO_SELL);
        //Set seller ID
        request.setSellerID(userID);
        //Set goodID
        request.setGoodID(goodID);
        return request;
    }


}
