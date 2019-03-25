package client;

import communication.Communication;

import java.io.Console;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

import static client.UserInterface.commandExec;

public class CommandExecution {

    private User user;


    public void setUser(User user) {this.user = user; }


    /**
     * Logs in the user into the system so network dependent operations can be completed
     * @param login the login information
     * @throws BadArgument if the input is invalid
     * @throws InvalidUser if the login information is incorrect
     */
    public void login(Login login){
            //throws BadArgument, InvalidUser {
        setUser(new User(login.getUsername(), login.getPassword()));

        // Tries to login at auth server
     /*   if(!communication.login(user)){
            System.out.println("Username or password are invalid");
            return;
        } */

        PublicKey publicKey = null;
        PrivateKey privateKey = null;

        // Extract public key and private keys


        // set public and private keys
        user.setPublicKey(publicKey);
        user.setPrivateKey(privateKey);
    }


    /**
     * Register the given user to the service and generates his needed private information
     * @param login the login information
     */
    public void register(Login login){
            //throws BadArgument, UserAlreadyExists {
        //Create the user
        User user = new User(login.getUsername(), login.getPassword());

        // Generate key pair
        KeyPair keyPair = null;
        try {
            //keyPair = Crypto.generateRSAKeys();
            user.setPrivateKey(keyPair.getPrivate());
            user.setPublicKey(keyPair.getPublic());

        } catch (Exception e) { //trocar
            e.printStackTrace();
            return;
        }

        // Send register request
        setUser(user);
      /*  if(!communication.register(user)){
            return;
        } */

    }

    public void intentionToSell() {
    }

    public void getStateOfGood() {
    }

    public void buyGood() {
    }

    public void listGoods() {

    }

}
