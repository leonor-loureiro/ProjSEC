import client.Login;
import commontypes.Good;
import commontypes.User;
import commontypes.exception.GoodNotExistsException;
import crypto.Crypto;
import org.junit.Test;

import java.security.KeyPair;

public class GetStateOfGoodTest extends ClientTests{

    /**
     * The get state of good request isnt sent due to the fact the user inserted a good that does not exist.
     */
    @Test (expected = GoodNotExistsException.class)
    public void goodDoesNotExist() throws Exception{
       KeyPair keyPair = Crypto.generateRSAKeys();

        users.add(
                new User(seller, keyPair.getPublic())
        );

        goods.add(
                new Good("wronggood", seller, false)
        );

        clientManager.dummyPopulate(users, goods);

        Login login = new Login();

        login.setUsername(seller);
        login.setPassword((seller + seller).toCharArray());
        clientManager.login(login);

        clientManager.getStateOfGood(goodID);

    }
}
