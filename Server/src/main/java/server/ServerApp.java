package server;

import commontypes.Good;
import commontypes.User;
import resourcesloader.ResourcesLoader;
import java.util.List;

public class ServerApp {
    public static void main(String args[]) {

        try {
            Boolean running = true;

            ResourcesLoader rsl = new ResourcesLoader();

            List<User> list = null;
            List<Good> goodList = null;

            list = rsl.loadUserList();
            goodList = rsl.loadGoodsList();

            for (User user : list) {
                System.out.println(user.getUserID());
            }

            for (Good good : goodList) {
                System.out.println(good.getGoodID());
            }
            rsl.loadUserCertificate("user1", "user1user1");
            rsl.getPrivateKey("user1", "user1user1");

            while (running) {
                Manager.getInstance().startServer(8080);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
