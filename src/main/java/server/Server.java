package server;

import server.game.Game;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

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
            System.out.println("Server started. Listening on port 5000");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void runServer(){

        //Accepting multiple client connections
        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("A client connected.");

            //Creating a new connector thread for each client.
            ConnectingThread connector = new ConnectingThread(this, clientSocket);
            connector.start();
        }

    }
    public static synchronized Server getInstance() {
        if (instance == null)
            instance = new Server();

        return instance;
    }

    //------------------Getter/SetterMethods---------------------------------------------------------------
    public synchronized Set<String> getNames() {

        return this.names;
    }
    public synchronized int getActivePlayerCount(){
        return this.activePlayerCount;
    }
    public synchronized List<ServerThread> getActivePlayersList() {

        return this.activePlayersList;
    }
    public synchronized boolean getHasGameStarted(){
        return this.hasGameStarted;
    }
    public synchronized Game getGame(){
        return this.game;
    }
    public synchronized ArrayList<ServerThread> getActivePlayerList(){
        return this.activePlayersList;
    }
    public synchronized void setHasGameStarted(boolean value){
        this.hasGameStarted = value;
    }

    //------------------Add/RemoveMethods------------------------------------------------------------------
    public synchronized void addToMap(ServerThread serverThread){

        mapOfServerThreads.put(serverThread, serverThread.getOutput());

    }
    public synchronized void removeFromMap(ServerThread serverThread){

        mapOfServerThreads.remove(serverThread);
    }
    public synchronized void addToServerThreadList(ServerThread client){
        this.serverThreads.add(client);

    }
    public synchronized void removeName(String name){
        names.remove(name);
    }

    //--------------------MessageMethods-----------------------------------------------------------------------

    public synchronized void sendMessageToAllClients(String message) {

        for (ServerThread key : mapOfServerThreads.keySet()) {
            PrintWriter output = mapOfServerThreads.get(key);
            output.println(message);
        }
    }

    public synchronized void sendMessageToOneClient(ServerThread client, String message){

        PrintWriter output = mapOfServerThreads.get(client);
        output.println(message);
    }

    public  synchronized  void sendMessageToAllActivePlayers(String message){

        for(ServerThread player : activePlayersList){
            player.getOutput().println(message);
        }
    }

    public synchronized void sendMessageToAllActivePlayersExceptOne(ServerThread excluded, String message){

        for (ServerThread player : activePlayersList) {

            if(player == excluded){
                continue;
            }

            player.getOutput().println(message);
        }

    }

    public synchronized void sendMessageToNotActivePlayers(String message){

        for (ServerThread key : mapOfServerThreads.keySet()) {

            if(this.activePlayersList.contains(key)){
                continue;
            }
            PrintWriter output = mapOfServerThreads.get(key);
            output.println(message);
        }

    }
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

    //----------------------------------------------------------------------------------------------------------------

    public synchronized boolean increaseActivePlayerCount(ServerThread client) throws InterruptedException {

        if(this.activePlayerCount + 1 > 4){
            System.out.println("ServerThread " + client.getName() + " cannot join the game.");
            return false;
        }

        this.activePlayerCount = this.activePlayerCount + 1;
        activePlayersList.add(client);
        System.out.println("ServerThread " + client.getName() + " joined the game.");
        return true;

    }
    public synchronized boolean decreaseActivePlayerCount(ServerThread client) throws InterruptedException {

        if(this.activePlayerCount - 1 < 0){
            System.out.println("ServerThread " + client.getName()
                    + " tries to decrease the number of active players below 0.");
            return false;
        }

        this.activePlayerCount = this.activePlayerCount - 1;
        activePlayersList.remove(client);
        System.out.println("ServerThread " + client.getName() + " have exited the game.");
        return true;

    }
    public synchronized void modifyActivePlayerList(ArrayList<ServerThread> list){

        if(list == null || list.isEmpty()){
            System.out.println("activePlayersList remains unmodified.");
            return;
        }
        this.activePlayersList.clear();
        this.activePlayersList.addAll(list);

    }


    //---------GameRelatedMethods-----------------------------------------------------
    private void createGame(){

        this.game = Game.getInstance(this, this.activePlayersList);
        this.hasGameStarted = true;


    }
    public void gameOver(){

        sendMessageToAllClients("The game is over.");

        for(ServerThread client : serverThreads){
            client.resetPlayerAttributes();
            try {
                decreaseActivePlayerCount(client);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            client.setHasJoinedGame(false);
        }
        game.resetGame();
        setHasGameStarted(false);
        sendMessageToAllClients("You can join a new game by typing $joinGame.");
    }
    public synchronized void joinGame(ServerThread serverThread){

        try {

            if(getHasGameStarted()){
                String sendMessage = "The game has started. You cannot join it anymore.";
                sendMessageToOneClient(serverThread, sendMessage);
                return;
            }

            if(serverThread.getHasJoinedGame() == true){
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

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

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


    }

    public  synchronized void exitGame(ServerThread serverThread){

        //it user type $exitGame before joining the game
        if(serverThread.getHasJoinedGame() == false){
            String sendMessage = "You cannot exit the game if you have not yet joined it.";
            sendMessageToOneClient(serverThread, sendMessage);
            return;
        }

        try {

            if(decreaseActivePlayerCount(serverThread) == false){
                String sendMessage = "You were not able to exit the game.";
                sendMessageToOneClient(serverThread, sendMessage);
            } else {
                serverThread.setHasJoinedGame(false);
                String sendMessage = "You have exited the game.";
                sendMessageToOneClient(serverThread, sendMessage);
                String sendToEveryoneMessage = serverThread.getName() + " has exited the game.";
                sendMessageToAllActivePlayersExceptOne(serverThread, sendToEveryoneMessage);

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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
