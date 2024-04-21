package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private static final String hostname = "localhost";
    private static final int port = 5000;
    private Scanner scanner;

    private PrintWriter output;

    public Client() {

        this.scanner = new Scanner(System.in);

        try (Socket clientSocket = new Socket(this.hostname,this.port)){
            System.out.println("Connected to server successfully!");
            System.out.println("Enter a name: ");
            ReceivingThread receivingThread = new ReceivingThread(clientSocket);
            receivingThread.start();

            output = new PrintWriter(clientSocket.getOutputStream(), true);
            while (true) {
                String message = scanner.nextLine();
                output.println(message);
                if (message.equals("bye")) {
                    System.exit(0);
                }
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

