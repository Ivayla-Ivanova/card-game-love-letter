package server.game;

import server.Server;
import server.ServerThread;

import java.util.List;

public class Game {

    private static Game instance = null;
    private final Server server;

    private Deck deck;



    private Game(Server server) {

        this.server = server;
        this.deck = Deck.getInstance(this);
    }

    public static synchronized Game getInstance(Server server) {
        if (instance == null)
            instance = new Game(server);

        return instance;
    }

    public Server getGameServer(){
        return this.server;
    }
    public static void knockOutOfRound(ServerThread player){
        // Discard hand
        player.setIsInRound(false);
    }

    public void startRound(ServerThread player){

        this.deck.setUp();
        String gameMessage = "The top card has been set aside.";
        player.sendingMessageToEveryone(server.getActivePlayersList(), gameMessage);

        if(server.getActivePlayerCount() == 2){
            String gameMessageForTwoPlayers;
            gameMessageForTwoPlayers = deck.printTopThreeCards();
            player.sendingMessageToEveryone(server.getActivePlayersList(), gameMessageForTwoPlayers);
        }


    }

}
