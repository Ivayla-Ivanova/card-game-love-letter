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

    private boolean haveJoinedGame;

    public ServerThread(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.output = new PrintWriter(clientSocket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // A Thread cannot start executing before entering a valid name
        this.name = enteringName();

        this.haveJoinedGame = false;


    }

    @Override
    public void run() {

        if (this.name == null) {

            this.output.close();
            this.interrupt();
            System.out.println("Client disconnected before entering the chat.");
        } else {
            this.setName(this.name);
            System.out.println("A new ServerThread started. ID: " + this.name);


            try {

                while (true) {

                    // Receiving messages from the client
                    String receivedMessage = input.readLine();


                    // Clean up after the client disconnects
                    if (receivedMessage == null) {
                        System.out.println(this.getName() + " interrupted.");
                        exitingGame();

                        server.removeServerThread(this);
                        server.removeName(this.getName());

                        String sendMessage = "%s left the room".formatted(this.getName());
                        sendToEveryone(sendMessage);

                        this.output.close();
                        this.interrupt();
                        break;
                    } else if(receivedMessage.isBlank()){
                        //Do nothing
                    } else if (receivedMessage.startsWith("@")) {
                        sendingPersonalMessage(receivedMessage);
                    } else if(receivedMessage.startsWith("$")){
                        sendingGameMessages(receivedMessage);
                    } else {
                        String sendMessage = this.getName() + ": " + receivedMessage;
                        sendToEveryone(sendMessage);
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
    private void sendToEveryone(String message) {

        for (ServerThread client : server.getServerThreadsList()) {

            sendingMessage(client, message);
        }
    }

    private void sendingToOwnClientMessage(String message){

        sendingMessage(this, message);

    }

    private void sendingPersonalMessage(String receivedMessage){

        String [] temp = receivedMessage.split(" ", 2);

        if (temp.length != 2) {

            String sendMessage = """
            Invalid request.
            Please send private messages by e.g. writing '@name message'!""";
            sendingToOwnClientMessage( sendMessage);
            return;
        }

        String addresseeName = temp[0].substring(1);

        if(!server.getNames().contains(addresseeName) || addresseeName.equals(this.name)){
            String sendMessage = """
            Invalid name.
            Please send private messages by e.g. writing '@name message'!""";
            sendingToOwnClientMessage(sendMessage);

        }
        if(temp[1].isBlank()){
            return;
        }
        String sendMessage = "[private] " + this.getName() + ": " + temp[1];



        for (ServerThread client : server.getServerThreadsList()) {

            if(client == this){
                continue;
            }
            if (client.getName().equals(addresseeName)) {
                sendingMessage(client, sendMessage);
            }
        }

    }

    private void sendingGameMessages(String receivedMessage){

        if(receivedMessage.substring(1).equals("joinGame")){

            joiningGame();

        }else if (receivedMessage.substring(1).equals("exitGame")){

            exitingGame();

        }else if(receivedMessage.substring(1).equals("startGame")){

            startingGame();

        }else {

            String sendMessage = "You have entered an invalid game command. Please try again.";
            sendingToOwnClientMessage(sendMessage);
        }

    }

    private void sendingToAllActivePlayersMessages(String message){

        for (ServerThread client : server.getActivePlayersList()) {

            sendingMessage(client, message);
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
                    sendingToOwnClientMessage(sendMessage);
                } else {
                    server.getNames().add(name);
                    break;
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String welcomeMessage = "Welcome " + name + "!\nIf you wish to join the game, please type $joinGame.";
        sendingToOwnClientMessage(welcomeMessage);

        //Informing other threads that a new client has joined

        sendToEveryone("%s joined the room".formatted(name));


        return name;
    }

    private void joiningGame(){

        try {

            if(server.getHasGameStarted()){
                String sendMessage = "The game has started. You cannot join it anymore.";
                sendingToOwnClientMessage(sendMessage);
                return;
            }

            if(haveJoinedGame == true){
                String sendMessage = "You have already joined the game.";
                sendingToOwnClientMessage(sendMessage);
                return;
            }

            if(server.increaseActivePlayerCount(this) == false){

                String sendMessage = "You were not able to join the game. Please try again later.";
                sendingToOwnClientMessage(sendMessage);
            } else {
                server.addToActivePlayersList(this);
                this.haveJoinedGame = true;
                String sendMessage = "You joined the game.\nTo exit the game type $exitGame.";
                sendingToOwnClientMessage(sendMessage);
                String sendToEveryoneMessage = this.name + " joined the game";
                for(ServerThread player : server.getActivePlayersList()){

                    if(player == this){
                        continue;
                    }

                    sendingMessage(player, sendToEveryoneMessage);
                }
                printGameMessagesToActivePlayers(server.getActivePlayerCount());

            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void exitingGame(){

        if(haveJoinedGame == false){
            String sendMessage = "You cannot exit the game if you have not yet joined it.";
            sendingToOwnClientMessage(sendMessage);
            return;
        }

        try {

            if(server.decreaseActivePlayerCount(this) == false){
                String sendMessage = "You were not able to exit the game.";
            } else {
                this.haveJoinedGame = false;
                server.removeFromActivePlayersList(this);
                String sendMessage = "You have exited the game.";
                sendingToOwnClientMessage(sendMessage);
                String sendToEveryoneMessage = this.name + " has exited the game";
                for(ServerThread player : server.getActivePlayersList()){

                    if(player == this){
                        continue;
                    }

                    sendingMessage(player, sendToEveryoneMessage);
                }
                if(server.getHasGameStarted() == false) {
                    printGameMessagesToActivePlayers(server.getActivePlayerCount());
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void startingGame(){

        if(!haveJoinedGame){
            String sendMessage = "You need to first join the game to start it.";
            sendingToOwnClientMessage(sendMessage);
            return;
        }

        if(server.getHasGameStarted()){
            String sendMessage = "The game has already started.";
            sendingToOwnClientMessage(sendMessage);
            return;
        }

        if(server.getActivePlayerCount() < 2){
            printGameMessagesToActivePlayers(server.getActivePlayerCount());
            return;
        }

        server.startingGame();
        String sendMessageToActivePlayers = "The game has started.";
        sendingToAllActivePlayersMessages(sendMessageToActivePlayers);
        String sendMessageToClients = "The game has started. You cannot join it anymore.";
        for(ServerThread client : server.getServerThreadsList()){

            if(server.getActivePlayersList().contains(client)){
                continue;
            }
            sendingMessage(client, sendMessageToClients);
        }

    }

    private void printGameMessagesToActivePlayers(int count){


        String countMessage;

        switch (count) {
            case 0:
                countMessage = "There needs to be at least two players to start the game.";
                sendingToAllActivePlayersMessages(countMessage);
                break;

            case 1:
                countMessage = "Waiting for at least one more player to start the game.";
                sendingToAllActivePlayersMessages(countMessage);
                break;
            case 2:
                countMessage = "You can start the game now by typing $startGame or wait for one or two more people to join.";
                sendingToAllActivePlayersMessages(countMessage);
                break;
            case 3:
                countMessage = "You can start the game now by typing $startGame or wait for one more person to join.";
                sendingToAllActivePlayersMessages(countMessage);
                break;
            case 4:
                countMessage = "You can start the game now by typing $startGame.";
                sendingToAllActivePlayersMessages(countMessage);
                break;
            default:
                // Do nothing
        }

    }


}


