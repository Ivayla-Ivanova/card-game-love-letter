package server;

import client.Client;

import java.util.Scanner;

/**
 * Main class with the main method of the server application.
 */
public class ServerMain {

    /**
     *Main mathod of the server Main class.
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

