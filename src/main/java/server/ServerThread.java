package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import server.cards.Card;

/**
 * Instances of the ServerThread class handle requests send by their associate clients.
 * An instance of this class in successfully created after the associate client provides valid name.
 * A ServerThread manages also the resources associated with participating in the game.
 */
public class ServerThread extends Thread {

    //---------------ServerThread attributes------------------------------------------------------------------

    private Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;

    private Server server;

    private String name;
    private boolean hasJoinedGame;
    private boolean hasEnteredDaysSinceLastDate;
    private int age;
    private boolean hasEnteredAge;
    private boolean hasEnteredJoinGame;
    private static Random randomGenerator = new Random();

    //-----------------------------------Player attributes--------------------------------------------------------
    private Hand hand;
    private ArrayList<Card> discardPile;
    private int tokens;
    private boolean isInRound;
    private boolean wonLastRound;
    private boolean isOnTurn;
    private int daysSinceLastDate;
    private String receivedCard;
    private String nameOfChosenPlayer;
    private boolean hasCountess;
    private boolean isProtected;

    private Card discaredCard;
    private boolean playedSelection;

    private boolean hasDiscardedPrince;

    private int chosenNumber;
    private boolean hasChosenNumber;


//--------------------------------------------------------------------------------------------------------

    ServerThread(Server server, Socket clientSocket) {

        this.server = server;
        this.clientSocket = clientSocket;
        try {
            this.output = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            server.serverLog(Thread.currentThread(), "Failed to create output writer");
        }
        try {
            this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            server.serverLog(Thread.currentThread(), "Failed to create input reader.");
        }
        this.name = enteringName(input, output);

        this.hasJoinedGame = false;

        this.hasEnteredDaysSinceLastDate = false;
        this.daysSinceLastDate = 0;

        this.hasEnteredAge = false;
        this.age = 0;

        this.hasEnteredJoinGame = false;

        server.addToMap(this);
        server.addToServerThreadList(this);

        resetPlayerAttributes();

    }

    /**
     * Initialize the player attributes with default values.
     * This method is called, when ServerThread instance is created or new game starts.
     */
    public void resetPlayerAttributes(){

        this.hand = new Hand();
        this.discardPile = new ArrayList<>();
        this.tokens = 0;
        this.isInRound = false;
        this.wonLastRound = false;
        this.isOnTurn = false;

        this.receivedCard = null;
        this.nameOfChosenPlayer = null;
        this.hasCountess = false;
        this.isProtected = false;
        this.discaredCard = null;
        this.playedSelection = true;
        this.hasDiscardedPrince = false;
        this.chosenNumber = 0;
        this.hasChosenNumber = true;

    }

    /**
     *Receive incoming requests from the associate client and passes them to request handling methods.
     */
    @Override
    public void run() {

        if (this.name == null) {

            this.output.close();
            this.interrupt();
            server.serverLog(Thread.currentThread(), "Client disconnected before entering the chat.");
        } else {
            this.setName(this.name);
            server.serverLog(Thread.currentThread(), "A new ServerThread started. NAME: " + this.name);


            while (true) {

                // Receiving messages from the client
                String receivedMessage = null;
                try {
                    receivedMessage = input.readLine();
                } catch (IOException e) {
                    server.serverLog(Thread.currentThread(),"Attempt to read from the input reader has failed.");
                }


                // Clean up after the client disconnects
                if (receivedMessage == null) {
                    server.serverLog(Thread.currentThread(), this.getName() + " interrupted.");
                    server.decreaseActivePlayerCount(this);
                    safelyExitGame();

                    server.removeFromMap(this);
                    server.removeName(this.getName());

                    String sendMessage = "%s left the room.".formatted(this.getName());
                    server.sendMessageToAllClients(sendMessage);

                    this.output.close();
                    this.interrupt();
                    break;
                } else if(receivedMessage.isBlank()){
                    //Do nothing
                } else if (receivedMessage.startsWith("@")) {
                    server.sendPersonalMessage(this, receivedMessage);
                } else if(receivedMessage.startsWith("$")){
                    sendGameMessage(receivedMessage);
                } else {
                    String sendMessage = this.getName() + ": " + receivedMessage;
                    server.sendMessageToAllClients(sendMessage);
                }
            }

        }
    }

    //------------------------------------Getter/SetterMethods---------------------------------------------------------

    PrintWriter getOutput(){
        return this.output;
    }

    boolean getHasJoinedGame(){
        return this.hasJoinedGame;
    }
    void setHasJoinedGame(boolean value){
        this.hasJoinedGame = value;
    }

    boolean getIsInRound(){
        return this.isInRound;
    }
    void setIsInRound(boolean value){
        this.isInRound = value;
    }
    boolean getHasWonLastRound(){
        return  this.wonLastRound;
    }
    void setHasWonLastRound(boolean value){
        this.wonLastRound = value;
    }

    String getReceivedCard(){
        return this.receivedCard;
    }

    int getDaysSinceLastDate(){
        return this.daysSinceLastDate;
    }

    void setIsOnTurn(Thread call ,boolean value){
        this.isOnTurn = value;
        System.out.println(this.getName() + " setIsOnTurn: " + value + " by " + call.getName());
    }
    boolean getIsOnTurn(Thread call){
        System.out.println(this.getName() + " getIsOnTurn: " + this.isOnTurn + " was called by " + call.getName());
        return this.isOnTurn;

    }

    Hand getHand(){
        return this.hand;
    }
    void setHand(Hand hand){
        this.hand = hand;
    }

    void addTokens(){
        this.tokens = this.tokens + 1;
    }
    int getTokens(){
        return this.tokens;
    }
    boolean getHasEnteredJoin(){
        return this.hasEnteredJoinGame;
    }

    boolean getHasEnteredDaysSinceLastDate(){
        return this.hasEnteredDaysSinceLastDate;
    }

    int getAge(){
        return this.age;
    }

    boolean getHasEnteredAge(){
        return this.hasEnteredAge;
    }
    int getChosenNumber(){
        return this.chosenNumber;
    }

    boolean getHasChosenNumber(){
        return this.hasChosenNumber;
    }

    /**
     * Set-Method for a binary semaphore.
     */
    public void setHasChosenNumber(boolean value){
        this.hasChosenNumber = value;
    }

    boolean getHasDiscardedPrince(){
        return this.hasDiscardedPrince;
    }

    /**
     * Set-Method for a binary semaphore.
     */
    public void setHasDiscardedPrince(boolean value){
        this.hasDiscardedPrince = value;
    }

    boolean getPlayedSelection(){
        return this.playedSelection;
    }

    void setPlayedSelection(boolean value){
        this.playedSelection = value;
    }

    ArrayList<Card> getDiscardPile(){
        return this.discardPile;
    }

    Card getDiscaredCard(){
        return this.discaredCard;
    }

    void setDiscardedCard(Card card){
        this.discaredCard = card;
    }

    boolean getHasCountess(){
        return this.hasCountess;
    }

    /**
     * Set-Method for a binary semaphore.
     */
    public void setHasCountess(boolean value){
        this.hasCountess = value;
    }

    boolean getIsProtected(){
        return this.isProtected;
    }

    /**
     * Set-Method for a binary semaphore.
     * @param value
     */
    public void setIsProtected(boolean value){
        this.isProtected = value;
    }
    String getNameOfChosenPlayer(){
        return this.nameOfChosenPlayer;
    }

    /**
     * @return String representation of the discard pile of a player.
     */
    public String getDiscardPileRepresentation(){

        if(this.discardPile.isEmpty() || this.discardPile == null){
            return "Discard Pile is empty.";
        }

        StringBuilder printDiscardPile = new StringBuilder("Discard Pile: [");

        for(int i = 0; i < this.discardPile.size() - 1; i++){
            printDiscardPile.append(this.discardPile.get(i).toString() + ", ");
        }
        printDiscardPile.append(discardPile.get(discardPile.size()- 1).toString() + "]");

        return String.valueOf(printDiscardPile);
    }
    int getScoreOfDiscardPile(){
        int score = 0;
        for(Card card : this.discardPile){
            score = score + card.getCardNumber();
        }
        return score;
    }


    //--------------add/remove/clear-Methods-------------------------------------------------------------------

    void addToDiscardPile(Card card){
        this.discardPile.add(card);

    }
    void clearDiscardPile(){
        this.discardPile.clear();
    }


//-----------------CommandsHandler/Utility-Methods-----------------------------------------------------------------------------------------
    private void sendGameMessage(String receivedMessage){

        String receivedCommand = receivedMessage.substring(1);
        int receivedNumber = 0;
        try {
            receivedNumber = Integer.parseInt(receivedCommand);
        } catch (NumberFormatException e) {
            server.serverLog(Thread.currentThread(), "The received command is not an integer.");
        }

        if(receivedCommand.equals("joinGame")){

            this.hasEnteredJoinGame = true;

            server.joinGame(this);

        }else if (receivedCommand.equals("exitGame")){

            safelyExitGame();

        }else if(receivedCommand.equals("startGame")){

            server.starGame(this);

        }else if(receivedCommand.equals("help")){

            printCardDescription();

        } else if(receivedCommand.equals("card1")){

            receiveCard("card1");

        } else if(receivedCommand.equals("card2")){

            receiveCard("card2");

        } else if(server.getNames().contains(receivedCommand)){

            receiveName(receivedCommand);

        }else if(receivedNumber != 0){

            if(this.hasJoinedGame == false && server.getHasGameStarted() == false && this.hasEnteredJoinGame){


                if(this.hasEnteredDaysSinceLastDate == false){

                    this.hasEnteredDaysSinceLastDate = enterDaysSinceLastDate(receivedNumber);
                    if(this.hasEnteredJoinGame) {
                        server.joinGame(this);
                    }

                } else{

                    if(hasEnteredJoinGame) {
                        this.hasEnteredAge = enterAge(receivedNumber);
                        server.joinGame(this);
                    }
                }

            }

            if(server.getHasGameStarted()){
                receiveNumber(receivedNumber);
            }

        }else {

            String sendMessage = "You have entered an invalid game command. Please try again.";
            server.sendMessageToOneClient(this, sendMessage);
        }

    }

    private boolean enterAge(int receivedNumber){
        if(receivedNumber < 1 || receivedNumber > 120) {
            server.sendMessageToOneClient(this, "That doesn't seem right! Please try again.");
            return false;
        } else {
            this.age = receivedNumber;
            return true;
        }
    }

    private boolean enterDaysSinceLastDate(int receivedNumber){
        if(receivedNumber < 1 || receivedNumber > 1825) {
            server.sendMessageToOneClient(this, "That doesn't seem right! Please try again.");
            return false;
        } else {
            this.daysSinceLastDate = receivedNumber;
            return true;
        }
    }

    private void receiveNumber(int number){

        // Bad user input
        if (!server.getActivePlayerList().contains(this)) {
            server.sendMessageToOneClient(this, "You cannot use this game command when you are not playing.");
        } else if (this.isOnTurn == false) {

            server.sendMessageToOneClient(this, "It's not your turn! You cannot select a player.");
        } else if(number < 2 || number > 8){
            server.sendMessageToOneClient(this, "Wrong number! Try again.");
        }
        else {

            this.chosenNumber = number;
            this.hasChosenNumber = true;

        }

    }

    private void receiveName(String receivedCommand) {

        // Bad user input
        if (!server.getActivePlayerList().contains(this)) {
            server.sendMessageToOneClient(this, "You cannot use this game command when you are not playing.");
        } else if (this.isOnTurn == false) {

            server.sendMessageToOneClient(this, "It's not your turn! You cannot select a player.");
        } else {

            this.nameOfChosenPlayer = receivedCommand;
            boolean playedSelection = server.getGame().playSelection(this);

            if (!playedSelection) {

                if(this.hasChosenNumber == false) {
                    server.getGame().checkSelectable(this);
                    if (server.getGame().getSelectableList().size() < 2) {
                        server.getGame().playSelection(this);
                    }
                } else {

                    server.getGame().checkSelectable(this);
                    if (server.getGame().getSelectableList().size() < 2) {
                        server.getGame().playSelection(this);
                    }

                    String message = "You have selected an unselectable player. \n"
                            + server.getGame().printSelectable();
                    server.sendMessageToOneClient(this, message);
                }

            }

        }
    }

    private void receiveCard(String receivedCard) {
        if (!server.getActivePlayerList().contains(this)) {
            server.sendMessageToOneClient(this, "You cannot use this game command when you are not playing.");
        } else if (this.isOnTurn == false) {

            server.sendMessageToOneClient(this, "It's not your turn! You cannot discard a card right now.");
        } else {

            this.receivedCard = receivedCard;

            server.getGame().playCard(this);


            if (this.isOnTurn == false) {

                server.getGame().checkMoveOn(this);

            }



        }
    }

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

    private void safelyExitGame(){

        this.hasEnteredJoinGame = false;

        int randomCard = randomGenerator.nextInt(2);

        if(randomCard == 0) {
            this.receivedCard = "card1";
        } else{
            this.receivedCard = "card2";
        }

        this.hasChosenNumber = true;
        int randomNumber = randomGenerator.nextInt(2,9);
        this.chosenNumber = randomNumber;

        this.playedSelection = true;
        server.getGame().checkSelectable(this);
        int randomIndex = randomGenerator.nextInt(server.getGame().getSelectableList().size());
        this.nameOfChosenPlayer = server.getGame().getSelectableList().get(randomIndex).getName();
        server.getGame().knockOutOfRound(this);
        this.isOnTurn = false;
        server.getGame().checkMoveOn(this);
        server.exitGame(this);

    }

}


