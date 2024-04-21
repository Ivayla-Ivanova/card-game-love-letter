package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread{

    private Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;

    private Server server;

    public ClientThread(Server server,Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        output = new PrintWriter(clientSocket.getOutputStream(), true);
        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));



    }

    @Override
    public void run() {
        System.out.println("A Thread started. ID: " + currentThread().getName());
        System.out.println("List from ClientThread: " + server.getClientThreadsList());

        enteringName(input, output);
        currentThread().
        try{

            while (true) {
                String receivedMessage = input.readLine();

                if (receivedMessage.equals("bye")) {
                    System.out.println(currentThread().getName() + " interrupted.");
                    server.removeClientThread(this);
                    for (ClientThread client : server.getClientThreadsList()) {

                        client.output.println("%s left the room".formatted(currentThread().getName()));
                    }

                    output.close();
                    this.interrupt();
                    break;
                } else {
                    sendToEveryone(receivedMessage);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendToEveryone(String receivedMessage) {
        for (ClientThread client : server.getClientThreadsList()) {

            if(client == this){
                continue;
            }

            client.output.println(currentThread().getName() + ": " + receivedMessage);
        }
    }

    private void enteringName(BufferedReader in, PrintWriter out){

        String name;

        out.println("Enter a name: ");

        try {
            name = in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        currentThread().setName(name);

        out.println("Welcome " + name + "!");

        for (ClientThread client : server.getClientThreadsList()) {

            if(client == this){
                continue;
            }
            client.output.println("%s joined the room".formatted(currentThread().getName()));
        }
    }
            }


