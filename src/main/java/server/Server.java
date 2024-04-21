package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Server {

    private List<ServerThread> serverThreads;
    private ServerSocket serverSocket;

    private Set<String> names;

    private static Server uniqueInstance = null;

    // Constructor for a Singleton instance
    private Server() {

        this.serverThreads = new ArrayList<>();
        this.names = new HashSet<>();

        try {
            serverSocket = new ServerSocket(5000);
            System.out.println("Server started. Listening on port 5000");

            //Accepting multiple client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("A client connected.");

                //Creating a new thread for each client.
                ServerThread client = new ServerThread(this, clientSocket);
                serverThreads.add(client);
                client.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized Server getInstance() {
        if (uniqueInstance == null)
            uniqueInstance = new Server();

        return uniqueInstance;
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

}
