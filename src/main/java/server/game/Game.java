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
    private ArrayList<ServerThread> listOfActivePlayers;


    private Game(Server server, ArrayList<ServerThread> activePlayers) {

        this.server = server;
        this.deck = Deck.getInstance(this);
        this.listOfActivePlayers = activePlayers;

    }

    public static synchronized Game getInstance(Server server, ArrayList<ServerThread> activePlayers) {
        if (instance == null)
            instance = new Game(server, activePlayers);

        return instance;
    }

    public Server getGameServer() {
        return this.server;
    }

    public Deck getDeck(){
        return this.deck;
    }

    public static void knockOutOfRound(ServerThread player) {
        // Discard hand
        player.setIsInRound(false);
    }

    public void takeTurn(ServerThread player) {

        player.getHand().addToHand(deck.drawCard());
        server.sendMessageToOneClient(player, "You drew a card.");
        server.sendMessageToAllActivePlayersExceptOne(player,player.getName() + " drew a card.");
        server.sendMessageToOneClient(player, player.getHand().toString());
        server.sendMessageToOneClient(player, "Which card do you want to discard? Type $card1 or $card2.");


    }

    public void playCard(ServerThread player){

        if(player.getReceivedCard() == "card1"){
            try {
                discardCard(player, player.getHand().getCard1());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(player.getReceivedCard() == "card2"){
            try {
                discardCard(player, player.getHand().getCard2());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }


    public void  discardCard(ServerThread player, Card card) throws IOException {

        player.getHand().removeFromHand(card);
        // add to discardPile
        server.sendMessageToOneClient(player, "You discarded " + card.toString() + ".");
        server.sendMessageToAllActivePlayersExceptOne(player, player.getName() +" discarded " + card.toString());
        server.sendMessageToOneClient(player, player.getHand().toString());
        //card.applyCardEffect(player);

    }

    public void endRound(){

        for(ServerThread player : this.listOfActivePlayers){

            if(player.getIsInRound() == true) {

                server.sendMessageToOneClient(player, "The round has ended. Please reveal your hand!");
            }
            if(player.getIsInRound() == false){

                server.sendMessageToOneClient(player, "The round has ended. All hands will be revealed.");
            }
        }

        for(ServerThread player : this.listOfActivePlayers){
            server.sendMessageToOneClient(player,"Your " + player.getHand().toString()+ ".");
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + "'s " + player.getHand().toString()+".");
        }
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
        reSortListOfActivePlayers(initialPlayer);

        for (int i = 0; i < this.listOfActivePlayers.size(); i++) {
            takeInitialTurn(this.listOfActivePlayers.get(i));
        }

        takeTurn(initialPlayer);



    }

        //--------FullyImplementedAndWorkingMethods-----------------------

        public void takeInitialTurn(ServerThread player) {

        player.getHand().addToHand(deck.drawCard());
        server.sendMessageToOneClient(player, "You drew a card.");
        server.sendMessageToAllActivePlayersExceptOne(player,player.getName() + " drew a card.");
        server.sendMessageToOneClient(player, player.getHand().toString());
    }
        private void reSortListOfActivePlayers(ServerThread initialPlayer){

            List<ServerThread> firstSubList =
                    this.listOfActivePlayers.subList(this.listOfActivePlayers.indexOf(initialPlayer), this.listOfActivePlayers.size());
            List<ServerThread> secondSubList = this.listOfActivePlayers.subList(0, this.listOfActivePlayers.indexOf(initialPlayer));
            firstSubList.addAll(secondSubList);
            ArrayList<ServerThread> temp = new ArrayList<>();
            temp.addAll(firstSubList);
            server.modifyActivePlayerList(temp);


        }
        private ServerThread getInitialPlayer(){

            for (ServerThread initialPlayer : server.getActivePlayersList()) {

                if (initialPlayer.getWonLastRound()) {

                    server.sendMessageToOneClient(initialPlayer, "You won in the last round. Now it's your turn to go first in this round.");
                    server.sendMessageToAllActivePlayersExceptOne(initialPlayer, initialPlayer.getName() + " won the last round. Now " + initialPlayer.getName() + " goes first.");
                    return initialPlayer;

                }

                if (initialPlayer.getDaysSinceLastDate() == getLowestDaysSinceDate()) {

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

                if (client.getDaysSinceLastDate() < minDaysSinceLastDate) {
                    minDaysSinceLastDate = client.getDaysSinceLastDate();
                }
            }

            return minDaysSinceLastDate;

        }
}

