package client;

import communication.IMessageProcess;
import communication.Message;
import communication.RequestsReceiver;

import java.io.IOException;

public class ClientApp {
    public static void main(String[] args) {
        boolean running = true;

        Login login = null;

        RequestsReceiver commandReceiver = new RequestsReceiver();

        while(running) {

            UserInterface.home();

            try{

                UserInterface.welcome("TEST USER");

                while (running) {
                    if(login !=null) {
                        UserInterface.listCommands();
                        running = UserInterface.parseCommand();
                        UserInterface.clearScreen();
                    }
                    else{
                        login = UserInterface.requestLogin();
                        Manager.getInstance().startClient(login.getUsername());
                    }
                }

            }catch(Exception e){
                //e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }
}
