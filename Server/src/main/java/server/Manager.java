package server;

import commontypes.Good;
import commontypes.User;
import communication.IMessageProcess;
import communication.Message;
import communication.RequestsReceiver;

import java.io.*;
import java.util.ArrayList;
import java.io.IOException;

import java.util.List;

public class Manager implements IMessageProcess {

    private static final String USERS_GOODS_MAPPING = "../../resources/goods_users";
    private static final String USERS_FILE = "../../resources/users_keys";

    static Manager manager = null;

    List<User> users;
    List<Good> goods;

    public static Manager getInstance(){
        if(manager == null)
            manager = new Manager();
        return manager;
    }


    private Manager(){
    }

    public void startServer(int port){
        RequestsReceiver requestReceiver = new RequestsReceiver();

        try {
            requestReceiver.initialize(port, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean intentionToSell(String goodID){
        return true;
    }

    public Good getStateOfGood(String goodID){
        return null;
    }

    public boolean transferGood(String goodID, String buyerID){
        Good good = findGood(goodID);
        if(!good.isOnSale())
            return false;
        //TODO validate transaction

        //Alter internal mapping of Goods->Users
        good.setUserID(buyerID);

        //TODO send back certification attesting validity of transaction

        return true;
    }

    private Good findGood(String goodID){
        for(Good good : goods)
            if(good.getGoodID().equals(goodID))
                return good;
        return null;
    }

    private User findUser(String userID){
        for (User user : users)
            if(user.getUserID().equals(userID))
                return user;
        return null;
    }

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


    public Message process(Message message) {
        switch (message.getOperation()) {
            case "intentiontosell":
                // TODO: return intentionToSell(message.getGoodID);, value must be a message
            case "getstateofgood":
            case "transfergood":
            case "findgood":
            case "finduser":

                default:
                    System.out.println("Operation Unknown!");
        }
        return null;
    }
}
