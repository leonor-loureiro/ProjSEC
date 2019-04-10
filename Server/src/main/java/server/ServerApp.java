package server;

import commontypes.Good;
import commontypes.User;
import resourcesloader.ResourcesLoader;
import java.util.List;

public class ServerApp {
    public static void main(String args[]) {

        try {
            System.out.println("\n" + "\n" +
                    "  _   _   ____    ____      _   _           _                            \n" +
                    " | | | | |  _ \\  / ___|    | \\ | |   ___   | |_    __ _   _ __   _   _   \n" +
                    " | |_| | | | | | \\___ \\    |  \\| |  / _ \\  | __|  / _` | | '__| | | | |  \n" +
                    " |  _  | | |_| |  ___) |   | |\\  | | (_) | | |_  | (_| | | |    | |_| |  \n" +
                    " |_| |_| |____/  |____/    |_| \\_|  \\___/   \\__|  \\__,_| |_|     \\__, |  \n" +
                    "          ____                                                   |___/   \n" +
                    "         / ___|    ___   _ __  __   __   ___   _ __                      \n" +
                    "         \\___ \\   / _ \\ | '__| \\ \\ / /  / _ \\ | '__|                     \n" +
                    "          ___) | |  __/ | |     \\ V /  |  __/ | |                        \n" +
                    "         |____/   \\___| |_|      \\_/    \\___| |_|                        \n" +
                    "                                                                         \n".replace("\\", "\\\\"));
            Boolean running = true;
            while (running) {
                Manager.getInstance().startServer(8080);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
