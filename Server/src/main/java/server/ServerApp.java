package server;

import commontypes.Good;
import commontypes.User;
import resourcesloader.ResourcesLoader;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.List;

public class ServerApp {
    public static void main(String args[]) {

        try {
            System.out.println(
                    "\n" + "\n" +
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
                    "                                                                         \n"
            .replace("\\", "\\\\"));

            String portnumber = args[0];

            String byzantineMode = args[1];

            Boolean isNotary = Integer.parseInt(args[2]) != 0;

            System.out.println(portnumber);
            Manager.getInstance().startServer(Integer.parseInt(portnumber), isNotary);
            
            if(byzantineMode.equals("true")) {
                Manager.setByzantine(true);
                System.out.println(Manager.getByzantine());
            }


            BufferedReader reader =  new BufferedReader(new InputStreamReader(System.in));
            System.out.println("press <enter> to shutdown");
            reader.readLine();

            // initializes server shutdown
            Manager.getInstance().closeServer();



        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
