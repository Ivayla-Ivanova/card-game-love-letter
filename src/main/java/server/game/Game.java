package server.game;

import server.Server;
import server.ServerThread;
import server.game.cards.Card;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Game {

    private static Game instance = null;
    private final Server server;

    private Deck deck;
    private List<ServerThread> resortedListOfActivePlayers;


    private Game(Server server) {

        this.server = server;
        this.deck = Deck.getInstance(this);
        this.resortedListOfActivePlayers = new ArrayList<>();
    }

    public static synchronized Game getInstance(Server server) {
        if (instance == null)
            instance = new Game(server);

        return instance;
    }

    public Server getGameServer() {
        return this.server;
    }

    public static void knockOutOfRound(ServerThread player) {
        // Discard hand
        player.setIsInRound(false);
    }

    public void takeTurn(ServerThread player) {
        player.setIsOnTurn(true);
        player.getHand().addToHand(deck.drawCard());

    }

    public void takeInitialTurn(ServerThread player) {

        player.setIsOnTurn(true);
        player.getHand().addToHand(deck.drawCard());
        player.sendingMessageToOwnClient("You drew a card.");
        player.sendingToAllPlayersExceptMe(player.getName() + " drew a card.");
        player.sendingMessageToOwnClient(player.getHand().toString());
        player.setIsOnTurn((false));
    }

    public void discardCard(ServerThread player, Card card) throws IOException {

        player.getHand().removeFromHand(card);
        card.applyCardEffect(player);
        player.setIsOnTurn(false);

    }

    public void startRound(ServerThread player) {

        this.deck.setUp();
        String gameMessage = "The top card has been set aside.";
        player.sendingMessageToEveryone(server.getActivePlayersList(), gameMessage);

        if (server.getActivePlayerCount() == 2) {
            String gameMessageForTwoPlayers;
            gameMessageForTwoPlayers = deck.printTopThreeCards();
            player.sendingMessageToEveryone(server.getActivePlayersList(), gameMessageForTwoPlayers);
        }

        int minDaysSinceLastDate = 400;
        for (ServerThread client : server.getActivePlayersList()) {

            if (client.getDaysSinceLastDate() < minDaysSinceLastDate) {
                minDaysSinceLastDate = client.getDaysSinceLastDate();
            }
        }

            for (ServerThread initialPlayer : server.getActivePlayersList()) {

                if (initialPlayer.getWonLastRound()) {
                    initialPlayer.sendingMessageToOwnClient("You won in the last round. Now it's your turn to go first in this round.");
                    initialPlayer.sendingToAllPlayersExceptMe(initialPlayer.getName() + " won the last round. Now " + initialPlayer.getName() + " goes first.");
                    takeInitialTurn(initialPlayer);
                    return;

                }

                if (initialPlayer.getDaysSinceLastDate() == minDaysSinceLastDate) {


                    initialPlayer.sendingMessageToOwnClient("You had recently a date. Now it's your turn to go first in this round.");
                    initialPlayer.sendingToAllPlayersExceptMe(initialPlayer.getName() + " hat recently a date. Now " + initialPlayer.getName() + " goes first.");
                    takeInitialTurn(initialPlayer);
                    reSortListOfActivePlayers(initialPlayer);
                    return;


                }


            }


        }

        private void reSortListOfActivePlayers(ServerThread initialPlayer){

            List<ServerThread> firstSubList =
                    server.getActivePlayersList().subList(server.getActivePlayersList().indexOf(initialPlayer), server.getActivePlayersList().size());
            List<ServerThread> secondSubList = server.getActivePlayersList().subList(0, server.getActivePlayersList().indexOf(initialPlayer));
            firstSubList.addAll(secondSubList);
            this.resortedListOfActivePlayers = firstSubList;


            /*for (int pl = 1; pl < firstSubList.size(); pl++) {
                takeInitialTurn(firstSubList.get(pl));
            } */

        }
}

