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

public class NotaryServerTests {

    protected Manager notary = Manager.getInstance();

    protected ArrayList<User> users;
    protected ArrayList<Good> goods;

    protected Random random = new Random();

    protected PublicKey notaryPublicKey;

    protected String userID = "dummyUser";
    protected String goodID = "dummyGood";

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
