package client;

import java.io.Console;
import java.util.Scanner;

public interface UserInterface {

    /**
     * reads input in a safe way, doesn't work with all consoles/terminals
     */
    Console input = System.console();

    /**
     * not as safe input reader but works with all consoles/terminals
     */
    Scanner scan = new Scanner(System.in);

    /*
    Class that handles client operations
     */
    ClientManager CLIENT_MANAGER = ClientManager.getInstance();


    static void home() {
        System.out.println("\n" +
                "    __  ______  _____    _   __      __                  \n" +
                "   / / / / __ \\/ ___/   / | / /___  / /_____ ________  __\n" +
                "  / /_/ / / / /\\__ \\   /  |/ / __ \\/ __/ __ `/ ___/ / / /\n" +
                " / __  / /_/ /___/ /  / /|  / /_/ / /_/ /_/ / /  / /_/ / \n" +
                "/_/ /_/_____//____/  /_/ |_/\\____/\\__/\\__,_/_/   \\__, /  \n" +
                "                                                /____/   \n".replace("\\", "\\\\"));
    }

    /**
     * list the commands available for the user
     */
    static void listCommands() {
        System.out.println();
        System.out.print("Available commands use initial letter of command: " + "\n"
        + "i for intention to sell" + "\n" + "g for get state of good" + "\n" + "b for buy good" + "\n" + "l for list goods");

        System.out.println();
    }

    /*
     * parses the commands that the user receives
     */
    static void parseCommand() {
        /*
        command inserted by the user
         */
        String command;

        System.out.println("Insert command:");

        command = scan.next();
        Command valueOfCommand;
        String goodID;
        String userName;
        try{
            valueOfCommand = Command.valueOf(command);
            switch(valueOfCommand){

                case i:
                    System.out.println("Insert the id  of the Good");
                    goodID = requestInput();
                    CLIENT_MANAGER.intentionToSell(goodID);
                    break;

                case g:
                    System.out.println("Insert the id  of the Good");
                    goodID = requestInput();
                    CLIENT_MANAGER.getStateOfGood(goodID);
                    break;

                case b:
                    System.out.println("Insert the id  of the Good");
                    goodID = requestInput();
                    System.out.println("Insert the name of the owner");
                    userName = requestInput();
                    CLIENT_MANAGER.buyGood(userName,goodID);
                    break;

                case l:
                    CLIENT_MANAGER.listGoods();
                    break;
                default:
                    System.out.println();
                    System.out.println("Unknown command");
                    break;
            }
        } catch(IllegalArgumentException e){
            System.out.println("Use one of the commands available");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        System.out.println();

    }

    /**
     * asks the user for the login information
     * @return
     */
    static Login requestLogin(){
        Login login = new Login();

        System.out.println();

        System.out.println("Please insert your information:");

        System.out.println("Username: ");

        String username = requestInput();

        if (!username.matches("[a-zA-Z0-9]*")) {
            System.out.println("A username can only contain letters and numbers");
            return null;
        }

        System.out.println("password: ");

        char[] password = requestSensibleInput();

        login.setUsername(username);
        login.setPassword(password);

        return login;
    }

    /**
     * request the user for input
     * attempts to use advanced input reading line, if not possible in current terminal
     * uses the scanner instead
     * @return string read input
     */
    static String requestInput(){
        String result;
        try{
            result = input.readLine();
        }catch(NullPointerException np){
            result = scan.next();
        }
        return result;
    }

    /**
     * request the user for input
     * attempts to use advanced input reading line, if not possible in current console uses the scanner instead
     * Stores in char[] instead of string due to java's unsafe string storage handling
     * @return char[] readinput
     */
    static char[] requestSensibleInput(){
        char[] result;
        try{
            result = input.readPassword();
        }catch(NullPointerException np){
            result = scan.next().toCharArray();
        }
        return result;
    }


    static void clearScreen(){
        for(int i = 0; i < 2; i++)
            System.out.println();
    }

}
