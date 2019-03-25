package server;

import commontypes.Good;
import commontypes.User;
import communication.IMessageProcess;
import communication.Message;
import communication.RequestsReceiver;

import java.io.IOException;
import java.util.List;

public class Manager implements IMessageProcess {

    private static final String USERS_GOODS_MAPPING = "../../resources/goods-users-mapping";
    private static final String USERS_FILE = "../../resources/users";

    List<User> users;
    List<Good> goods;



    public Manager(int port){
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
