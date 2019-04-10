import commontypes.Good;
import commontypes.Utils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.KeyGenerator;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * This class implements the tests for the serialization of the objects
 */
public class SerializationTests {

    private static final String FILENAME = "goods_users";

    @Before
    public void setUp() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
    }



    @Test
    public void serializeGoodsSuccess() throws IOException, ClassNotFoundException {
        ArrayList<Good> goods = new ArrayList();
        goods.add(new Good("diamond", "alice", false));
        goods.add(new Good("gold", "bob", false));
        goods.add(new Good("platinum", "trudy", false));
        goods.add(new Good("silver", "eve", false));
        goods.add(new Good("bronze", "alice", false));
        goods.add(new Good("amethyst", "bob", false));
        goods.add(new Good("sapphire", "trudy", false));
        goods.add(new Good("ruby", "eve", false));
        goods.add(new Good("amber", "alice", false));
        goods.add(new Good("pearl", "bob", false));
        goods.add(new Good("jade", "trudy", false));
        goods.add(new Good("emerald", "eve", false));
        goods.add(new Good("turquoise", "alice", false));
        goods.add(new Good("serpentine", "bob", false));
        goods.add(new Good("quartz", "trudy", false));
        goods.add(new Good("limestone", "eve", false));

        Utils.serializeArrayList(goods, FILENAME);
        ArrayList goods1 = Utils.deserializeArrayList(FILENAME);
        Assert.assertEquals(goods1, goods);
    }

    @After
    public void cleanUp(){
        File file = new File(FILENAME);
        file.delete();
    }
}
