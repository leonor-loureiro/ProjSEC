import client.ClientManager;
import commontypes.Good;
import commontypes.User;
import crypto.Crypto;
import org.junit.After;
import org.junit.Before;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;

public class ClientTests {

    protected ClientManager clientManager = ClientManager.getInstance();

    protected ArrayList<User> users;
    protected ArrayList<Good> goods;

    protected PublicKey notaryPublicKey;

    protected String userID = "user1";
    protected String userID2 = "user2";

    protected String goodID = "good00";

    @Before
    public void setUp() throws Exception {
        users = new ArrayList<>();
        goods = new ArrayList<>();

        notaryPublicKey = Crypto.getPublicKey(
                "../Server/SEC-Keystore","notary","password".toCharArray()
        );
    }


}
