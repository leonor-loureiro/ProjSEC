package client;

import commontypes.User;
import commontypes.Good;
import communication.IMessageProcess;
import communication.Message;
import communication.RequestsReceiver;

import java.io.*;
import java.util.ArrayList;
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

            requestReceiver.initializeInNewThread(port, this);
    }


    public boolean intentionToSell(String goodID){
        return true;
    }

    public Good getStateOfGood(String goodID){
        return null;
    }

    public boolean transferGood(String goodID, String buyerID){
        Good good = findGood(goodID);
        if(!good.isForSale())
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



    public Message process(Message message) {
        switch (message.getOperation()) {
            case INTENTION_TO_SELL:
                // TODO: return intentionToSell(message.getGoodID);, value must be a message
                System.out.println("Received intentiontosell");
                return message;
            case GET_STATE_OF_GOOD:
                System.out.println("Received getstateofgood");
                return message;
            case BUY_GOOD:
                System.out.println("Received buygood");
                return message;
            default:
                System.out.println("Operation Unknown!");
        }
        return null;
    }
}
