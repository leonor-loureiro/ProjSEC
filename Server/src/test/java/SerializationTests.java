import commontypes.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import server.Manager;

import javax.crypto.KeyGenerator;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * This class implements the tests for the serialization of the objects
 */
public class SerializationTests {

    private ArrayList<User> users;
    private Manager manager = Manager.getInstance();
    private static final String FILENAME = "users_keys";

    @Before
    public void setUp() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        users = new ArrayList<User>();
        users.add(new User("alice", keyGen.generateKey()));
        users.add(new User("bob", keyGen.generateKey()));
        users.add(new User("trudy", keyGen.generateKey()));
        users.add(new User("eve", keyGen.generateKey()));
    }

    @Test
    public void serializeUsersSuccess() throws IOException, ClassNotFoundException {
        manager.serializeArrayList(users, FILENAME);
        ArrayList users1 = manager.deserializeArrayList(FILENAME);
        Assert.assertEquals(users1, users);
    }

    @After
    public void cleanUp(){
        File file = new File(FILENAME);
        file.delete();
    }
}
