package client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

class Client {

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
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

