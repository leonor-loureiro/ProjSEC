package client;

import communication.Communication;
import communication.Message;

import commontypes.User;
import java.io.Console;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

import static client.UserInterface.commandExec;
import static client.UserInterface.requestInput;

public class CommandExecution {

    private User user;

    Communication sendRequest = new Communication();

    public void setUser(User user) {this.user = user; }


    /**
     * Logs in the user into the system so network dependent operations can be completed
     * @param login the login information
     * @throws BadArgument if the input is invalid
     * @throws InvalidUser if the login information is incorrect
     */
    public void login(Login login){
            //throws BadArgument, InvalidUser {
        setUser(new User(login.getUsername(), login.getPort()));

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
    }


    /**
     * Register the given user to the service and generates his needed private information
     * @param login the login information
     */
    public void register(Login login){
            //throws BadArgument, UserAlreadyExists {
        //Create the user
        User user = new User(login.getUsername(), login.getPort());

        // Generate key pair
        KeyPair keyPair = null;
        try {
            //keyPair = Crypto.generateRSAKeys();
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

    public void intentionToSell() throws IOException, ClassNotFoundException {
        Message msg = new Message();
        msg.setOperation("Intention to Sell");
        sendRequest.sendMessage("localhost",8081,msg);
    }

    public void getStateOfGood() throws IOException, ClassNotFoundException {
        Message msg = new Message();
        msg.setOperation("getstateofgood");
        sendRequest.sendMessage("localhost",8081,msg);
    }

    public void buyGood() throws IOException, ClassNotFoundException {
        Message msg = new Message();
        msg.setOperation("buygood");
        System.out.println("Insert the other lad");
        int porttosend = Integer.parseInt(requestInput());
        sendRequest.sendMessage("localhost",porttosend,msg);
    }

    public void listGoods() throws IOException, ClassNotFoundException {
        Message msg = new Message();
        msg.setOperation("List Goods");
        sendRequest.sendMessage("localhost",8081,msg);

    }

}
