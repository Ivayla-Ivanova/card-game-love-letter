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
        player.getPlayer().setIsInRound(false);
    }

    public void takeTurn(ServerThread player) {
        player.getPlayer().setHasPlayedCard(false);
        player.getPlayer().getHand().addToHand(deck.drawCard());
        server.sendMessageToOneClient(player, "You drew a card.");
        server.sendMessageToAllActivePlayersExceptOne(player,player.getName() + " drew a card.");
        server.sendMessageToOneClient(player, player.getPlayer().getHand().toString());
        server.sendMessageToOneClient(player, "Which card do you want to discard? Type $card1 or $card2.");


    }

    public void playCard(ServerThread player){

        if(player.getPlayer().getReceivedCard() == "card1"){
            try {
                discardCard(player, player.getPlayer().getHand().getCard1());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(player.getPlayer().getReceivedCard() == "card2"){
            try {
                discardCard(player, player.getPlayer().getHand().getCard2());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }


    public void  discardCard(ServerThread player, Card card) throws IOException {

        player.getPlayer().getHand().removeFromHand(card);
        // add to discardPile
        server.sendMessageToOneClient(player, "You discarded " + card.toString() + ".");
        server.sendMessageToAllActivePlayersExceptOne(player, player.getName() +" discarded " + card.toString());
        server.sendMessageToOneClient(player, player.getPlayer().getHand().toString());
        //card.applyCardEffect(player);

    }






    public void startRound(ServerThread player) {

        this.deck.setUp();
        String gameMessage = "The top card has been set aside.";
        server.sendMessageToAllActivePlayers(gameMessage);

        if (server.getActivePlayerCount() == 2) {
            String gameMessageForTwoPlayers;
            gameMessageForTwoPlayers = deck.printTopThreeCards();
            server.sendMessageToAllActivePlayers(gameMessageForTwoPlayers);
        }

        ServerThread initialPlayer;
        initialPlayer = getInitialPlayer();
        this.resortedListOfActivePlayers = reSortListOfActivePlayers(initialPlayer);

        for(int i = 0; i < this.resortedListOfActivePlayers.size(); i++){
            takeInitialTurn(resortedListOfActivePlayers.get(i));
        }

        takeTurn(initialPlayer);


        }

        //--------FullyImplementedAndWorkingMethods-----------------------

        public void takeInitialTurn(ServerThread player) {

        player.getPlayer().getHand().addToHand(deck.drawCard());
        server.sendMessageToOneClient(player, "You drew a card.");
        server.sendMessageToAllActivePlayersExceptOne(player,player.getName() + " drew a card.");
        server.sendMessageToOneClient(player, player.getPlayer().getHand().toString());
    }
        private List<ServerThread> reSortListOfActivePlayers(ServerThread initialPlayer){

            List<ServerThread> firstSubList =
                    server.getActivePlayersList().subList(server.getActivePlayersList().indexOf(initialPlayer), server.getActivePlayersList().size());
            List<ServerThread> secondSubList = server.getActivePlayersList().subList(0, server.getActivePlayersList().indexOf(initialPlayer));
            firstSubList.addAll(secondSubList);
            System.out.println("ResortedList: " + firstSubList);
            return firstSubList;

        }
        private ServerThread getInitialPlayer(){

            for (ServerThread initialPlayer : server.getActivePlayersList()) {

                if (initialPlayer.getPlayer().getWonLastRound()) {

                    server.sendMessageToOneClient(initialPlayer, "You won in the last round. Now it's your turn to go first in this round.");
                    server.sendMessageToAllActivePlayersExceptOne(initialPlayer, initialPlayer.getName() + " won the last round. Now " + initialPlayer.getName() + " goes first.");
                    return initialPlayer;

                }

                if (initialPlayer.getPlayer().getDaysSinceLastDate() == getLowestDaysSinceDate()) {

                    server.sendMessageToOneClient(initialPlayer, "You had recently a date. Now it's your turn to go first in this round.");
                    server.sendMessageToAllActivePlayersExceptOne(initialPlayer, initialPlayer.getName() + " had recently a date. Now " + initialPlayer.getName() + " goes first.");
                    return initialPlayer;


                }


            }

            return null;

        }

        private int getLowestDaysSinceDate(){

            int minDaysSinceLastDate = 400;
            for (ServerThread client : server.getActivePlayersList()) {

                if (client.getPlayer().getDaysSinceLastDate() < minDaysSinceLastDate) {
                    minDaysSinceLastDate = client.getPlayer().getDaysSinceLastDate();
                }
            }

            return minDaysSinceLastDate;

        }
}

