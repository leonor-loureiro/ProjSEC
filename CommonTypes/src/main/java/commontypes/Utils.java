package commontypes;

import crypto.Crypto;
import crypto.CryptoException;

import javax.crypto.KeyGenerator;
import java.io.*;
import java.lang.reflect.Array;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;

public class Utils {

    /**
     * This function is responsible for serializing an array list
     * @param arrayList array list to be serialized
     * @param filename file to store the serialized array list
     * @throws IOException
     */
    public static void serializeArrayList(ArrayList<?> arrayList, String filename) throws IOException {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(filename);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(arrayList);
        }finally {
            if(oos != null)
                oos.close();
            if(fos != null)
                fos.close();
        }
    }

    /**
     * This funtion is responsible for deserializing an array list
     * @param filename name of the file where the serialized object is stored
     * @return array list
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static ArrayList deserializeArrayList( String filename) throws IOException, ClassNotFoundException {
        ArrayList arrayList;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(filename);
            ois = new ObjectInputStream(fis);
            arrayList = (ArrayList) ois.readObject();
        }finally {
            ois.close();
            fis.close();
        }
        return arrayList;
    }

    public static ArrayList<User> initializeUsers(){
        ArrayList<User> users = new ArrayList<User>();
        try {
            users.add(new User("alice", 8081,Crypto.generateRSAKeys().getPrivate(), Crypto.generateRSAKeys().getPublic()));
            users.add(new User("bob", 8082,Crypto.generateRSAKeys().getPrivate(), Crypto.generateRSAKeys().getPublic()));
            users.add(new User("trudy", 8083,Crypto.generateRSAKeys().getPrivate(), Crypto.generateRSAKeys().getPublic()));
            users.add(new User("eve", 8084,Crypto.generateRSAKeys().getPrivate(), Crypto.generateRSAKeys().getPublic()));
        }catch(CryptoException e){
            e.printStackTrace();
        }
        return users;
    }

    public static ArrayList<Good> initializeGoods(){
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
        return goods;
    }
}
