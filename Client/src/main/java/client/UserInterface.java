package client;

import java.io.Console;
import java.util.EnumSet;
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

    /**
     *
     */
    CommandExecution commandExec = new CommandExecution();

    public static void welcome(String test_user) {
    }

    public static void home() {
        System.out.println("\n" +
                "    __  ______  _____    _   __      __                  \n" +
                "   / / / / __ \\/ ___/   / | / /___  / /_____ ________  __\n" +
                "  / /_/ / / / /\\__ \\   /  |/ / __ \\/ __/ __ `/ ___/ / / /\n" +
                " / __  / /_/ /___/ /  / /|  / /_/ / /_/ /_/ / /  / /_/ / \n" +
                "/_/ /_/_____//____/  /_/ |_/\\____/\\__/\\__,_/_/   \\__, /  \n" +
                "                                                /____/   \n".replace("\\", "\\\\"));
    }

    public static void listCommands() {
        System.out.println();
        System.out.print("Available commands use initial letter of command: " + "\n"
        + "i for intention so sell" + "\n" + "g for get state of good" + "\n" + "b for buy good" + "\n" + "l for list goods");

        System.out.print("  ");
       /* EnumSet.allOf(Command.class)
                .forEach(command -> System.out.print(" " + command)); */

        System.out.println();
    }

    static Login parseCommandLoginOrRegister() {
        String command;
        System.out.println("r for register");
        System.out.println("log for login");

        command = scan.next();
        Command c;
        try{
            c = Command.valueOf(command);
            switch(c){

                case r:
                    return requestLoginOrRegister("register");

                case log:
                    return requestLoginOrRegister("login");

                default:
                    System.out.println();
                    System.out.println("Unknown command");
                    break;
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        System.out.println();
        return null;

    }

    public static boolean parseCommand() {
        String command;
        System.out.println("Insert command:");

        command = scan.next();
        Command c;
        try{
            c = Command.valueOf(command);
            switch(c){

                case i:
                    commandExec.intentionToSell();
                    break;

                case g:
                    commandExec.getStateOfGood();
                    break;

                case b:
                    commandExec.buyGood();
                    break;

                case l:
                    commandExec.listGoods();
                    break;

                default:
                    System.out.println();
                    System.out.println("Unknown command");
                    break;
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        System.out.println();
        return true;

    }

    static Login requestLoginOrRegister(String intention){
            //throws InvalidUser, BadArgument, UserAlreadyExists {
        Login login = new Login();

        System.out.println();
        String loginOrRegister = intention;

        if(!(loginOrRegister.equals("login") || loginOrRegister.equals("register")))
            return null;


        System.out.println();
        System.out.println("Please insert your information:");

        System.out.println("Username: ");

        String username = requestInput();

        if (!username.matches("[a-zA-Z0-9]*")) {
            System.out.println("A username can only contain letters and numbers");
            return null;
        }
        login.setUsername(username);


        System.out.println("Password: ");
        login.setPassword(requestSensibleInput());


        System.out.println();

        if(loginOrRegister.equals("login"))
            commandExec.login(login);
        else{
            if(login.getPassword().length < 6){
                System.out.println("Please use a password with 6 or more characters");
                login.setPassword(requestSensibleInput());
            }
            commandExec.register(login);
        }
        login.setUsername(username);
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

    /**
     * fills the screen with blank spaces
     */
    static void clearScreen(){
        for(int i = 0; i < 2; i++)
            System.out.println();
    }

    static boolean validateInputNames(String input){
        return false;
    }
}
