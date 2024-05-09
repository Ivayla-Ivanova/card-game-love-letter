package client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * The Client class represents a server application.
 * It handles communication with the server.
 * Start threads for sending and receiving messages.
 */
public class Client {

    private static final String hostname = "localhost";
    private static final int port = 5000;

    public Client() {

        try{
            Socket clientSocket = new Socket(hostname, port);
            System.out.println("Enter 'bye' to exit \nEnter a name: ");

            ReceivingThread receivingThread = new ReceivingThread(clientSocket);
            receivingThread.start();
            UserThread userThread = new UserThread(clientSocket);
            userThread.start();

        } catch (UnknownHostException e) {
            System.out.println("Unknown host '" + hostname + "'.");
        } catch (IOException e) {
            System.out.println("Failed to connect. There is no running server.");
        }
    }


}

