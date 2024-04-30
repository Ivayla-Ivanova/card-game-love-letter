package server;

/**
 * Main class with the main method of the server application.
 */
public class ServerMain {

    /**
     *Main mathod of the server Main class.
     */
    public static void main(String[] args) {

        Server server = Server.getInstance();
        server.runServer();
    }
}
