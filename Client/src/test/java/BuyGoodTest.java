import client.Login;
import commontypes.Good;
import commontypes.User;
import commontypes.exception.GoodNotExistsException;
import commontypes.exception.UserNotExistException;
import crypto.Crypto;
import org.junit.Test;

import java.security.KeyPair;

public class BuyGoodTest extends ClientTests{

    /**
     * user sends a buy good requests and is unsuccessfull due to the good not existing in the system
     */
    @Test (expected = GoodNotExistsException.class)
    public void goodDoesNotExist() throws Exception {
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();


        users.add(
                new User(seller, keyPair.getPublic())
        );

        users.add(
                new User(buyer, keyPair2.getPublic())
        );

        goods.add(
                new Good("wronggood", seller, false)
        );

        clientManager.dummyPopulate(users, goods);

        Login login = new Login();

        login.setUsername(seller);
        login.setPassword((seller + seller).toCharArray());
        clientManager.login(login);

        clientManager.buyGood(buyer,goodID);

    }

    /**
     * user sends a buy good requests and is unsuccessful due to seller of the good not existing
     */
    @Test (expected = UserNotExistException.class)
    public void SellerDoesNotExist() throws Exception {
        KeyPair keyPair = Crypto.generateRSAKeys();
        KeyPair keyPair2 = Crypto.generateRSAKeys();


        users.add(
                new User(seller, keyPair.getPublic())
        );

        users.add(
                new User(buyer, keyPair2.getPublic())
        );

        goods.add(
                new Good(goodID, seller, false)
        );

        clientManager.dummyPopulate(users, goods);

        Login login = new Login();

        login.setUsername(seller);
        login.setPassword((seller + seller).toCharArray());
        clientManager.login(login);

        clientManager.buyGood("wronguser",goodID);

    }
}
