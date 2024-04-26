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
        player.getPlayer().setIsOnTurn(true);
        player.getPlayer().setHasPlayedCard(false);
        player.getPlayer().getHand().addToHand(deck.drawCard());
        player.sendingMessageToOwnClient("You drew a card.");
        player.sendingToAllPlayersExceptMe(player.getName() + " drew a card.");
        player.sendingMessageToOwnClient(player.getPlayer().getHand().toString());
        player.sendingMessageToOwnClient("Which card do you want to discard? Type $card1 or $card2.");
        /*while(player.getPlayer().getHasPlayedCard() == false){
            playCard(player);
        }
*/

    }
/*
    public void playCard(ServerThread player){

        if(player.getReceivedCard() == "card1"){
            try {
                discardCard(player, player.getHand().getCard1());
                player.setHasPlayedCard(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(player.getReceivedCard() == "card2"){
            try {
                discardCard(player, player.getHand().getCard2());
                player.setHasPlayedCard(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

 */

    public void  discardCard(ServerThread player, Card card) throws IOException {

        player.getPlayer().getHand().removeFromHand(card);
        // add to discardPile
        player.sendingMessageToOwnClient("You discarded " + card.toString() + ".");
        player.sendingToAllPlayersExceptMe(player.getName() +" discarded " + card.toString());
        player.sendingMessageToOwnClient(player.getPlayer().getHand().toString());
        card.applyCardEffect(player);
        player.getPlayer().setIsOnTurn(false);

    }



    public void takeInitialTurn(ServerThread player) {

        player.getPlayer().setIsOnTurn(true);
        player.getPlayer().getHand().addToHand(deck.drawCard());
        player.sendingMessageToOwnClient("You drew a card.");
        player.sendingToAllPlayersExceptMe(player.getName() + " drew a card.");
        player.sendingMessageToOwnClient(player.getPlayer().getHand().toString());
        player.getPlayer().setIsOnTurn(false);
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

        ServerThread initialPlayer;
        initialPlayer = getInitialPlayer();
        this.resortedListOfActivePlayers = reSortListOfActivePlayers(initialPlayer);

        for(int i = 0; i < this.resortedListOfActivePlayers.size(); i++){
            takeInitialTurn(resortedListOfActivePlayers.get(i));
        }

        for(int i = 0; i < this.resortedListOfActivePlayers.size(); i++){
            takeTurn(resortedListOfActivePlayers.get(i));
        }

        }

        private List<ServerThread> reSortListOfActivePlayers(ServerThread initialPlayer){

            List<ServerThread> firstSubList =
                    server.getActivePlayersList().subList(server.getActivePlayersList().indexOf(initialPlayer), server.getActivePlayersList().size());
            List<ServerThread> secondSubList = server.getActivePlayersList().subList(0, server.getActivePlayersList().indexOf(initialPlayer));
            firstSubList.addAll(secondSubList);
            return firstSubList;

        }

        private ServerThread getInitialPlayer(){

            for (ServerThread initialPlayer : server.getActivePlayersList()) {

                if (initialPlayer.getPlayer().getWonLastRound()) {
                    initialPlayer.sendingMessageToOwnClient("You won in the last round. Now it's your turn to go first in this round.");
                    initialPlayer.sendingToAllPlayersExceptMe(initialPlayer.getName() + " won the last round. Now " + initialPlayer.getName() + " goes first.");
                    return initialPlayer;

                }

                if (initialPlayer.getPlayer().getDaysSinceLastDate() == getLowestDaysSinceDate()) {


                    initialPlayer.sendingMessageToOwnClient("You had recently a date. Now it's your turn to go first in this round.");
                    initialPlayer.sendingToAllPlayersExceptMe(initialPlayer.getName() + " had recently a date. Now " + initialPlayer.getName() + " goes first.");
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

