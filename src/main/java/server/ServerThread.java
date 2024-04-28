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
    private boolean hasJoinedGame;

    //Player attributes
    private Hand hand;
    private ArrayList<Card> discardPile;
    private int tokens;
    private boolean isInRound;
    private boolean wonLastRound;
    private boolean isOnTurn;
    private Card playedCard;
    private int daysSinceLastDate;
    private String receivedCard;

    private boolean hasPlayedCard;
    private static Random randomGenerator = new Random();

//--------------------------------------------------------------------------------------------------------
    public ServerThread(Server server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;
        this.output = new PrintWriter(clientSocket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.name = enteringName(input, output);
        this.hasJoinedGame = false;
        server.addToMap(this);

        resetPlayerAttributes();

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
        this.receivedCard = null;
        this.hasPlayedCard = false;

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
                        server.exitGame(this);

                        server.removeFromMap(this);
                        server.removeName(this.getName());

                        String sendMessage = "%s left the room".formatted(this.getName());
                        server.sendMessageToAllClients(sendMessage);

                        this.output.close();
                        this.interrupt();
                        break;
                    } else if(receivedMessage.isBlank()){
                        //Do nothing
                    } else if (receivedMessage.startsWith("@")) {
                        server.sendPersonalMessage(this, receivedMessage);
                    } else if(receivedMessage.startsWith("$")){
                        sendingGameMessage(receivedMessage);
                    } else {
                        String sendMessage = this.getName() + ": " + receivedMessage;
                        server.sendMessageToAllClients(sendMessage);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //--------------Getter/Setter-Methods-------------------------------------------------------------------

    public PrintWriter getOutput(){
        return this.output;
    }
    public boolean getHasJoinedGame(){
        return this.hasJoinedGame;
    }
    public void setHasJoinedGame(boolean value){
        this.hasJoinedGame = value;
    }

    public boolean getIsInRound(){
        return this.isInRound;
    }
    public void setIsInRound(boolean value){
        this.isInRound = value;
    }
    public boolean getHasPlayedCard(){
        return this.hasPlayedCard;
    }
    public void setHasPlayedCard(boolean value){
        this.hasPlayedCard = value;
    }

    public boolean getHasWonLastRound(){
        return  this.wonLastRound;
    }

    public void setHasWonLastRound(boolean value){
        this.wonLastRound = value;
    }

    public String getReceivedCard(){
        return this.receivedCard;
    }

    public void setReceivedCard(String value){
        this.receivedCard = value;
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

    public boolean getIsOnTurn(){
        return this.isOnTurn;
    }

    public Hand getHand(){
        return this.hand;
    }

    public void addToken(){
        int newTokens = this.tokens + 1;
        this.tokens = newTokens;
    }

    public int getToken(){
        return this.tokens;
    }

    public void addToDiscardPile(Card card){
        this.discardPile.add(card);

    }

    public void clearDiscardPile(){
        this.discardPile.clear();
    }

    public String getDiscardPileRepresentation(){

        StringBuilder printDiscardPile = new StringBuilder("Discard Pile: [");

        for(int i = 0; i < this.discardPile.size() - 1; i++){
            printDiscardPile.append(this.discardPile.get(i).toString() + ", ");
        }
        printDiscardPile.append(discardPile.get(discardPile.size()- 1).toString() + "]");

        return String.valueOf(printDiscardPile);
    }

    public int getScoreOfDiscardPile(){
        int score = 0;
        for(Card card : this.discardPile){
            score = score + card.getCardNumber();
        }
        return score;
    }


//----------------------------------------------------------------------------------------------------------
    public void sendingGameMessage(String receivedMessage){

        if(receivedMessage.substring(1).equals("joinGame")){

            server.joinGame(this);

        }else if (receivedMessage.substring(1).equals("exitGame")){

            server.exitGame(this);

        }else if(receivedMessage.substring(1).equals("startGame")){

            server.starGame(this);

        }else if(receivedMessage.substring(1).equals("help")){

            printCardDescription();

        } else if(receivedMessage.substring(1).equals("card1")){

            if(!server.getActivePlayerList().contains(this)){
                server.sendMessageToOneClient(this, "You cannot use this game command when you are not playing.");
            } else if(this.isOnTurn ==false) {

                server.sendMessageToOneClient(this, "It's not your turn! You cannot discard a card right now.");
            }else {

                this.receivedCard = "card1";
                server.getGame().playCard(this);
                if (!server.getGame().getDeck().IsDeckEmpty()) {
                    server.getGame().takeTurn(server.getActivePlayersList().get((server.getActivePlayersList().indexOf(this) + 1) % server.getActivePlayersList().size()));
                } else {
                    server.getGame().endRound();
                }
            }

        } else if(receivedMessage.substring(1).equals("card2")){

            if(!server.getActivePlayerList().contains(this)){
                server.sendMessageToOneClient(this, "You cannot use this game command when you are not playing.");
            } else if(this.isOnTurn ==false) {

                server.sendMessageToOneClient(this, "It's not your turn! You cannot discard a card right now.");
            }else{

                this.receivedCard = "card2";

                server.getGame().playCard(this);
                if (!server.getGame().getDeck().IsDeckEmpty()) {
                    server.getGame().takeTurn(server.getActivePlayersList().get((server.getActivePlayersList().indexOf(this) + 1) % server.getActivePlayersList().size()));
                } else {
                    server.getGame().endRound();
                }
            }


        }else {

            String sendMessage = "You have entered an invalid game command. Please try again.";
            server.sendMessageToOneClient(this, sendMessage);
        }

    }


    //---------FullyImplementedMethods---------------------------------------------------------------------

    private String enteringName(BufferedReader in, PrintWriter out) {

        String name;

        //Accepting an entered name and adding it to the set of names
        try {

            while (true) {
                name = in.readLine();

                if(name == null){
                    return null;
                }

                if (server.getNames().contains(name) || name.isBlank() || name.startsWith("$") || name.startsWith("@")) {
                    String sendMessage = "This name is not available. Please enter another name: ";
                    out.println(sendMessage);
                } else {
                    server.getNames().add(name);
                    break;
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String welcomeMessage = "Welcome " + name + "!\nIf you wish to join the game, please type $joinGame.";
        output.println(welcomeMessage);

        //Informing other threads that a new client has joined

        server.sendMessageToAllClients("%s joined the room".formatted(name));


        return name;
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
        server.sendMessageToOneClient(this, description);
    }


}


