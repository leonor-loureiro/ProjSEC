package server;

public class ServerApp {
    public static void main(String args[]){
        Boolean running = true;

        while(running){
            Manager.getInstance().startServer(8080);
        }
    }
}
