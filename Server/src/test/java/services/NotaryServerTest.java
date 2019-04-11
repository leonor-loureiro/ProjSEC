package services;

import commontypes.Good;
import commontypes.User;
import crypto.Crypto;
import org.junit.After;
import org.junit.Before;
import pteidlib.PteidException;
import server.Manager;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;

public class NotaryServerTest {

    Manager notary = Manager.getInstance();

    ArrayList<User> users;
    ArrayList<Good> goods;

    Random random = new Random();

    PublicKey notaryPublicKey;

    String userID = "dummyUser";
    String goodID = "dummyGood";

    @Before
    public void setUp() throws Exception {
        users = new ArrayList<>();
        goods = new ArrayList<>();
        notaryPublicKey = Crypto.getPublicKey(
                "SEC-Keystore","notary","password".toCharArray()
        );
    }

    @After
    public void cleanUp() throws PteidException {
        notary.closeServer();
    }
}
