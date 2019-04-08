package client;

public class ClientApp {
    public static void main(String[] args) {
        boolean running = true;

        Login login = null;

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
                        if(ClientManager.getInstance().login(login))
                            ClientManager.getInstance().startClient(login);
                        else{
                            System.out.println("Insert a existing username");
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
