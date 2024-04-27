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
    private int initialCountOfActivePlayers;
    private int numberOfTokensToWin;


    private Game(Server server, ArrayList<ServerThread> activePlayers) {

        this.server = server;
        this.deck = Deck.getInstance(this);
        this.listOfActivePlayers = activePlayers;
        this.initialCountOfActivePlayers = activePlayers.size();
        if(activePlayers.size() == 2){
            this.numberOfTokensToWin = 7;
        }
        if(activePlayers.size() == 3){
            this.numberOfTokensToWin = 5;
        }

        if(activePlayers.size() == 4){
            this.numberOfTokensToWin = 4;
        }

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

        player.setIsOnTurn(true);
        player.getHand().addToHand(deck.drawCard());
        server.sendMessageToOneClient(player, "You drew a card.");
        server.sendMessageToAllActivePlayersExceptOne(player,player.getName() + " drew a card.");
        server.sendMessageToOneClient(player, player.getHand().toString());
        server.sendMessageToOneClient(player, "Which card do you want to discard? Type $card1 or $card2.");


    }

    public void playCard(ServerThread player){

        if(player.getReceivedCard() == "card1"){

            if(player.getHand().getCard1() == null){
                server.sendMessageToOneClient(player, "1.Card does not exist!");
                return;
            }

            try {
                discardCard(player, player.getHand().getCard1());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(player.getReceivedCard() == "card2"){

            if(player.getHand().getCard2() == null){
                server.sendMessageToOneClient(player, "2.Card does not exist!");
                return;
            }

            try {
                discardCard(player, player.getHand().getCard2());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        player.setIsOnTurn(false);


    }


    public void  discardCard(ServerThread player, Card card) throws IOException {

        player.getHand().removeFromHand(card);
        player.addToDiscardPile(card);
        server.sendMessageToOneClient(player, "You discarded " + card.toString() + ".");
        server.sendMessageToOneClient(player, "Your " + player.getDiscardPileRepresentation());
        server.sendMessageToAllActivePlayersExceptOne(player, player.getName() +" discarded " + card.toString());
        server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + "'s " + player.getDiscardPileRepresentation());
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

        getRoundWinner();
        for(ServerThread player : this.listOfActivePlayers) {
            server.sendMessageToOneClient(player, "You have " + player.getToken() + " token(s).");
            server.sendMessageToAllActivePlayersExceptOne(player,player.getName() + " has " + player.getToken() + " token(s).");
            player.clearDiscardPile();
        }
    }

    public void getRoundWinner(){

        int highestScore = 0;
        ArrayList<ServerThread> winners = new ArrayList<>();

        for(ServerThread player : this.listOfActivePlayers){

            if(player.getIsInRound() == true) {
                if (player.getHand().getHandScore() > highestScore) {
                    System.out.println(player.getName() + "'s HandScore: " + player.getHand().getHandScore());
                    highestScore = player.getHand().getHandScore();
                }
            }
        }

        System.out.println("HighestScore: " + highestScore);

        int countOfHighestScore = 0;

        for(ServerThread player : this.listOfActivePlayers){

            if(player.getIsInRound() == true && player.getHand().getHandScore() == highestScore){
                countOfHighestScore = countOfHighestScore + 1;
                winners.add(player);
            }
        }

       // System.out.println("Number of winners: " + countOfHighestScore);
       // System.out.println("Winners: " + winners);

        if(countOfHighestScore > 1){
            server.sendMessageToAllActivePlayers("There is a tie! The sum of players' " +
                                                 "hand scores and discard pile scores will decide the winner.");
            for(ServerThread winner : winners){
                int score = 0;
                score = score + winner.getScoreOfDiscardPile() + winner.getHand().getHandScore();
                if(score > highestScore){
                    highestScore = score;
                }
            }
            countOfHighestScore = 0;
            winners.clear();

            for(ServerThread player : this.listOfActivePlayers){
                if(player.getIsInRound() == true && player.getHand().getHandScore() + player.getScoreOfDiscardPile() == highestScore){
                    countOfHighestScore = countOfHighestScore + 1;
                    winners.add(player);
                }
            }

            if(countOfHighestScore > 1){
                StringBuilder message = new StringBuilder("The winners in this round are ");
                for(int i = 0; i < winners.size() - 1; i++){
                    message.append(winners.get(i).getName() + ", ");
                }
                message.append(winners.getLast().getName());
                for(ServerThread winner : winners){
                    winner.addToken();
                }
                server.sendMessageToAllActivePlayers(String.valueOf(message));
                return;

            }
            winners.getFirst().addToken();
            server.sendMessageToAllActivePlayers("The winner of this round is: " + winners.getFirst().getName());
            return;
        }

        winners.getFirst().addToken();
        server.sendMessageToAllActivePlayers("The winner of this round is: " + winners.getFirst().getName());



    }

    public void startRound(ServerThread player) {

        this.deck.setUp();
        for(ServerThread activePlayer : this.listOfActivePlayers){
            activePlayer.setIsInRound(true);
        }
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

        player.setIsOnTurn(true);
        player.getHand().addToHand(deck.drawCard());
        server.sendMessageToOneClient(player, "You drew a card.");
        server.sendMessageToAllActivePlayersExceptOne(player,player.getName() + " drew a card.");
        server.sendMessageToOneClient(player, player.getHand().toString());
        player.setIsOnTurn(false);
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

