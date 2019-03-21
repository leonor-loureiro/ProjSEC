package client;
public class ClientApp {
    public static void main(String[] args) {
        boolean running = true;

        boolean logged = false;

        Login login = new Login();

        login.setUsername("admin");
        login.setPassword("admin1".toCharArray());

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
                        login = UserInterface.parseCommandLoginOrRegister();
                    }
                }

            }catch(Exception e){
                //e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }
}
