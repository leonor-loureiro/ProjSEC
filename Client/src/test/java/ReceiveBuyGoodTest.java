import client.Login;
import commontypes.Good;
import commontypes.User;
import communication.Message;
import crypto.Crypto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;

public class ReceiveBuyGoodTest extends ClientTests{

    @Before
    public void setUp() throws Exception {
        super.setUp();
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();


        users.add(
                new User(userID, keyPair.getPublic())
        );

        users.add(
                new User(userID2, keyPair2.getPublic())
        );

        goods.add(
                new Good(goodID, userID, false)
        );

        Login login = new Login();

        clientManager.dummyPopulate(users, goods);

        login.setUsername(userID);
        login.setPassword((userID + userID).toCharArray());
        clientManager.login(login);

    }
    @Test
    public void GoodDoesNotExist()  {
        Message message = generateBuyGoodMessage(userID,userID2,"wrong good");
        clientManager.addFreshness(message);
        Message response = clientManager.process(message);

        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Good does not exist");

    }
    @Test
    public void BuyerDoesNotExist() {

        Message message = generateBuyGoodMessage(userID,"wronguser",goodID);
        clientManager.addFreshness(message);
        Message response = clientManager.process(message);

        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Buyer user does not exist");

    }
    @Test
    public void SellerDoesNotExist() {

        Message message = generateBuyGoodMessage("wronguser",userID2,goodID);
        clientManager.addFreshness(message);
        Message response = clientManager.process(message);

        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Seller user does not exist");

    }

    @Test
    public void SellerDoesNotMath() {

        Message message = generateBuyGoodMessage(userID2,userID,goodID);
        clientManager.addFreshness(message);
        Message response = clientManager.process(message);

        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Seller ID does not match current owner.");

    }
    @Test
    public void NotFreshMessageRepeatedNonce() {

        Message message = generateBuyGoodMessage(userID,userID2,goodID);
        Message response = clientManager.process(message);

        clientManager.addFreshness(message);
        clientManager.addNonce("repeatednonce");
        message.setNonce("repeatednonce");

        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Request is not fresh");

    }

    @Test
    public void NotFreshMessageBadTimestamp() {

        Message message = generateBuyGoodMessage(userID,userID2,goodID);
        Message response = clientManager.process(message);

        clientManager.addFreshness(message);

        message.setTimestamp(0);

        Assert.assertEquals(response.getOperation(),Message.Operation.ERROR);
        Assert.assertEquals(response.getErrorMessage(),"Request is not fresh");

    }

    private Message generateBuyGoodMessage(String userid,String userid2,String goodid) {
        Message message = new Message();
        message.setBuyerID(userid2);
        message.setSellerID(userid);
        message.setGoodID(goodid);
        message.setOperation(Message.Operation.BUY_GOOD);
        return message;

    }
}
