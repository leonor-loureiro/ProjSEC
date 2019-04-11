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

public class GetStateOfGoodTest extends ClientTests{

    /**
     * The get state of good request isnt sent due to the fact the user inserted a good that does not exist.
     * @throws ClassNotFoundException
     * @throws PasswordIsWrongException
     * @throws CryptoException
     * @throws UserNotExistException
     * @throws IOException
     * @throws GoodNotExistsException
     */
    @Test (expected = GoodNotExistsException.class)
    public void goodDoesNotExist() throws ClassNotFoundException, PasswordIsWrongException, CryptoException, UserNotExistException, IOException, GoodNotExistsException {
       KeyPair keyPair = Crypto.generateRSAKeys();

        users.add(
                new User(userID, keyPair.getPublic())
        );

        goods.add(
                new Good("wronggood", userID, false)
        );

        clientManager.dummyPopulate(users, goods);

        Login login = new Login();

        login.setUsername(userID);
        login.setPassword((userID + userID).toCharArray());
        clientManager.login(login);

        clientManager.getStateOfGood(goodID);

    }
}
