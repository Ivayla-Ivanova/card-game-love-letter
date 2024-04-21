package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ReceivingThread extends Thread {

    private Socket clientSocket;


    public ReceivingThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        System.out.println("ReceivingTread was created.");
    }

    public void run() {

        System.out.println("ReceivingThread started");

        try(BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            while (true) {
                String receivedMessage = input.readLine();
                System.out.println(receivedMessage);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
