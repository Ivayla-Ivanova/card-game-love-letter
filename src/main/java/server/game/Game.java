package server.game;

import server.Server;
import server.ServerThread;
import server.game.cards.Card;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Game {

    private static Game instance = null;
    private final Server server;

    private Deck deck;
    private ArrayList<ServerThread> listOfActivePlayers;
    private int initialCountOfActivePlayers;
    private int numberOfTokensToWin;
    private ConcurrentHashMap<ServerThread, Integer> roundWinners;
    private ServerThread roundWinner;
    private ServerThread gameWinner;
    private ArrayList<ServerThread> gameWinners;



    private Game(Server server, ArrayList<ServerThread> activePlayers) {

        this.server = server;
        this.deck = Deck.getInstance(this);
        this.listOfActivePlayers = activePlayers;

        resetGame();

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

    public void resetGame(){

        this.initialCountOfActivePlayers = server.getActivePlayerList().size();
        this.roundWinners = new ConcurrentHashMap<ServerThread, Integer>();
        this.roundWinner = null;
        this.gameWinner = null;
        this.gameWinners = new ArrayList<>();
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

    private synchronized void addToRoundWinners(ServerThread player, Integer score){
        getRoundWinnersMap().put(player, score);
    }

    private synchronized void removeFromRoundWinners(ServerThread winner){
        getRoundWinnersMap().remove(winner);
    }

    private synchronized  ConcurrentHashMap<ServerThread, Integer> getRoundWinnersMap(){
        return this.roundWinners;
    }

    private synchronized void clearRoundWinners(){
        getRoundWinnersMap().clear();
    }

    private synchronized Integer getFromMap(ServerThread key){

       return getRoundWinnersMap().get(key);
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

    public void endRound() {

        //Inform all activePlayers that the round has ended.
        for(ServerThread player : this.listOfActivePlayers){

            if(player.getIsInRound() == true) {

                server.sendMessageToOneClient(player, "The round has ended. Please reveal your hand!");
            }
            if(player.getIsInRound() == false){

                server.sendMessageToOneClient(player, "The round has ended. All hands will be revealed.");
            }
        }

        //Reveal all hands
        for(ServerThread player : this.listOfActivePlayers){
            server.sendMessageToOneClient(player,"Your " + player.getHand().toString()+ ".");
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + "'s " + player.getHand().toString()+".");
        }

        getRoundWinner();

        // Show tokens update
        for(ServerThread player : this.listOfActivePlayers) {
            server.sendMessageToOneClient(player, "You have " + player.getTokens() + " token(s).");
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + " has " + player.getTokens() + " token(s).");
            player.clearDiscardPile();
            player.getHand().clearHand();
        }

        // Determine if there is a game winner
        for(ServerThread player : server.getActivePlayerList()){

            if(player.getTokens() == this.numberOfTokensToWin){
                this.gameWinner = player;
                this.gameWinners.add(player);
            }
        }

        if(this.gameWinners.size() == 1){
            server.sendMessageToAllActivePlayers(this.gameWinner.getName() + " is the winner!");
            server.gameOver();

            return;
        }

        if(this.gameWinners.size() > 1){
            for(ServerThread winner : this.gameWinners){
                server.sendMessageToAllActivePlayers(winner.getName() + " is a winner!");
                server.gameOver();
                return;
            }
        }

        startRound();
    }




    public synchronized void getRoundWinner(){

        int highestScore = 0;

        // Determine highestScore of a hand and winner if no tie
        for(ServerThread player : this.listOfActivePlayers){

            if(player.getIsInRound() == true) {
                if (player.getHand().getHandScore() > highestScore) {
                    System.out.println(player.getName() + "'s HandScore: " + player.getHand().getHandScore());
                    highestScore = player.getHand().getHandScore();
                    this.roundWinner = player;
                }
            }
        }

        System.out.println("HighestScore: " + highestScore);


        // fill HashMap of roundWinners
        for(ServerThread player : this.listOfActivePlayers){

            if(player.getIsInRound() == true && player.getHand().getHandScore() == highestScore){
                addToRoundWinners(player, player.getHand().getHandScore());
            }
        }

        // if there is a tie ...
        if(getRoundWinnersMap().size() > 1){
            server.sendMessageToAllActivePlayers("There is a tie! The sum of players' " +
                                                 "hand scores and discard pile scores will decide the winner.");

                 // update score in HashMap
                for (ServerThread winner : getRoundWinnersMap().keySet()) {
                    int score = 0;
                    score = score + winner.getScoreOfDiscardPile() + winner.getHand().getHandScore();
                    addToRoundWinners(winner, score);
                    if (score > highestScore) {
                        highestScore = score;
                        this.roundWinner = winner;
                    }
                }
                synchronized (this.roundWinners) {
                    // remove nonWinners from HashMap
                    for (ServerThread winner : getRoundWinnersMap().keySet()) {
                        if (getFromMap(winner) != highestScore) {
                            removeFromRoundWinners(winner);
                        }
                    }
                }

            // if there is still a tie everyone from HashMap wins
                if(getRoundWinnersMap().size() > 1){

                ArrayList<ServerThread> winners = new ArrayList<>(getRoundWinnersMap().size());
                for(ServerThread winner : getRoundWinnersMap().keySet()){
                    winner.addTokens();
                    winner.setHasWonLastRound(true);
                    winners.add(winner);
                }

                StringBuilder message = new StringBuilder("The winners in this round are ");
                for(int i = 0; i < winners.size() - 1; i++){
                    message.append(winners.get(i).getName() + ", ");
                }
                message.append(winners.getLast().getName());
                for(ServerThread winner : winners){
                    winner.addTokens();
                    winner.setHasWonLastRound(true);
                }
                server.sendMessageToAllActivePlayers(String.valueOf(message));
                this.roundWinner = null;
                return;

            }

            // else there is one winner
            this.roundWinner.addTokens();
            this.roundWinner.setHasWonLastRound(true);
            server.sendMessageToAllActivePlayers("The winner of this round is " + this.roundWinner.getName());
            return;
        }

        this.roundWinner.addTokens();
        this.roundWinner.setHasWonLastRound(true);
        server.sendMessageToAllActivePlayers("The winner of this round is " + this.roundWinner.getName());
    }


    public void startRound() {

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
        clearRoundWinners();
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

        ServerThread initialPlayer = null;

                if(roundWinners.size() == 1) {
                    initialPlayer = this.roundWinner;
                    server.sendMessageToOneClient(initialPlayer, "You won in the last round. Now it's your turn to go first in this round.");
                    server.sendMessageToAllActivePlayersExceptOne(initialPlayer, initialPlayer.getName() + " won the last round. Now " + initialPlayer.getName() + " goes first.");

                    return initialPlayer;
                }

                if(roundWinners.size() < 1) {

                    for (ServerThread player : listOfActivePlayers) {
                        if (player.getDaysSinceLastDate() == getLowestDaysSinceDate()) {
                            initialPlayer = player;
                            server.sendMessageToOneClient(player, "You had recently a date. Now it's your turn to go first in this round.");
                            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() +
                                                                                  " had recently a date. Now " + player.getName() + " goes first.");
                            return initialPlayer;
                        }

                    }
                }

                if(roundWinners.size() > 1){

                    for(ServerThread player : listOfActivePlayers){

                        if (player.getHasWonLastRound() && player.getDaysSinceLastDate() == getLowestDaysSinceDate()) {
                            initialPlayer = player;
                            server.sendMessageToOneClient(player, " You won in the last round and you had recently a date. Now it's your turn to go first in this round.");
                            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() +
                                                                                  " won the last round and had recently a date. Now " + player.getName() + " goes first.");
                            return initialPlayer;
                        }

                    }

                }

            return initialPlayer;
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

