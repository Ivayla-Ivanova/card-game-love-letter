package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Server {

    private List<ClientThread> clientThreads;
    private ServerSocket serverSocket;

    private static Server uniqueInstance = null;

    private Server() {

        this.clientThreads = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(5000);
            System.out.println("Server started. Listening on port 5000");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("A client connected.");
                ClientThread client = new ClientThread(this, clientSocket);
                clientThreads.add(client);
                //System.out.println(clientThreads);
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

    public synchronized void removeClientThread(ClientThread client) {
        clientThreads.remove(client);
    }

    public synchronized List<ClientThread> getClientThreadsList() {
        return new ArrayList<>(clientThreads);
    }



}
