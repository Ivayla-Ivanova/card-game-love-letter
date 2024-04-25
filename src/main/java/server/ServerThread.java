package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import server.game.cards.Card;

public class ServerThread extends Thread {

    //ServerThread attributes

    private Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;

    private Server server;

    private String name;

    private boolean haveJoinedGame;

    //Player attributes

    private Hand hand;
    private ArrayList<Card> discardPile;
    private int tokens;
    private boolean isInRound;
    private boolean wonLastRound;
    private boolean isOnTurn;
    private Card playedCard;
    private int daysSinceLastDate;

    private static Random randomGenerator = new Random();

    public ServerThread(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.output = new PrintWriter(clientSocket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // A Thread cannot start executing before entering a valid name
        this.name = enteringName();
        this.haveJoinedGame = false;

        //Player
        resetPlayerAttributes();


    }

    //Player methods
    public void setIsInRound(boolean value){
        this.isInRound = value;
    }

    public void resetPlayerAttributes(){

        this.hand = new Hand();
        this.discardPile = new ArrayList<>();
        this.tokens = 0;
        this.isInRound = false;
        this.wonLastRound = false;
        this.isOnTurn = false;
        this.playedCard = null;
        this.daysSinceLastDate = randomGenerator.nextInt(366);

    }

    public int getDaysSinceLastDate(){
        return this.daysSinceLastDate;
    }

    public boolean getWonLastRound(){
        return this.wonLastRound;
    }

    public void setIsOnTurn(boolean value){
        this.isOnTurn = value;
    }

    public Hand getHand(){
        return this.hand;
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
                        sendingMessageToEveryone(server.getServerThreadsList(), sendMessage);

                        this.output.close();
                        this.interrupt();
                        break;
                    } else if(receivedMessage.isBlank()){
                        //Do nothing
                    } else if (receivedMessage.startsWith("@")) {
                        sendingPersonalMessage(receivedMessage);
                    } else if(receivedMessage.startsWith("$")){
                        sendingGameMessage(receivedMessage);
                    } else {
                        String sendMessage = this.getName() + ": " + receivedMessage;
                        sendingMessageToEveryone(server.getServerThreadsList(), sendMessage);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendingMessageToOneClient(ServerThread client, String message){

        client.output.println(message);
    }

    // Forwarding received messages from the client to other threads
    public void sendingMessageToEveryone(List<ServerThread> listOfClients, String message) {

        for (ServerThread client : listOfClients) {

            sendingMessageToOneClient(client, message);
        }
    }

    public void sendingMessageToOwnClient(String message){

        sendingMessageToOneClient(this, message);

    }

    private void sendingPersonalMessage(String receivedMessage){

        String [] temp = receivedMessage.split(" ", 2);

        if (temp.length != 2) {

            String sendMessage = """
            Invalid request.
            Please send private messages by e.g. writing '@name message'!""";
            sendingMessageToOwnClient( sendMessage);
            return;
        }

        String addresseeName = temp[0].substring(1);

        if(!server.getNames().contains(addresseeName) || addresseeName.equals(this.name)){
            String sendMessage = """
            Invalid name.
            Please send private messages by e.g. writing '@name message'!""";
            sendingMessageToOwnClient(sendMessage);

        }
        if(temp[1].isBlank()){
            return;
        }
        String sendMessage = "[private] " + this.getName() + ": " + temp[1];



        for (ServerThread client : server.getServerThreadsList()) {

            if (client.getName().equals(addresseeName)) {
                sendingMessageToOneClient(client, sendMessage);
            }
        }

    }

    private void sendingGameMessage(String receivedMessage){

        if(receivedMessage.substring(1).equals("joinGame")){

            joiningGame();

        }else if (receivedMessage.substring(1).equals("exitGame")){

            exitingGame();

        }else if(receivedMessage.substring(1).equals("startGame")){

            startingGame();

        }else if(receivedMessage.substring(1).equals("help")){

            printCardDescription();

        }else {

            String sendMessage = "You have entered an invalid game command. Please try again.";
            sendingMessageToOwnClient(sendMessage);
        }

    }
    public void sendingToAllPlayersExceptMe(String message){

        for (ServerThread client : server.getActivePlayersList()) {

            if(client == this){
                continue;
            }

            sendingMessageToOneClient(client, message);
        }

    }
    private void printCardDescription() {
        String description = """ 
                8-Princess (1): Lose if discarded.
                7-Countess (1): Must be played if you have Kind or Prince in hand.
                6-King (1): Trade hands with another player.
                5-Prince (2): Choose another player. They discard their hand and draw a new card.
                4-Handmaid (2): You cannot be chosen until your next turn.
                3-Baron (2): Compare hands with another player; lower number is out.
                2-Priest (2): Look at a player´s hand.
                1-Guard (2): Guess a player´s hand; if correct the player is out.
                """;
        sendingMessageToOwnClient(description);
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
                    sendingMessageToOwnClient(sendMessage);
                } else {
                    server.getNames().add(name);
                    break;
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String welcomeMessage = "Welcome " + name + "!\nIf you wish to join the game, please type $joinGame.";
        sendingMessageToOwnClient(welcomeMessage);

        //Informing other threads that a new client has joined

        sendingMessageToEveryone(server.getServerThreadsList(), "%s joined the room".formatted(name));


        return name;
    }

    private void joiningGame(){

        try {

            if(server.getHasGameStarted()){
                String sendMessage = "The game has started. You cannot join it anymore.";
                sendingMessageToOwnClient(sendMessage);
                return;
            }

            if(haveJoinedGame == true){
                String sendMessage = "You have already joined the game.";
                sendingMessageToOwnClient(sendMessage);
                return;
            }

            if(server.increaseActivePlayerCount(this) == false){

                String sendMessage = "You were not able to join the game. Please try again later.";
                sendingMessageToOwnClient(sendMessage);
            } else {
                server.addToActivePlayersList(this);
                this.haveJoinedGame = true;
                String sendMessage = "You joined the game.\nTo exit the game type $exitGame.";
                sendingMessageToOwnClient(sendMessage);
                String sendToEveryoneMessage = this.name + " joined the game";
                for(ServerThread player : server.getActivePlayersList()){

                    if(player == this){
                        continue;
                    }

                    sendingMessageToOneClient(player, sendToEveryoneMessage);
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
            sendingMessageToOwnClient(sendMessage);
            return;
        }

        try {

            if(server.decreaseActivePlayerCount(this) == false){
                String sendMessage = "You were not able to exit the game.";
            } else {
                this.haveJoinedGame = false;
                server.removeFromActivePlayersList(this);
                String sendMessage = "You have exited the game.";
                sendingMessageToOwnClient(sendMessage);
                String sendToEveryoneMessage = this.name + " has exited the game";
                for(ServerThread player : server.getActivePlayersList()){

                    if(player == this){
                        continue;
                    }

                    sendingMessageToOneClient(player, sendToEveryoneMessage);
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
            sendingMessageToOwnClient(sendMessage);
            return;
        }

        if(server.getHasGameStarted()){
            String sendMessage = "The game has already started.";
            sendingMessageToOwnClient(sendMessage);
            return;
        }

        if(server.getActivePlayerCount() < 2){
            printGameMessagesToActivePlayers(server.getActivePlayerCount());
            return;
        }

        server.startingGame();

        sendingMessageToEveryone(server.getActivePlayersList(), "The game has started. You are playing now!\n" +
                "To see the description of the cards, enter $help. ");
        for(ServerThread client : server.getServerThreadsList()){

            if(server.getActivePlayersList().contains(client)){
                continue;
            }
            sendingMessageToOneClient(client, "The game has started. You cannot join it anymore.");

        }

        for(ServerThread player : server.getActivePlayersList()){
            String sendMessage = "Last time you went on a date was "
                    + player.getDaysSinceLastDate()+" days ago!";
            sendingMessageToOneClient(player, sendMessage);
        }

        server.getGame().startRound(this);


    }

    private void printGameMessagesToActivePlayers(int count){


        String countMessage;

        switch (count) {
            case 0:
                countMessage = "There needs to be at least two players to start the game.";
                sendingMessageToEveryone(server.getActivePlayersList(), countMessage);
                break;

            case 1:
                countMessage = "Waiting for at least one more player to start the game.";
                sendingMessageToEveryone(server.getActivePlayersList(), countMessage);
                break;
            case 2:
                countMessage = "You can start the game now by typing $startGame or wait for one or two more players to join.";
                sendingMessageToEveryone(server.getActivePlayersList(), countMessage);
                break;
            case 3:
                countMessage = "You can start the game now by typing $startGame or wait for one more player to join.";
                sendingMessageToEveryone(server.getActivePlayersList(), countMessage);
                break;
            case 4:
                countMessage = "You can start the game now by typing $startGame.";
                sendingMessageToEveryone(server.getActivePlayersList(), countMessage);
                break;
            default:
                // Do nothing
        }

    }


}


