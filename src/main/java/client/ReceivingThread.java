package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * This class represents a thread for receiving messages from the server and displaying them to the user.
 */
public class ReceivingThread extends Thread {

    private Socket clientSocket;

    ReceivingThread(Socket clientSocket) {
        this.clientSocket = clientSocket;

    }

    /**
     * run method of the ReceivingThread.
     */
    public void run() {

        try(BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            while (true) {
                String receivedMessage = input.readLine();
                    System.out.println(receivedMessage);
            }
        } catch (IOException e) {
            System.out.println("Failed to create input reader.");
        }
    }
}
