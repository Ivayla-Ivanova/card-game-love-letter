package server;

import java.net.Socket;

/**
 * Instances of this class are created and started by the server instance after a client connection is accepted.
 * A ConnectingThread creates and started a ServerThread.
 * This class allows the server to accept multiple client connections
 * while the constructor of ServerThread waits for valid name.
 */
public class ConnectingThread extends Thread{

    private Socket clientSocket;
    private Server server;

    ConnectingThread(Server server, Socket clientSocket){
        this.clientSocket = clientSocket;
        this.server = server;
    }
    // ServerThread is created after the client enters a valid name

    /**
     * run-Method of the ConnectingThread
     */
    @Override
    public void run() {
        ServerThread client = null;
        client = new ServerThread(this.server, this.clientSocket);
        client.start();
    }

}
