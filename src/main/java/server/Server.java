package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Server {
    private static Server instance = null;
    private List<ServerThread> serverThreads;
    private ServerSocket serverSocket;
    private Set<String> names;

    private int activePlayerCount;

    // Constructor for a Singleton instance
    private Server() {

        this.serverThreads = new ArrayList<>();
        this.names = new HashSet<>();
        this.activePlayerCount = 0;

        try {
            serverSocket = new ServerSocket(5000);
            System.out.println("Server started. Listening on port 5000");

            //Accepting multiple client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("A client connected.");

                //Creating a new connector thread for each client.
                ConnectingThread connector = new ConnectingThread(this, clientSocket);
                connector.start();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized Server getInstance() {
        if (instance == null)
            instance = new Server();

        return instance;
    }

    public synchronized void removeServerThread(ServerThread client) {

        serverThreads.remove(client);
    }

    public synchronized List<ServerThread> getServerThreadsList() {

        return this.serverThreads;
    }

    public synchronized Set<String> getNames() {

        return this.names;
    }

    public synchronized void removeName(String name){
        names.remove(name);
    }

    public synchronized int getActivePlayerCount(){
        return this.activePlayerCount;
    }

    public synchronized boolean increaseActivePlayerCount(ServerThread client) throws InterruptedException {

        if(this.activePlayerCount + 1 > 4){
            System.out.println("ServerThread " + client.getName() + " cannot join the game.");
            return false;
        }

        this.activePlayerCount = this.activePlayerCount + 1;
        System.out.println("ServerThread " + client.getName() + " joined the game.");
        notifyAll();
        return true;

    }

    public synchronized boolean decreaseActivePlayerCount(ServerThread client) throws InterruptedException {

        if(this.activePlayerCount - 1 < 0){
            System.out.println("ServerThread " + client.getName()
                    + " tries to decrease the number of active players below 0.");
            return false;
        }

        this.activePlayerCount = this.activePlayerCount - 1;
        System.out.println("ServerThread " + client.getName() + " have exited the game.");
        notifyAll();
        return true;

    }



}
