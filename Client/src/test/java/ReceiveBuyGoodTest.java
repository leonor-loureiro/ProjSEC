import client.Login;
import commontypes.Good;
import commontypes.User;
import communication.data.Message;
import crypto.Crypto;
import crypto.CryptoException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;

public class ReceiveBuyGoodTest extends ClientTests{

    KeyPair keyPair;
    KeyPair keyPair2;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        keyPair = Crypto.generateRSAKeys();
        keyPair2 = Crypto.generateRSAKeys();


        users.add(
                new User(seller, keyPair.getPublic())
        );

        users.add(
                new User(buyer, keyPair2.getPublic())
        );

        goods.add(
                new Good(goodID, seller, false)
        );

        Login login = new Login();

        clientManager.dummyPopulate(users, goods);

        login.setUsername(seller);
        login.setPassword((seller + seller).toCharArray());
        clientManager.login(login);

    }

    /**
     * User sends a buygood request fails due to the good not existing
     */
    @Test
    public void GoodDoesNotExist() throws CryptoException {
        Message message = generateBuyGoodMessage(seller, buyer,"wrong good");
        message.addFreshness(buyer);

        message.setSignature(Crypto.sign(message.getBytesToSign(),keyPair.getPrivate()));

        Message response = clientManager.process(message);

        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Good does not exist");

    }

    /**
     * User sends a buygood request fails due to the buyer not existing
     */
    @Test
    public void BuyerDoesNotExist() throws CryptoException {

        Message message = generateBuyGoodMessage(seller,"wronguser",goodID);
        message.addFreshness(buyer);
        message.setSignature(Crypto.sign(message.getBytesToSign(),keyPair.getPrivate()));

        Message response = clientManager.process(message);

        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Buyer user does not exist");

    }

    /**
     * User sends a buygood requests fails due to the seller not existing
     */
    @Test
    public void SellerDoesNotExist() throws CryptoException {

        Message message = generateBuyGoodMessage("wronguser", buyer,goodID);
        message.addFreshness(buyer);

        message.setSignature(Crypto.sign(message.getBytesToSign(),keyPair.getPrivate()));

        Message response = clientManager.process(message);


        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Seller user does not exist");

    }

    /**
     * User sends a buygood request fails due to the seller and receiving user not matching.
     */
    @Test
    public void SellerDoesNotMath() throws CryptoException {

        Message message = generateBuyGoodMessage(buyer, seller,goodID);
        message.addFreshness(buyer);

        message.setSignature(Crypto.sign(message.getBytesToSign(),keyPair.getPrivate()));

        Message response = clientManager.process(message);

        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Seller ID does not match my ID");

    }
    @Test

    /**
     * User sends a not fresh message and is refused by the other client.
     */
    public void NotFreshMessageRepeatedNonce() throws CryptoException {

        Message message = generateBuyGoodMessage(seller, buyer,goodID);

        message.addFreshness(buyer);

        message.setSignature(Crypto.sign(message.getBytesToSign(),keyPair.getPrivate()));


        Message response = clientManager.process(message);

        Message response2 = clientManager.process(message);


        Assert.assertEquals(response2.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response2.getErrorMessage(),"Request is not fresh");

    }

    /**
     * User sends a not fresh message and is refused by the other client.
     */

    @Test
    public void NotFreshMessageBadTimestamp() {

        Message message = generateBuyGoodMessage(seller, buyer,goodID);
        message.addFreshness(buyer);

        message.setTimestamp(0);

        Message response = clientManager.process(message);


        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Request is not fresh");

    }

    /**
     * Test where the user doesnt sign the message, so the other user refuses the message.
     */
    @Test
    public void NotSignedMessage(){
        Message message = generateBuyGoodMessage(seller, buyer,goodID);
        message.addFreshness(buyer);
        Message response = clientManager.process(message);

        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Authentication Failed");
    }

    /**
     * Test where the user changes the message after signing, so the other refuses the message.
     */
    @Test
    public void CorruptMessage() throws CryptoException {
        Message message = generateBuyGoodMessage(seller, buyer,goodID);
        message.addFreshness(buyer);

        message.setSignature(Crypto.sign(message.getBytesToSign(),keyPair.getPrivate()));

        message.setBuyerID(seller);
        Message response = clientManager.process(message);

        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Authentication Failed");

    }



    /**
     * generates a buygood message
     */
    private Message generateBuyGoodMessage(String userid,String userid2,String goodid) {
        Message message = new Message();
        message.setBuyerID(userid2);
        message.setSellerID(userid);
        message.setGoodID(goodid);
        message.setOperation(Message.Operation.BUY_GOOD);
        return message;

    }
}
