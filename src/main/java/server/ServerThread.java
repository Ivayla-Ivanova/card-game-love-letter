package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ServerThread extends Thread {

    private Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;

    private Server server;

    private String name;

    public ServerThread(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        output = new PrintWriter(clientSocket.getOutputStream(), true);
        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // A Thread cannot start executing before entering a valid name
        this.name = enteringName(input, output);


    }

    @Override
    public void run() {

        currentThread().setName(this.name);
        System.out.println("A Thread started. Current ID: " + currentThread().getName());


        try {

            while (true) {

                // Receiving messages from the client
                String receivedMessage = input.readLine();

                // Clean up after the client disconnects
                if (receivedMessage == null) {
                    System.out.println(currentThread().getName() + " interrupted.");
                    server.removeServerThread(this);
                    server.removeName(currentThread().getName());
                    for (ServerThread client : server.getServerThreadsList()) {

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

    // Forwarding received messages from the client to other threads
    private void sendToEveryone(String receivedMessage) {
        for (ServerThread client : server.getServerThreadsList()) {

            if (client == this) {
                continue;
            }

            client.output.println(currentThread().getName() + ": " + receivedMessage);
        }
    }

    private String enteringName(BufferedReader in, PrintWriter out) {

        String name;

        //Accepting an entered name and adding it to the set of names
        try {

            while (true) {
                name = in.readLine();
                if (server.getNames().contains(name) || name.isBlank()) {
                    out.println("This name is not available. Please enter another name: ");
                } else {
                    server.getNames().add(name);
                    break;
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        out.println("Welcome " + name + "!");

        //Informing other threads that a new client has joined
        for (ServerThread client : server.getServerThreadsList()) {

            if (client == this) {
                continue;
            }
            client.output.println("%s joined the room".formatted(name));
        }

        return name;
    }


}


