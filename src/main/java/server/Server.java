package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * The Server class represents a server application.
 * It handles incoming client connections, manages message passing to ServerThreads and
 * shared resources between ServerThread and Game classes.
 */

public class Server {
    private static Server instance = null;
    private ServerSocket serverSocket;
    private Set<String> names;
    private ArrayList<ServerThread> activePlayersList;
    private Map<ServerThread, PrintWriter> mapOfServerThreads;
    private ArrayList<ServerThread> serverThreads;

    private int activePlayerCount;

    private Game game;
    private boolean hasGameStarted;

    private Thread mainThread;



    //---------------------------------------------------------------------------------------------------------

    // Constructor for a Singleton instance
    private Server() {

        mainThread = Thread.currentThread();
        this.names = new HashSet<>();
        this.activePlayerCount = 0;
        this.activePlayersList = new ArrayList<>();
        this.hasGameStarted = false;
        this.game = null;
        this.mapOfServerThreads = new HashMap<>();
        this.serverThreads = new ArrayList<>();

        try {
            serverSocket = new ServerSocket(5000);
            serverLog(Thread.currentThread(),"Server started. Listening on port 5000");

        } catch (IOException e) {
            serverLog(Thread.currentThread(), "Failed to create ServerSocket.");
        }
    }
    // Start listening for connecting clients
    public void runServer(){

        //Accepting multiple client connections
        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                serverLog(Thread.currentThread(), "Error accepting client connection.");
            }
            serverLog(Thread.currentThread(),"A client connected.");

            //Creating a new connector thread for each client.
            ConnectingThread connector = new ConnectingThread(this, clientSocket);
            connector.start();
        }

    }

    /**
     * Creates unique Server instance.
     */
    public static synchronized Server getInstance() {
        if (instance == null)
            instance = new Server();

        return instance;
    }

    //------------------Getter/SetterMethods---------------------------------------------------------------

    public ServerSocket getServerSocket(){
        return this.serverSocket;
    }
    synchronized Set<String> getNames() {

        return this.names;
    }
    synchronized int getActivePlayerCount(){
        return this.activePlayerCount;
    }
    synchronized List<ServerThread> getActivePlayersList() {

        return this.activePlayersList;
    }
    synchronized boolean getHasGameStarted(){
        return this.hasGameStarted;
    }
    synchronized Game getGame(){
        return this.game;
    }
    synchronized ArrayList<ServerThread> getActivePlayerList(){
        return this.activePlayersList;
    }
    synchronized void setHasGameStarted(boolean value){
        this.hasGameStarted = value;
    }

    //------------------Add/RemoveMethods------------------------------------------------------------------
    synchronized void addToMap(ServerThread serverThread){

        mapOfServerThreads.put(serverThread, serverThread.getOutput());

    }
    synchronized void removeFromMap(ServerThread serverThread){

        mapOfServerThreads.remove(serverThread);
    }
    synchronized void addToServerThreadList(ServerThread client){
        this.serverThreads.add(client);

    }
    synchronized void removeName(String name){
        names.remove(name);
    }

    //--------------------MessageMethods-----------------------------------------------------------------------

    /**
     * Passes message to all connected and registered clients.
     */
    public synchronized void sendMessageToAllClients(String message) {

        for (ServerThread key : mapOfServerThreads.keySet()) {
            PrintWriter output = mapOfServerThreads.get(key);
            output.println(message);
        }
    }

    /**
     * Passes message to specified client.
     */
    public synchronized void sendMessageToOneClient(ServerThread client, String message){

        PrintWriter output = mapOfServerThreads.get(client);
        output.println(message);
    }

    /**
     * Passes message to all active players.
     */
    public synchronized  void sendMessageToAllActivePlayers(String message){

        for(ServerThread player : activePlayersList){
            player.getOutput().println(message);
        }
    }

    /**
     * Passes message to all active player except for one.
     * @param excluded Excluded player.
     */
    public synchronized void sendMessageToAllActivePlayersExceptOne(ServerThread excluded, String message){

        for (ServerThread player : activePlayersList) {

            if(player == excluded){
                continue;
            }

            player.getOutput().println(message);
        }

    }

    /**
     * Passes message to all registered but not playing clients.
     */
    public synchronized void sendMessageToNotActivePlayers(String message){

        for (ServerThread key : mapOfServerThreads.keySet()) {

            if(this.activePlayersList.contains(key)){
                continue;
            }
            PrintWriter output = mapOfServerThreads.get(key);
            output.println(message);
        }

    }
    /**
     * Passes message to one client by its ServerThread if its name was entered correctly.
     * @param from This is the ServerThread sending a passing message request.
     */
    public synchronized void sendPersonalMessage(ServerThread from, String message){

        String [] temp = message.split(" ", 2);

        if (temp.length != 2) {

            String sendMessage = """
            Invalid request.
            Please send private messages by e.g. writing '@name message'!""";
            sendMessageToOneClient(from, sendMessage);
            return;
        }

        String addresseeName = temp[0].substring(1);

        if(!this.names.contains(addresseeName) || addresseeName.equals(from.getName())){
            String sendMessage = """
            Invalid name.
            Please send private messages by e.g. writing '@name message'!""";
            sendMessageToOneClient(from, sendMessage);
            return;
        }
        if(temp[1].isBlank()){
            return;
        }
        String sendMessage = "[private] " + from.getName() + ": " + temp[1];

        for (ServerThread client : mapOfServerThreads.keySet()) {

            if (client.getName().equals(addresseeName)) {
                sendMessageToOneClient(client, sendMessage);
            }
        }

    }

    void serverLog(Thread call, String message){
        System.out.println("From " + call.getName() + ": " +message);
    }

    //----------------------------------------------------------------------------------------------------------------

    /**
     * Increases the count of active players if its maximum is still not reached.
     * @return true if the procedure was completed successfully.
     */
    public synchronized boolean increaseActivePlayerCount(ServerThread client){

        if(this.activePlayerCount + 1 > 4){
            serverLog(Thread.currentThread(), "ServerThread " + client.getName() + " cannot join the game.");
            return false;
        }

        this.activePlayerCount = this.activePlayerCount + 1;
        activePlayersList.add(client);
        serverLog(Thread.currentThread(), "ServerThread " + client.getName() + " joined the game.");
        System.out.println();
        return true;

    }
    /**
     * Decreases the count of active players with restrictions.
     * @return true if the procedure was completed successfully.
     */
    public synchronized boolean decreaseActivePlayerCount(ServerThread client){

        if(this.activePlayerCount - 1 < 0){
            serverLog(Thread.currentThread(), "ServerThread " + client.getName()
                    + " tries to decrease the number of active players below 0.");
            return false;
        }

        this.activePlayerCount = this.activePlayerCount - 1;
        activePlayersList.remove(client);
        serverLog(Thread.currentThread(), "ServerThread " + client.getName() + " have exited the game.");
        return true;

    }

    /**
     * Replaces the entries of the list of active players with the entries of a given list.
     */
    public synchronized void modifyActivePlayerList(ArrayList<ServerThread> list){

        if(list == null || list.isEmpty()){
            serverLog(Thread.currentThread(), "List of active players remains unmodified.");
            return;
        }
        this.activePlayersList.clear();
        this.activePlayersList.addAll(list);

    }


    //---------GameRelatedMethods-----------------------------------------------------
    private void createGame(){

        this.game = Game.getInstance(this, this.activePlayersList);
        this.hasGameStarted = true;
        serverLog(Thread.currentThread(), "A game instance was created.");

    }

    /**
     * Performs clean-up tasks and notifies all connected clients about the end of the game
     * after a specified ending condition is met.
     */
    public void gameOver(){

        serverLog(Thread.currentThread(), "The game was ended.");

        sendMessageToAllClients("The game is over.");

        for(ServerThread client : serverThreads){
            client.resetPlayerAttributes();
            decreaseActivePlayerCount(client);
            client.setHasJoinedGame(false);
        }
        game.resetGame();
        setHasGameStarted(false);
        sendMessageToAllClients("You can join a new game by typing $joinGame.");
    }

    /**
     * Handles a join game request sent by a client.
     * This method processes the request, manages the conditions for a client to join the game
     * and notifies other players about the new player joining if the conditions are met.
     */
    public synchronized void joinGame(ServerThread serverThread){

        if(serverThread.getHasEnteredJoin() == false){
            String sendMessage = "You cannot use game commands until you have joined the game.";
            sendMessageToOneClient(serverThread, sendMessage);
            return;
        }

        if(getHasGameStarted() && serverThread.getHasEnteredJoin()){
            String sendMessage = "The game has started. You cannot join it anymore.";
            sendMessageToOneClient(serverThread, sendMessage);
            return;
        }

        if(serverThread.getHasJoinedGame() == true && serverThread.getHasEnteredJoin()){
            String sendMessage = "You have already joined the game.";
            sendMessageToOneClient(serverThread, sendMessage);
            return;
        }

        if(serverThread.getHasEnteredDaysSinceLastDate() == false) {
            sendMessageToOneClient(serverThread, "When was the last time you went on a date within the last 5 years?\n" +
                    "Enter $number of days since your last date: ");

        }else {
            if (serverThread.getHasEnteredAge() == false) {
                sendMessageToOneClient(serverThread, "How old are you in years?");

            }
        }

        if(serverThread.getHasEnteredDaysSinceLastDate() == true &&
        serverThread.getHasEnteredAge() == true) {

            if (increaseActivePlayerCount(serverThread) == false) {

                String sendMessage = "You were not able to join the game. Please try again later.";
                sendMessageToOneClient(serverThread, sendMessage);
            } else {
                serverThread.setHasJoinedGame(true);
                String sendMessage = "You joined the game.\nTo exit the game type $exitGame.";
                sendMessageToOneClient(serverThread, sendMessage);
                String sendToEveryoneMessage = serverThread.getName() + " joined the game";
                sendMessageToAllActivePlayersExceptOne(serverThread, sendToEveryoneMessage);
                printGameMessagesToActivePlayers(this.activePlayerCount);

            }
        }

    }

    /**
     * Handles a start game request sent by a client. If the conditions to start a game are met,
     * this method calls the createGame method and starts the first round.
     * It also notifies all clients about the start of a new game.
     */
    public synchronized void starGame(ServerThread serverThread){

        if(!serverThread.getHasJoinedGame()){
            String sendMessage = "You need to first join the game to start it.";
            sendMessageToOneClient(serverThread, sendMessage);
            return;
        }

        if(this.hasGameStarted){
            String sendMessage = "The game has already started.";
            sendMessageToOneClient(serverThread, sendMessage);
            return;
        }

        if(this.activePlayerCount < 2){
            printGameMessagesToActivePlayers(this.activePlayerCount);
            return;
        }

        createGame();

        sendMessageToAllActivePlayers("The game has started. You are playing now!\n" +
                                             "To see the description of the cards, enter $help. ");

        sendMessageToNotActivePlayers("The game has started. You cannot join it anymore.");

        for(ServerThread player : this.activePlayersList){
            String sendMessage = "Last time you went on a date was "
                                 + player.getDaysSinceLastDate()+" days ago!";
            sendMessageToOneClient(player, sendMessage);
        }

        this.game.startRound();
        serverLog(Thread.currentThread(), "The game has started.");


    }

    /**
     * Handles an exit game request sent by a client. If the conditions to exit the game are met,
     * this method decreases the number of active players and if the number is lower than 2 ends the game.
     * It also notifies all clients about player exiting the game.
     */
    public synchronized void exitGame(ServerThread serverThread){

        //if user types $exitGame before joining the game
        if(serverThread.getHasJoinedGame() == false){
            String sendMessage = "You cannot exit the game if you have not yet joined it.";
            sendMessageToOneClient(serverThread, sendMessage);
            return;
        }

        if(decreaseActivePlayerCount(serverThread) == false){
            String sendMessage = "You were not able to exit the game.";
            sendMessageToOneClient(serverThread, sendMessage);
        } else {
            serverThread.setHasJoinedGame(false);
            String sendMessage = "You have exited the game.";
            sendMessageToOneClient(serverThread, sendMessage);
            String sendToEveryoneMessage = serverThread.getName() + " has exited the game.";
            sendMessageToAllActivePlayersExceptOne(serverThread, sendToEveryoneMessage);
            serverLog(Thread.currentThread(), serverThread.getName() + " has exited the game.");

            if(hasGameStarted == true){

                if(activePlayersList.size() < 2){
                    gameOver();
                    return;
                }
            }

            if(hasGameStarted == false) {
                printGameMessagesToActivePlayers(this.activePlayerCount);
            }
        }

    }

    private void printGameMessagesToActivePlayers(int count){


        String countMessage;

        switch (count) {
            case 0:
                countMessage = "There needs to be at least two players to start the game.";
                sendMessageToAllActivePlayers(countMessage);
                break;

            case 1:
                countMessage = "Waiting for at least one more player to start the game.";
                sendMessageToAllActivePlayers(countMessage);
                break;
            case 2:
                countMessage = "You can start the game now by typing $startGame or wait for one or two more players to join.";
                sendMessageToAllActivePlayers(countMessage);
                break;
            case 3:
                countMessage = "You can start the game now by typing $startGame or wait for one more player to join.";
                sendMessageToAllActivePlayers(countMessage);
                break;
            case 4:
                countMessage = "You can start the game now by typing $startGame.";
                sendMessageToAllActivePlayers(countMessage);
                break;
            default:
                // Do nothing
        }

    }

}
