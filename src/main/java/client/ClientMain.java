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

        Scanner scanner = new Scanner(System.in);

        System.out.println("Do you want to run this application as a server or as a client?");
        System.out.println("Type 'server' or 'client'.");

        String input = scanner.nextLine().trim().toLowerCase();

        if (input.equals("client")) {

            Client client = new Client();

        } else{
            Server server = Server.getInstance();
            if(server.getServerSocket() == null){
                System.out.println("There is a running server.");
                System.out.println("Now you are running as a client.");
                Client client = new Client();
                return;
            }
            server.runServer();
        }






    }
}
