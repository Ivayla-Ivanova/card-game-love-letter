package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class UserThread extends Thread{

    private Socket clientSocket;
    private Scanner scanner;

    public UserThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.scanner = new Scanner(System.in);
    }

    public void run() {

        try(PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {

            while (true) {
                    String message = scanner.nextLine();
                    if (message.equals("bye")) {
                        System.exit(0);
                    }
                    output.println(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    }
