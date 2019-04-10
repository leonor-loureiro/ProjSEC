package client;

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

                        // if we had a correct login we cant initiliaze the client
                        if(ClientManager.getInstance().login(login)) {
                            System.out.println("Sucessful login");
                            ClientManager.getInstance().startClient(login);
                        }
                        else{
                            System.out.println("Insert correct information");
                            login = null;
                        }
                    }
                }

            }catch(Exception e){
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }
}
