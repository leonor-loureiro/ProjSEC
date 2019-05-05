import client.Login;
import commontypes.Good;
import commontypes.User;
import commontypes.exception.PasswordIsWrongException;
import commontypes.exception.UserNotExistException;
import crypto.Crypto;
import crypto.CryptoException;
import org.junit.*;
import resourcesloader.ResourcesLoader;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;


public class LoginTest extends ClientTests{

    /**
     * User inserts correct login information and succesfully logs in.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws CryptoException
     * @throws UserNotExistException
     * @throws PasswordIsWrongException
     */
    @Test
    public void sucess() throws IOException, ClassNotFoundException, CryptoException, UserNotExistException, PasswordIsWrongException {
        KeyPair keyPair = Crypto.generateRSAKeys();

        users.add(
                new User(seller, keyPair.getPublic())
        );

        clientManager.dummyPopulate(users,new ArrayList<Good>());
        Login login = new Login();

        login.setUsername(seller);
        login.setPassword((seller + seller).toCharArray());
        clientManager.login(login);


        Assert.assertNotNull(clientManager.getUser());
        Assert.assertEquals(login.getUsername(),clientManager.getUser().getUserID());
        Assert.assertEquals(users.get(0).getPublicKey(),keyPair.getPublic());
        Assert.assertEquals(users.get(0).getPrivateKey(),ResourcesLoader.getPrivateKey(login.getUsername(),login.getUsername() + login.getUsername()));
    }

    /**
     * User fails to login due to the fact that the userid does not exist.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws CryptoException
     * @throws UserNotExistException
     * @throws PasswordIsWrongException
     */
    @Test(expected = UserNotExistException.class)
    public void userDoesntExist() throws IOException, ClassNotFoundException, CryptoException, UserNotExistException, PasswordIsWrongException {

        clientManager.dummyPopulate(new ArrayList<>(),new ArrayList<>());
        Login login = new Login();

        login.setUsername(seller);
        login.setPassword((seller + seller).toCharArray());
        clientManager.login(login);

    }

    /**
     * User fails to login due to the fact the password inserted is wrong.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws CryptoException
     * @throws UserNotExistException
     * @throws PasswordIsWrongException
     */
    @Test(expected = PasswordIsWrongException.class)
    public void passwordIsWrong() throws IOException, ClassNotFoundException, CryptoException, UserNotExistException, PasswordIsWrongException {

        KeyPair keyPair = Crypto.generateRSAKeys();

        users.add(
                new User(seller, keyPair.getPublic())
        );

        clientManager.dummyPopulate(users,new ArrayList<>());
        Login login = new Login();

        login.setUsername(seller);
        login.setPassword(("wrongpassword").toCharArray());
        clientManager.login(login);

    }
}
