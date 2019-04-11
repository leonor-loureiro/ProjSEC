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
import java.security.PrivateKey;
import java.util.ArrayList;


public class LoginTest extends ClientTests{

    @Test
    public void sucess() throws IOException, ClassNotFoundException, CryptoException, UserNotExistException, PasswordIsWrongException {
        KeyPair keyPair = Crypto.generateRSAKeys();

        users.add(
                new User(userID, keyPair.getPublic())
        );

        clientManager.dummyPopulate(users,new ArrayList<Good>());
        Login login = new Login();

        login.setUsername(userID);
        login.setPassword((userID + userID).toCharArray());
        clientManager.login(login);


        Assert.assertNotNull(clientManager.getUser());
        Assert.assertEquals(login.getUsername(),clientManager.getUser().getUserID());
        Assert.assertEquals(users.get(0).getPublicKey(),keyPair.getPublic());
        Assert.assertEquals(users.get(0).getPrivateKey(),ResourcesLoader.getPrivateKey(login.getUsername(),login.getUsername() + login.getUsername()));
    }
    @Test(expected = UserNotExistException.class)
    public void userDoesntExist() throws IOException, ClassNotFoundException, CryptoException, UserNotExistException, PasswordIsWrongException {

        clientManager.dummyPopulate(new ArrayList<>(),new ArrayList<>());
        Login login = new Login();

        login.setUsername(userID);
        login.setPassword((userID + userID).toCharArray());
        clientManager.login(login);

    }
    @Test(expected = PasswordIsWrongException.class)
    public void passwordIsWrong() throws IOException, ClassNotFoundException, CryptoException, UserNotExistException, PasswordIsWrongException {

        KeyPair keyPair = Crypto.generateRSAKeys();

        users.add(
                new User(userID, keyPair.getPublic())
        );

        clientManager.dummyPopulate(users,new ArrayList<>());
        Login login = new Login();

        login.setUsername(userID);
        login.setPassword(("wrongpassword").toCharArray());
        clientManager.login(login);

    }
}
