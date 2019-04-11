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

public class BuyGoodTest extends ClientTests{

    @Test
    public void sucess() throws ClassNotFoundException, PasswordIsWrongException, CryptoException, UserNotExistException, IOException, GoodNotExistsException {
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

        clientManager.getStateOfGood(goodID); */

    }

    @Test (expected = GoodNotExistsException.class)
    public void goodDoesNotExist() throws ClassNotFoundException, PasswordIsWrongException, CryptoException, UserNotExistException, IOException, GoodNotExistsException {
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();


        users.add(
                new User(userID, keyPair.getPublic())
        );

        users.add(
                new User(userID2, keyPair2.getPublic())
        );

        goods.add(
                new Good("wronggood", userID, false)
        );

        clientManager.dummyPopulate(users, goods);

        Login login = new Login();

        login.setUsername(userID);
        login.setPassword((userID + userID).toCharArray());
        clientManager.login(login);

        clientManager.buyGood(userID2,goodID);

    }
    @Test (expected = UserNotExistException.class)
    public void SellerDoesNotExist() throws ClassNotFoundException, PasswordIsWrongException, CryptoException, UserNotExistException, IOException, GoodNotExistsException {
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

        clientManager.dummyPopulate(users, goods);

        Login login = new Login();

        login.setUsername(userID);
        login.setPassword((userID + userID).toCharArray());
        clientManager.login(login);

        clientManager.buyGood("wronguser",goodID);

    }
}
