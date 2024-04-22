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
        this.output = new PrintWriter(clientSocket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // A Thread cannot start executing before entering a valid name
        this.name = enteringName();


    }

    @Override
    public void run() {

        if (this.name == null) {

            this.output.close();
            this.interrupt();
            System.out.println("Client disconnected before entering the chat.");
        } else {
            this.setName(this.name);
            System.out.println("A new Thread started. ID: " + currentThread().getName());


            try {

                while (true) {

                    // Receiving messages from the client
                    String receivedMessage = input.readLine();


                    // Clean up after the client disconnects
                    if (receivedMessage == null) {
                        System.out.println(this.getName() + " interrupted.");
                        server.removeServerThread(this);
                        server.removeName(this.getName());
                        for (ServerThread client : server.getServerThreadsList()) {

                            sendingMessage(client, "%s left the room".formatted(this.getName()));
                        }

                        this.output.close();
                        this.interrupt();
                        break;
                    }

                    if(receivedMessage.isBlank()){
                        continue;
                    }

                    if (receivedMessage.startsWith("@")) {
                        sendingPersonalMessage(receivedMessage);
                    } else {
                        sendToEveryone(receivedMessage);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void sendingMessage(ServerThread client, String message){

        client.output.println(message);
    }

    // Forwarding received messages from the client to other threads
    private void sendToEveryone(String receivedMessage) {

        String sendMessage = this.getName() + ": " + receivedMessage;

        for (ServerThread client : server.getServerThreadsList()) {

            if (client == this) {
                continue;
            }

            sendingMessage(client, sendMessage);
        }
    }

    private void sendingPersonalMessage(String receivedMessage){

        String [] temp = receivedMessage.split(" ", 2);

        if (temp.length != 2) {

            String sendMessage = """
            Invalid request.
            Please send private messages by e.g. writing '@name message'!""";
            sendingMessage(this, sendMessage);
            return;
        }

        String addresseeName = temp[0].substring(1);

        if(!server.getNames().contains(addresseeName) || addresseeName.equals(this.name)){
            String sendMessage = """
            Invalid name.
            Please send private messages by e.g. writing '@name message'!""";
            sendingMessage(this, sendMessage);

        }
        String sendMessage = "[private] " + this.getName() + ": "
                + receivedMessage.substring(receivedMessage.indexOf(" ") + 1);


        for (ServerThread client : server.getServerThreadsList()) {

            if(client == this){
                continue;
            }
            if (client.getName().equals(addresseeName)) {
                sendingMessage(client, sendMessage);
            }
        }

    }

    private String enteringName() {

        String name;

        //Accepting an entered name and adding it to the set of names
        try {

            while (true) {
                name = this.input.readLine();

                if(name == null){
                    return null;
                }

                if (server.getNames().contains(name) || name.isBlank()) {
                    String sendMessage = "This name is not available. Please enter another name: ";
                    sendingMessage(this, sendMessage);
                } else {
                    server.getNames().add(name);
                    break;
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sendingMessage(this, "Welcome " + name + "!");

        //Informing other threads that a new client has joined
        for (ServerThread client : server.getServerThreadsList()) {

            if (client == this) {
                continue;
            }
            sendingMessage(client, "%s joined the room".formatted(name));
        }

        return name;
    }


}


