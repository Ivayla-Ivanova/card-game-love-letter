package client;

import server.Server;

import java.util.Scanner;

/**
 * Main class with the main method of the client application.
 */
public class ClientMain {

    /**
     * Main method of the client application.
     */
    public static void main(String[] args) {

        Server server = Server.getInstance();
        if(server.getServerSocket() == null){
            Client client = new Client();
            return;
        }
        server.runServer();



    }
}
