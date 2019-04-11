import commontypes.Good;
import commontypes.Utils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import server.data.AtomicFileManager;

import javax.rmi.CORBA.Util;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AtomicFileManagerTest {
    private ArrayList<Good> goods = new ArrayList<>();
    private static final String FILENAME = "testFile.test";

    @Test
    public void successAtomicSerialize() throws IOException, ClassNotFoundException {
        goods.add(new Good("diamond", "alice", false));
        goods.add(new Good("gold", "bob", false));
        goods.add(new Good("platinum", "trudy", false));

        AtomicFileManager.atomicWriteObjectToFile(FILENAME, goods);
        Assert.assertEquals(goods, Utils.deserializeArrayList(FILENAME));
    }

    @After
    public void cleanUp(){
        File file = new File(FILENAME);
        file.delete();
    }


}
