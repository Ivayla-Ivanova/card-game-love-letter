package server;

import server.game.cards.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerThread extends Thread{

    private String name;
    private ServerThread serverThread;
    private Server server;


    public PlayerThread(ServerThread serverThread, Server server){

        this.serverThread = serverThread;
        this.name = serverThread.getName();
        this.server = server;



    }

    public void run(){

        while (true){

            /*
            if(this.isOnTurn == true){
                server.getGame().takeTurn(serverThread);
            }

             */

        }

    }



}
