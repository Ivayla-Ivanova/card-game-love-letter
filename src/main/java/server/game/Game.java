package server.game;

import server.Server;
import server.ServerThread;
import server.game.cards.Card;
import server.Hand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Game {

    private static Game instance = null;
    private final Server server;

    private Deck deck;
    private ArrayList<ServerThread> listOfActivePlayers;
    private int numberOfTokensToWin;

    private ConcurrentHashMap<ServerThread, Integer> roundWinners;
    private ServerThread roundWinner;
    private ServerThread gameWinner;
    private ArrayList<ServerThread> gameWinners;
    private ConcurrentHashMap<ServerThread, Boolean> playerInRound;
    private ArrayList<ServerThread> selectableList;

    //------------------------------------------------------------------------------------------------------
    private Game(Server server, ArrayList<ServerThread> activePlayers) {

        this.server = server;
        this.deck = Deck.getInstance(this);
        this.listOfActivePlayers = activePlayers;

        resetGame();



    }

    public void resetGame(){

        this.roundWinners = new ConcurrentHashMap<>();
        this.roundWinner = null;
        this.gameWinner = null;
        this.gameWinners = new ArrayList<>();
        this.playerInRound = new ConcurrentHashMap<>();
        this.selectableList = new ArrayList<>();

        if(server.getActivePlayerList().size() == 2){
            this.numberOfTokensToWin = 7;
        }
        if(server.getActivePlayerList().size() == 3){
            this.numberOfTokensToWin = 5;
        }

        if(server.getActivePlayerList().size() == 4){
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

    public synchronized ArrayList<ServerThread> getSelectableList(){
        return this.selectableList;
    }

    //---------------RoundWinnersMethods-------------------------------------------------------------------------------------------

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

    //-----------------GameMethods-------------------------------------------------------------------------------

    public void knockOutOfRound(ServerThread player) {
        player.setIsInRound(false);
        this.playerInRound.put(player, false);
        Card hand = player.getHand().discardHand();
        player.addToDiscardPile(hand);
        player.getHand().clearHand();
        int countPlayersInRound = 0;
        for(ServerThread activePlayer : listOfActivePlayers){
            if(activePlayer.getIsInRound() == true){
                countPlayersInRound = countPlayersInRound + 1;
            }
        }
        if(countPlayersInRound < 2){
            endRound();
        }
    }

    //----------------------------TurnMethods---------------------------------------------------------------
    private void takeInitialTurn(ServerThread player) {

        player.setIsOnTurn(true);
        Card newCard = deck.drawCard();
        player.getHand().addToHand(newCard);

        if(newCard.getCardNumber() == 7){
            player.setHasCountess(true);
        }

        server.sendMessageToOneClient(player, "You drew a card.");
        server.sendMessageToAllActivePlayersExceptOne(player,player.getName() + " drew a card.");
        server.sendMessageToOneClient(player, player.getHand().toString());
        player.setIsOnTurn(false);
    }
    private void takeTurn(ServerThread player) throws IOException {

            player.setIsOnTurn(true);
            Card newCard = deck.drawCard();
            player.getHand().addToHand(newCard);
            server.sendMessageToOneClient(player, "You drew a card.");
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + " drew a card.");
            server.sendMessageToOneClient(player, player.getHand().toString());

            if(newCard.getCardNumber() == 7){
                player.setHasCountess(true);
                System.out.println(player.getName() + " drew the card 'Countess'.");
                if(player.getHand().getCard1().getCardNumber() == 5 ||
                   player.getHand().getCard2().getCardNumber() == 6){
                    discardCard(player, newCard);
                    player.setHasCountess(false);
                    if(deck.IsDeckEmpty() == false){
                        passTurn(player);
                        return;
                    } else{
                        endRound();
                        return;
                    }
                }
            }
            if(newCard.getCardNumber() == 5 || newCard.getCardNumber() == 6) {
                if (player.getHasCountess() == true) {

                    if (player.getHand().getCard1().getCardNumber() == 7) {
                        discardCard(player, player.getHand().getCard1());
                        player.setHasCountess(false);
                        if(deck.IsDeckEmpty() == false){
                            passTurn(player);
                            return;
                        } else{
                            endRound();
                            return;
                        }
                    }

                    if (player.getHand().getCard2().getCardNumber() == 7) {
                        discardCard(player, player.getHand().getCard2());
                        player.setHasCountess(false);
                        if(deck.IsDeckEmpty() == false){
                            passTurn(player);
                            return;
                        } else{
                            endRound();
                            return;
                        }
                    }
                }
            }
            server.sendMessageToOneClient(player, "Which card do you want to discard? Type $card1 or $card2.");

    }
    public synchronized void passTurn(ServerThread from){

        ServerThread nextPlayer;
        int indexOfNextPlayer;

        if(listOfActivePlayers.indexOf(from) + 1 < listOfActivePlayers.size()) {
            indexOfNextPlayer = listOfActivePlayers.indexOf(from) + 1;
        } else{
            indexOfNextPlayer = (listOfActivePlayers.indexOf(from) + 1) % listOfActivePlayers.size();
        }

        nextPlayer = listOfActivePlayers.get(indexOfNextPlayer);

        if(nextPlayer == null){
            System.out.println("There is no next player.");
            return;
        }

        if(nextPlayer.getIsInRound() == true){
            try {
                takeTurn(nextPlayer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            passTurn(nextPlayer);
        }
    }

    //-----------RoundMethods-------------------------------------------------------------------------------
    public void endRound() {

        //Inform all activePlayers that the round has ended.
        for (ServerThread player : this.listOfActivePlayers) {

            if (player.getIsInRound() == true) {

                server.sendMessageToOneClient(player, "The round has ended. Please reveal your hand!");
            }
            if (player.getIsInRound() == false) {

                server.sendMessageToOneClient(player, "The round has ended. All hands will be revealed.");
            }
        }

        //Reveal all hands
        for (ServerThread player : this.listOfActivePlayers) {
            server.sendMessageToOneClient(player, "Your " + player.getHand().toString() + ".");
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + "'s " + player.getHand().toString() + ".");
        }

        getRoundWinner();

        // Show tokens update
        for (ServerThread player : this.listOfActivePlayers) {
            server.sendMessageToOneClient(player, "You have " + player.getTokens() + " token(s).");
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + " has " + player.getTokens() + " token(s).");
            player.clearDiscardPile();
            player.getHand().clearHand();
        }

        // Determine if there is a game winner
        for (ServerThread player : server.getActivePlayerList()) {

            if (player.getTokens() == this.numberOfTokensToWin) {
                this.gameWinner = player;
                this.gameWinners.add(player);
            }
        }

        if (this.gameWinners.size() == 1) {
            server.sendMessageToAllActivePlayers(this.gameWinner.getName() + " is the winner!");
            server.gameOver();

            return;
        }

        if (this.gameWinners.size() > 1) {
            for (ServerThread winner : this.gameWinners) {
                server.sendMessageToAllActivePlayers(winner.getName() + " is a winner!");
                server.gameOver();
                return;
            }
        }

        startRound();
    }
    public void startRound() {

        this.deck.setUp();
        for (ServerThread activePlayer : this.listOfActivePlayers) {
            activePlayer.setIsInRound(true);
            playerInRound.put(activePlayer, true);
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

        try {
            takeTurn(initialPlayer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //---------------------------MethodsForPlayerSelecting----------------------------------------------------

    public void checkSelectable(ServerThread player){

        ArrayList<ServerThread> selectable = new ArrayList<>();

        for(ServerThread activePlayer : listOfActivePlayers){

            if(activePlayer == player){
                continue;
            }

            if(activePlayer.getIsInRound() == true && activePlayer.getIsProtected() == false){
                selectable.add(activePlayer);
            }

        }

        this.selectableList = selectable;
    }
    public String printSelectable(){

        StringBuilder message = new StringBuilder("Selectable players: ");
        for(int i = 0; i < this.selectableList.size() - 1; i++){
            message.append(this.selectableList.get(i).getName() + ", ");
        }
        message.append(this.selectableList.get(this.selectableList.size() - 1).getName() + ".");

        return String.valueOf(message);


    }

    //---------------------------MethodsForCardPlaying------------------------------------------------------------

    private void  discardCard(ServerThread player, Card card) throws IOException {

        if(card.getCardNumber() == 2 || card.getCardNumber() == 6 || card.getCardNumber() == 3){
            player.setPlayedSelection(false);
        }
        player.setDiscaredCard(card);
        player.getHand().removeFromHand(card);
        player.addToDiscardPile(card);
        String [] cardEffect = null;
        cardEffect = card.applyCardEffect(player);
        if(card.getCardNumber() == 2 || card.getCardNumber() == 6 || card.getCardNumber() == 3){
            if(selectableList.size() < 2){
                playSelection(player);
            }
        }

        if(card.getCardNumber() == 8){
            String messageForOwnClient  = cardEffect[0];
            String messageForEveryoneInRound = cardEffect[1];
            server.sendMessageToOneClient(player, messageForOwnClient);
            server.sendMessageToOneClient(player, "Your " + player.getDiscardPileRepresentation());
            server.sendMessageToAllActivePlayersExceptOne(player, messageForEveryoneInRound);
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + "'s " + player.getDiscardPileRepresentation());
        } else if(card.getCardNumber() == 7) {
            String messageForOwnClient = cardEffect[0];
            String messageForEveryoneInRound = cardEffect[1];
            server.sendMessageToOneClient(player, messageForOwnClient);
            server.sendMessageToOneClient(player, "Your " + player.getDiscardPileRepresentation());
            server.sendMessageToAllActivePlayersExceptOne(player, messageForEveryoneInRound);
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + "'s " + player.getDiscardPileRepresentation());
        } else if(card.getCardNumber() == 2) {
            String messageForOwnClient = cardEffect[0];
            String messageForEveryoneInRound = cardEffect[1];
            server.sendMessageToOneClient(player, messageForOwnClient);
            server.sendMessageToOneClient(player, "Your " + player.getDiscardPileRepresentation());
            server.sendMessageToAllActivePlayersExceptOne(player, messageForEveryoneInRound);
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + "'s " + player.getDiscardPileRepresentation());
        }else if(card.getCardNumber() == 6) {
            String messageForOwnClient = cardEffect[0];
            String messageForEveryoneInRound = cardEffect[1];
            server.sendMessageToOneClient(player, messageForOwnClient);
            server.sendMessageToOneClient(player, "Your " + player.getDiscardPileRepresentation());
            server.sendMessageToAllActivePlayersExceptOne(player, messageForEveryoneInRound);
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + "'s " + player.getDiscardPileRepresentation());
        }else if(card.getCardNumber() == 3) {
            String messageForOwnClient = cardEffect[0];
            String messageForEveryoneInRound = cardEffect[1];
            server.sendMessageToOneClient(player, messageForOwnClient);
            server.sendMessageToOneClient(player, "Your " + player.getDiscardPileRepresentation());
            server.sendMessageToAllActivePlayersExceptOne(player, messageForEveryoneInRound);
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + "'s " + player.getDiscardPileRepresentation());
        }else {
            server.sendMessageToOneClient(player, "You discarded " + card.toString() + ".");
            server.sendMessageToOneClient(player, "Your " + player.getDiscardPileRepresentation());
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + " discarded " + card.toString());
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + "'s " + player.getDiscardPileRepresentation());
            server.sendMessageToOneClient(player, "Your " + player.getHand().toString());
        }
    }
    public void playCard(ServerThread player){

        if(player.getPlayedSelection()==false){
            server.sendMessageToOneClient(player, "You cannot use this game command right now.");
            return;
        }

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

        if(player.getDiscardPile().getLast().getCardNumber() != 2 &&
           player.getDiscardPile().getLast().getCardNumber() != 6 &&
           player.getDiscardPile().getLast().getCardNumber() != 3) {
            player.setIsOnTurn(false);
        }


    }
    public boolean playSelection(ServerThread player) {

        ServerThread selected = null;

        if(selectableList.size() == 1){
            selected = selectableList.getFirst();
        } else if(selectableList.size() < 1) {

            player.setIsOnTurn(false);

            if (deck.IsDeckEmpty() == false) {
                server.getGame().passTurn(player);
            } else {
                server.getGame().endRound();
            }

            player.setPlayedSelection(true);
            return true;
        } else{

            for (ServerThread selectable : this.selectableList) {
                if (selectable.getName().equals(player.getNameOfChosenPlayer())) {
                    selected = selectable;
                }
            }
        }

        if(selected == null){
            return false;
        }

        if (player.getDiscaredCard().getCardNumber() == 2) {

            String showHand = selected.getName()
                                  + "'s " + selected.getHand().toString();
                server.sendMessageToOneClient(player, showHand);
            }


        if(player.getDiscaredCard().getCardNumber() == 6){

            Hand temp = player.getHand();
            player.setHand(selected.getHand());
            selected.setHand(temp);
            server.sendMessageToOneClient(player,"Your new " + player.getHand().toString());
            server.sendMessageToOneClient(selected,player.getName() +  " chose you and you traded cards!\n" +
                                                   "Your new " + selected.getHand().toString());

        }

        if(player.getDiscaredCard().getCardNumber() == 3){

            int playerScore = player.getHand().getHandScore();
            int selectedScore = selected.getHand().getHandScore();

            if(playerScore > selectedScore){

                server.sendMessageToOneClient(selected, "You are kicked out of the round.");
                server.sendMessageToAllActivePlayersExceptOne(selected, selected.getName() +" was kicked out of the round.");
                knockOutOfRound(selected);
            }

            if(selectedScore > playerScore){
                server.sendMessageToOneClient(player, "You are kicked out of the round.");
                server.sendMessageToAllActivePlayersExceptOne(player, player.getName() +" was kicked out of the round.");
                knockOutOfRound(player);
            }

            if(selectedScore == playerScore){
                server.sendMessageToAllActivePlayers("No one was kicked out of the round.");
            }
        }

        player.setIsOnTurn(false);

        if (deck.IsDeckEmpty() == false) {
            server.getGame().passTurn(player);
        } else {
            server.getGame().endRound();
        }

        player.setPlayedSelection(true);
        return true;
        }


    // --------InitializingMethods------------------------------------------------------------------
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
    private synchronized void getRoundWinner(){

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






}

