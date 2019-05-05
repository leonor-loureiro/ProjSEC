import client.Login;
import commontypes.Good;
import commontypes.User;
import commontypes.exception.GoodNotExistsException;
import crypto.Crypto;
import org.junit.Test;

import java.security.KeyPair;
import java.util.ArrayList;


public class IntentionToSellTest extends ClientTests {

    /**
     * The intentiontosell request isnt sent due to the fact the user inserted a good that does not exist.
     */
    @Test (expected= GoodNotExistsException.class)
    public void goodDoesnotExist() throws Exception{
       KeyPair keyPair = Crypto.generateRSAKeys();

        users.add(
                new User(seller, keyPair.getPublic())
        );

        clientManager.dummyPopulate(users,new ArrayList<Good>());
        Login login = new Login();

        login.setUsername(seller);
        login.setPassword((seller + seller).toCharArray());
        clientManager.login(login);

        clientManager.intentionToSell(goodID);
    }
}