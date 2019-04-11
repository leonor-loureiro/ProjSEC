package client;

import commontypes.exception.PasswordIsWrongException;
import commontypes.exception.UserNotExistException;
import crypto.CryptoException;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class ClientApp {
    public static void main(String[] args) {

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
                        // while we dont have a elegible login ask for another one
                        login = UserInterface.requestLogin();

                        // if we had a correct login we can initiliaze the client
                        try {
                            ClientManager.getInstance().login(login);
                            System.out.println("Sucessful login");
                            ClientManager.getInstance().startClient(login);
                        }catch (PasswordIsWrongException e){
                            System.out.println("Insert correct information");
                            login = null;
                        } catch (UserNotExistException e) {
                            System.out.println("Insert correct information");
                            login = null;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (CryptoException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
