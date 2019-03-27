package communication;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MessageTest {

    private Message message;
    @Before
    public void setUp(){
    }

    @Test
    public void convertToString(){
        message = new Message();
        message.setOperation(Message.Operation.INTENTION_TO_SELL);
        message.setGoodID("goodValue");
        message.setBuyerID("buyerValue");
        message.setSellerID("sellerValue");
        message.setNonce(10);
        message.setSignature("signature");
        message.setIntentionToBuy(new Message());

        Assert.assertNotNull(message.getBytesToSign());



    }

}