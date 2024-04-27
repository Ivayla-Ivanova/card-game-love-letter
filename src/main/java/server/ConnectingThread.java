package server;

import java.io.IOException;
import java.net.Socket;

public class ConnectingThread extends Thread{

    private Socket clientSocket;
    private Server server;

    public ConnectingThread(Server server, Socket clientSocket){
        this.clientSocket = clientSocket;
        this.server = server;
    }
    // ServerThread is created after the client enters a valid name
    @Override
    public void run() {
        ServerThread client = null;
        try {
            client = new ServerThread(this.server, this.clientSocket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        client.start();
    }

}
