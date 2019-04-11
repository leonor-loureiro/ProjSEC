import client.Login;
import commontypes.Good;
import commontypes.User;
import commontypes.exception.GoodNotExistsException;
import commontypes.exception.PasswordIsWrongException;
import commontypes.exception.SaveNonceException;
import commontypes.exception.UserNotExistException;
import crypto.Crypto;
import crypto.CryptoException;
import org.junit.Test;

import java.io.IOException;
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
                new User(userID, keyPair.getPublic())
        );

        clientManager.dummyPopulate(users,new ArrayList<Good>());
        Login login = new Login();

        login.setUsername(userID);
        login.setPassword((userID + userID).toCharArray());
        clientManager.login(login);

        clientManager.intentionToSell(goodID);
    }
}