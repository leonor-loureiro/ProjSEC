import client.Login;
import commontypes.Good;
import commontypes.User;
import commontypes.exception.GoodNotExistsException;
import commontypes.exception.PasswordIsWrongException;
import commontypes.exception.UserNotExistException;
import crypto.Crypto;
import crypto.CryptoException;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;


public class IntentionToSellTest extends ClientTests {

    @Test
    public void sucess() throws CryptoException, IOException, ClassNotFoundException, GoodNotExistsException {
   /*   KeyPair keyPair = Crypto.generateRSAKeys();

        users.add(
                new User(userID, keyPair.getPublic())
        );

        goods.add(
                new Good(goodID, userID, false)
        );

        Login login = new Login();

        login.setUsername(userID);
        login.setPassword((userID + userID).toCharArray());
        clientManager.login(login);

        clientManager.intentionToSell(goodID); */
    }
    @Test (expected= GoodNotExistsException.class)
    public void goodDoesnotExist() throws CryptoException, IOException, ClassNotFoundException, GoodNotExistsException, UserNotExistException, PasswordIsWrongException {
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