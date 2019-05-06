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

public class TransferGoodTest extends NotaryServerTest {

    String buyerID = "user2";

    @Test
    public void success() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();
        users.add(
                new User(userID, keyPair.getPublic())
        );
        users.add(
                new User(buyerID, keyPair2.getPublic())
        );
        goods.add(
                new Good(goodID, userID, true)
        );

        notary.dummyPopulate(users, goods);

        // Create intention to buy request
        Message intentionToBuy = createIntentionToBuy();
        intentionToBuy.setTimestamp(currentTimeMillis());
        intentionToBuy.setNonce(buyerID + random.nextInt());
        intentionToBuy.setSignature(Crypto.sign(intentionToBuy.getBytesToSign(), keyPair2.getPrivate()));

        // Create transfer good request
        Message request = createTransferGood();
        //Add intention to buy
        request.setIntentionToBuy(intentionToBuy);
        //Add freshness
        request.setTimestamp(currentTimeMillis());
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.TRANSFER_GOOD, response.getOperation());
        Assert.assertEquals(userID, response.getSellerID());
        Assert.assertEquals(buyerID, response.getBuyerID());
        Assert.assertEquals(goodID, response.getGoodID());
        Assert.assertTrue(
                Crypto.verifySignature(response.getSignature(), response.getBytesToSign(), notaryPublicKey)
        );
    }

    @Test
    public void goodNotExists() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();
        users.add(
                new User(userID, keyPair.getPublic())
        );
        users.add(
                new User(buyerID, keyPair2.getPublic())
        );

        notary.dummyPopulate(users, goods);

        // Create intention to buy request
        Message intentionToBuy = createIntentionToBuy();
        intentionToBuy.setTimestamp(currentTimeMillis());
        intentionToBuy.setNonce(buyerID + random.nextInt());
        intentionToBuy.setSignature(Crypto.sign(intentionToBuy.getBytesToSign(), keyPair2.getPrivate()));

        // Create transfer good request
        Message request = createTransferGood();
        //Add intention to buy
        request.setIntentionToBuy(intentionToBuy);
        //Add freshness
        request.setTimestamp(currentTimeMillis());
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
    }

    @Test
    public void buyerNotExists() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();
        users.add(
                new User(userID, keyPair.getPublic())
        );
        goods.add(
                new Good(goodID, userID, true)
        );

        notary.dummyPopulate(users, goods);

        // Create intention to buy request
        Message intentionToBuy = createIntentionToBuy();
        intentionToBuy.setTimestamp(currentTimeMillis());
        intentionToBuy.setNonce(buyerID + random.nextInt());
        intentionToBuy.setSignature(Crypto.sign(intentionToBuy.getBytesToSign(), keyPair2.getPrivate()));

        // Create transfer good request
        Message request = createTransferGood();
        //Add intention to buy
        request.setIntentionToBuy(intentionToBuy);
        //Add freshness
        request.setTimestamp(currentTimeMillis());
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        System.out.println(response.getErrorMessage());

        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
    }

    @Test
    public void sellerNotExists() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();

        users.add(
                new User(buyerID, keyPair2.getPublic())
        );
        goods.add(
                new Good(goodID, userID, true)
        );

        notary.dummyPopulate(users, goods);

        // Create intention to buy request
        Message intentionToBuy = createIntentionToBuy();
        intentionToBuy.setTimestamp(currentTimeMillis());
        intentionToBuy.setNonce(buyerID + random.nextInt());
        intentionToBuy.setSignature(Crypto.sign(intentionToBuy.getBytesToSign(), keyPair2.getPrivate()));

        // Create transfer good request
        Message request = createTransferGood();
        //Add intention to buy
        request.setIntentionToBuy(intentionToBuy);
        //Add freshness
        request.setTimestamp(currentTimeMillis());
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
        System.out.println(response.getErrorMessage());
    }


    @Test
    public void intentionToBuySignatureInvalid() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();
        users.add(
                new User(userID, keyPair.getPublic())
        );
        users.add(
                new User(buyerID, keyPair2.getPublic())
        );
        goods.add(
                new Good(goodID, userID, true)
        );

        notary.dummyPopulate(users, goods);

        // Create intention to buy request
        Message intentionToBuy = createIntentionToBuy();
        intentionToBuy.setTimestamp(currentTimeMillis());
        intentionToBuy.setNonce(buyerID + random.nextInt());
        //sign with key of the seller
        intentionToBuy.setSignature(Crypto.sign(intentionToBuy.getBytesToSign(), keyPair.getPrivate()));

        // Create transfer good request
        Message request = createTransferGood();
        //Add intention to buy
        request.setIntentionToBuy(intentionToBuy);
        //Add freshness
        request.setTimestamp(currentTimeMillis());
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
        System.out.println(response.getErrorMessage());
    }



    @Test
    public void corruptedIntentionToBuy() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();
        users.add(
                new User(userID, keyPair.getPublic())
        );
        users.add(
                new User(buyerID, keyPair2.getPublic())
        );
        goods.add(
                new Good(goodID, userID, true)
        );

        notary.dummyPopulate(users, goods);

        // Create intention to buy request
        Message intentionToBuy = createIntentionToBuy();
        intentionToBuy.setTimestamp(currentTimeMillis());
        intentionToBuy.setNonce(buyerID + random.nextInt());
        intentionToBuy.setSignature(Crypto.sign(intentionToBuy.getBytesToSign(), keyPair2.getPrivate()));
        //Change nonce after signing
        intentionToBuy.setNonce(buyerID + random.nextInt());

        // Create transfer good request
        Message request = createTransferGood();
        //Add intention to buy
        request.setIntentionToBuy(intentionToBuy);
        //Add freshness
        request.setTimestamp(currentTimeMillis());
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
        System.out.println(response.getErrorMessage());
    }

    @Test
    public void sellerNotCurrentOwner() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();

        KeyPair keyPair2 = Crypto.generateRSAKeys();
        users.add(
                new User(userID, keyPair.getPublic())
        );
        users.add(
                new User(buyerID, keyPair2.getPublic())
        );
        goods.add(
                new Good(goodID, buyerID, true)
        );

        notary.dummyPopulate(users, goods);

        // Create intention to buy request
        Message intentionToBuy = createIntentionToBuy();
        intentionToBuy.setTimestamp(currentTimeMillis());
        intentionToBuy.setNonce(buyerID + random.nextInt());
        intentionToBuy.setSignature(Crypto.sign(intentionToBuy.getBytesToSign(), keyPair2.getPrivate()));

        // Create transfer good request
        Message request = createTransferGood();
        //Add intention to buy
        request.setIntentionToBuy(intentionToBuy);
        //Add freshness
        request.setTimestamp(currentTimeMillis());
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);
        System.out.println(response.getErrorMessage());
        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());


    }

    @Test
    public void goodNotForSale() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();
        users.add(
                new User(userID, keyPair.getPublic())
        );
        users.add(
                new User(buyerID, keyPair2.getPublic())
        );
        goods.add(
                new Good(goodID, userID, false)
        );

        notary.dummyPopulate(users, goods);

        // Create intention to buy request
        Message intentionToBuy = createIntentionToBuy();
        intentionToBuy.setTimestamp(currentTimeMillis());
        intentionToBuy.setNonce(buyerID + random.nextInt());
        intentionToBuy.setSignature(Crypto.sign(intentionToBuy.getBytesToSign(), keyPair2.getPrivate()));

        // Create transfer good request
        Message request = createTransferGood();
        //Add intention to buy
        request.setIntentionToBuy(intentionToBuy);
        //Add freshness
        request.setTimestamp(currentTimeMillis());
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
        System.out.println(response.getErrorMessage());
    }

    @Test
    public void invalidTransferGoodSignature() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();
        users.add(
                new User(userID, keyPair.getPublic())
        );
        users.add(
                new User(buyerID, keyPair2.getPublic())
        );
        goods.add(
                new Good(goodID, userID, true)
        );

        notary.dummyPopulate(users, goods);

        // Create intention to buy request
        Message intentionToBuy = createIntentionToBuy();
        intentionToBuy.setNonce(buyerID + random.nextInt());
        intentionToBuy.setTimestamp(currentTimeMillis());
        intentionToBuy.setSignature(Crypto.sign(intentionToBuy.getBytesToSign(), keyPair2.getPrivate()));

        // Create transfer good request
        Message request = createTransferGood();
        //Add intention to buy
        request.setIntentionToBuy(intentionToBuy);
        //Add freshness
        request.setTimestamp(currentTimeMillis());
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair2.getPrivate()));

        Message response = notary.process(request);


        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
        System.out.println(response.getErrorMessage());

    }

    @Test
    public void intentionToBuyNotFresh() throws CryptoException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();
        users.add(
                new User(userID, keyPair.getPublic())
        );
        users.add(
                new User(buyerID, keyPair2.getPublic())
        );
        goods.add(
                new Good(goodID, userID, true)
        );

        notary.dummyPopulate(users, goods);

        // Create intention to buy request
        Message intentionToBuy = createIntentionToBuy();
        intentionToBuy.setTimestamp(currentTimeMillis() - 1000000);
        intentionToBuy.setNonce(buyerID + random.nextInt());
        intentionToBuy.setSignature(Crypto.sign(intentionToBuy.getBytesToSign(), keyPair2.getPrivate()));

        // Create transfer good request
        Message request = createTransferGood();
        //Add intention to buy
        request.setIntentionToBuy(intentionToBuy);
        //Add freshness
        request.setTimestamp(currentTimeMillis());
        request.setNonce(userID + random.nextInt());
        //Sign
        request.setSignature(Crypto.sign(request.getBytesToSign(), keyPair.getPrivate()));

        Message response = notary.process(request);

        Assert.assertEquals(Message.Operation.ERROR, response.getOperation());
        System.out.println(response.getErrorMessage());
    }



    private Message createTransferGood() {
        Message request = new Message();
        request.setOperation(Message.Operation.TRANSFER_GOOD);
        //Set seller ID
        request.setSellerID(userID);
        //Set goodID
        request.setGoodID(goodID);
        return request;
    }

    private Message createIntentionToBuy(){
        Message request = new Message();
        request.setOperation(Message.Operation.BUY_GOOD);
        //Set buyer ID
        request.setBuyerID(buyerID);
        //Set goodID
        request.setGoodID(goodID);
        return request;
    }
}
