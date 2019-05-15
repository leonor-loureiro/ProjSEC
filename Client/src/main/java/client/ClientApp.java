package client;

import commontypes.exception.GoodNotExistsException;
import commontypes.exception.PasswordIsWrongException;
import commontypes.exception.UserNotExistException;
import communication.ByzantineSimulator;
import crypto.CryptoException;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class ClientApp {
    public static void main(String[] args) throws CryptoException {

        /*
         variable to control running of app
         */
        boolean running = true;

        /*
        Login info of the client
         */
        Login login = null;

        while(running) {

            UserInterface.home();

            try{

                while (running) {

                    //if we already have a login ask for input
                    if(login !=null) {
                        UserInterface.listCommands();
                        UserInterface.parseCommand();
                        UserInterface.clearScreen();
                    }
                    else{
                        // while we don't have a valid login ask for another one
                        login = UserInterface.requestLogin();

                        // if we had a correct login we can initialize the client
                        try {
                            ClientManager.getInstance().login(login);
                            System.out.println("Successful login");
                            if(args[0].equals("true")){
                                ClientManager.getInstance().startClient(login);
                                ByzantineSimulator.setByzantine(true);
                                System.out.println("Byzantine Mode is " + ByzantineSimulator.getByzantine());
                            }
                            else if(args[1].equals("true")){
                                ClientManager.getInstance().startClient(login);
                                ByzantineSimulator.setIsDoubleBroadcast(true);
                                System.out.println("DoubleBroadcast Byzantine Mode is " + ByzantineSimulator.getIsDoubleBroadcast());
                            }
                            else
                                ClientManager.getInstance().startClient(login);
                        }catch (PasswordIsWrongException | UserNotExistException e){
                            System.out.println("Insert correct information");
                            login = null;
                        }
                    }
                }
            } catch (IOException | CertificateException | KeyStoreException |
                    NoSuchAlgorithmException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
