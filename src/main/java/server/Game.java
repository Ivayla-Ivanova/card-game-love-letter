package server;

import server.cards.Card;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *The Game class represents the game logic and manages relationships between players and game attributes.
 * It handles game mechanics such as turn management, player actions and game state transitions.
 */
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
    private HashSet<ServerThread> selectableSet;

    //------------------------------------------------------------------------------------------------------
    private Game(Server server, ArrayList<ServerThread> activePlayers) {

        this.server = server;
        this.deck = Deck.getInstance(this);
        this.listOfActivePlayers = activePlayers;

        resetGame();



    }

    /**
     * Initializes game attributes with default values.
     * This method is called when new game is created.
     */
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

    /**
     * Creates unique Game instance.
     */
    public static synchronized Game getInstance(Server server, ArrayList<ServerThread> activePlayers) {
        if (instance == null)
            instance = new Game(server, activePlayers);

        return instance;
    }


    //------------------Getter-Methods--------------------------------------------------------------------

    Server getGameServer() {
        return this.server;
    }

    /**
     *
     * @return List of players, who can be selected by a card effect.
     */
    public synchronized ArrayList<ServerThread> getSelectableList(){
        return this.selectableList;
    }

    //---------------RoundWinnersMethods------------------------------------------------------------------------------------------
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

    //---------------------------MethodsForCheck----------------------------------------------------

    /**
     * Checks if a player can pass the turn to the next player.
     * If not - the round ended.
     * @param player Sends a request to pass the turn to the next player.
     */
    public void checkMoveOn(ServerThread player){

        if (deck.IsDeckEmpty() == false) {
            if(checkPlayersInRound() > 1) {
                passTurn(player);
            } else{
                for(ServerThread activePlayer : listOfActivePlayers){
                    activePlayer.setIsOnTurn(Thread.currentThread(),false);
                }
                endRound();
            }
        } else {
            for(ServerThread activePlayer : listOfActivePlayers){
                activePlayer.setIsOnTurn(Thread.currentThread(),false);
            }
            endRound();
        }

    }

    /**
     * Updates the list of active players, who can be selected during the execution of a card effect.
     * @param player Sends request for an update
     */
    public void checkSelectable(ServerThread player){

        ArrayList<ServerThread> selectable = new ArrayList<>();

        for(ServerThread activePlayer : listOfActivePlayers){

            if(activePlayer == player && player.getHasDiscardedPrince() == false){
                continue;
            }

            if(activePlayer.getIsInRound() == true && activePlayer.getIsProtected() == false){
                selectable.add(activePlayer);
            }

        }

        this.selectableList = selectable;
    }

    /**
     * Builds a String out of the names of players, who can be selected during the execution of a card effect
     * and returns it.
     */
    public String printSelectable(){

        StringBuilder message = new StringBuilder("Selectable players: ");
        for(int i = 0; i < this.selectableList.size() - 1; i++){
            message.append(this.selectableList.get(i).getName() + ", ");
        }
        message.append(this.selectableList.get(this.selectableList.size() - 1).getName() + ".");

        return String.valueOf(message);


    }
    private int checkPlayersInRound(){

        int countPlayersInRound = 0;
        if(listOfActivePlayers == null || listOfActivePlayers.isEmpty()){
            return 0;
        }
        for(ServerThread activePlayer : listOfActivePlayers){

            if(activePlayer.getIsInRound() == true){
                countPlayersInRound = countPlayersInRound + 1;
            }
        }
        return countPlayersInRound;
    }

    //---------------------------MethodsForPlaying------------------------------------------------------------

    private void discardCard(ServerThread player, Card card){

        // Player Selection muss take place
        if(card.getCardNumber() == 2 || card.getCardNumber() == 6 || card.getCardNumber() == 3 ||
        card.getCardNumber() == 5 || card.getCardNumber() == 1){
            player.setPlayedSelection(false);
        }

        player.setDiscardedCard(card);
        player.getHand().removeFromHand(card);
        player.addToDiscardPile(card);
        String [] cardEffect = null;
        cardEffect = card.applyCardEffect(player);

        String messageForOwnClient  = cardEffect[0];
        String messageForEveryoneInRound = cardEffect[1];
        server.sendMessageToOneClient(player, messageForOwnClient);
        server.sendMessageToOneClient(player, "Your " + player.getDiscardPileRepresentation());
        server.sendMessageToAllActivePlayersExceptOne(player, messageForEveryoneInRound);
        server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + "'s " + player.getDiscardPileRepresentation());
        server.serverLog(Thread.currentThread(), player.getName() +" played card Nr: " + card.getCardNumber());

    }

    /**
     * Binds the String received as a game command by the ServerThread to a card from the player hand.
     * @param player passes the game command to the Game instance.
     */
    public void playCard(ServerThread player){

        // $card1 or $card1 was send from the user, $name was expected
        if(player.getPlayedSelection()==false || player.getHasChosenNumber() == false){
            server.sendMessageToOneClient(player, "You cannot use this game command right now.");
            return;
        }

        boolean cardNeedPlayerSelection;

        if(player.getReceivedCard() == "card1"){

            // check if card1 exists in the hand
            if(player.getHand().getCard1() == null){
                server.sendMessageToOneClient(player, "1.Card does not exist!");
                return;
            }

            cardNeedPlayerSelection = cardNeedGameCommand(player.getHand().getCard1());

            //if it exists discard the card
            discardCard(player, player.getHand().getCard1());

            // if discarded card does not need user input passTurn
            if(!cardNeedPlayerSelection){
                player.setIsOnTurn(Thread.currentThread(),false);
            }
        }
        if(player.getReceivedCard() == "card2"){

            // check if card1 exists in the hand
            if(player.getHand().getCard2() == null){
                server.sendMessageToOneClient(player, "2.Card does not exist!");
                return;
            }

            cardNeedPlayerSelection = cardNeedGameCommand(player.getHand().getCard2());

            discardCard(player, player.getHand().getCard2());

            // if discarded card does not need user input passTurn
            if(!cardNeedPlayerSelection){
                player.setIsOnTurn(Thread.currentThread(),false);
            }
        }


    }
    private boolean cardNeedGameCommand(Card card){

        if(card == null){
            server.serverLog(Thread.currentThread(), "Illegal argument - Card is null:");
            return false;
        }

        if(card.getCardNumber() == 2 || card.getCardNumber() == 3 ||
                card.getCardNumber() == 6 || card.getCardNumber() == 5 ||
        card.getCardNumber() == 1){
            return true;
        }
        return false;

    }

    private void playNumber(ServerThread selected, int number){

        if(selected.getHand().getCard1() != null){

            if(selected.getHand().getCard1().getCardNumber() == number){
                this.knockOutOfRound(selected);
                server.sendMessageToOneClient(selected,"You are kicked out of the round.");
                server.sendMessageToAllActivePlayersExceptOne(selected, selected.getName() + " was kicked out of the round.");
                return;
            }
        }

        if(selected.getHand().getCard2() != null){

            if(selected.getHand().getCard2().getCardNumber() == number){
                this.knockOutOfRound(selected);
                server.sendMessageToOneClient(selected,"You are kicked out of the round.");
                server.sendMessageToAllActivePlayersExceptOne(selected, selected.getName() + " was kicked out of the round.");
            }
        }

    }

    /**
     * This method contains the implementation of card effects affecting another player.
     * @param player is the ServerThread passing the selected player.
     * @return true if the card effect was successfully executed.
     */
    public boolean playSelection(ServerThread player) {

        if(player.getDiscaredCard().getCardNumber() == 1 && player.getHasChosenNumber() == false){
            server.sendMessageToOneClient(player, "First pick a number.");
            return false;
        }

        ServerThread selected = null;
        checkSelectable(player);

        //If players in round < 2 select a player automatically
        if(selectableList.size() == 1){
            selected = selectableList.getFirst();
        } else if(selectableList.isEmpty()) {

            player.setPlayedSelection(true);
            player.setIsOnTurn(Thread.currentThread(),false);
            checkMoveOn(player);
            return true;
        } else{

            checkSelectable(player);
            for (ServerThread selectable : this.selectableList) {
                if (selectable.getName().equals(player.getNameOfChosenPlayer())) {
                    selected = selectable;
                }
            }
        }

        if(selected == null){
            return false;
        }

        //Implementation of the card effect of 'Priest'
        if (player.getDiscaredCard().getCardNumber() == 2) {

            String showHand = selected.getName()
                                  + "'s " + selected.getHand().toString();
                server.sendMessageToOneClient(player, showHand);
                server.serverLog(Thread.currentThread(), "The effect of card 'Priest' was executed.");
            }

        // Implementation of the card effect of 'King'
        if(player.getDiscaredCard().getCardNumber() == 6){

            if(player.getHasCountess() == true){
                player.setHasCountess(false);
                selected.setHasCountess(true);
            }

            if(selected.getHasCountess() == true){
                player.setHasCountess(true);
                selected.setHasCountess(false);
            }

            Hand temp = player.getHand();
            player.setHand(selected.getHand());
            selected.setHand(temp);
            server.sendMessageToOneClient(player,"Your new " + player.getHand().toString());
            server.sendMessageToOneClient(selected,player.getName() +  " chose you and you traded cards!\n" +
                                                   "Your new " + selected.getHand().toString());
            server.serverLog(Thread.currentThread(), "The effect of card 'King' was executed.");

        }

        //Implementation of the card effect of 'Baron'
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

            server.serverLog(Thread.currentThread(), "The effect of card 'Baron' was executed.");
        }

        //Implementation of the card effect of 'Prince'
        if(player.getDiscaredCard().getCardNumber() == 5){

            Card discardedCard = selected.getHand().discardHand();
            selected.addToDiscardPile(discardedCard);
            selected.getHand().clearHand();

            if(discardedCard.getCardNumber() == 8){

                String [] cardEffect = null;
                cardEffect = discardedCard.applyCardEffect(selected);
                String messageForOwnClient  = cardEffect[0];
                String messageForEveryoneInRound = cardEffect[1];

                server.sendMessageToOneClient(selected, messageForOwnClient);
                server.sendMessageToOneClient(selected, "Your " + selected.getDiscardPileRepresentation());
                server.sendMessageToAllActivePlayersExceptOne(selected, messageForEveryoneInRound);
                server.sendMessageToAllActivePlayersExceptOne(selected, selected.getName() + "'s " + selected.getDiscardPileRepresentation());
            } else{
                server.sendMessageToOneClient(selected, "You discarded " + discardedCard.toString() + ".");
                server.sendMessageToOneClient(selected, "Your " + selected.getDiscardPileRepresentation());
                server.sendMessageToAllActivePlayersExceptOne(selected, selected.getName() + " discarded " + discardedCard.toString());
                server.sendMessageToAllActivePlayersExceptOne(selected, selected.getName() + "'s " + selected.getDiscardPileRepresentation());
                server.sendMessageToOneClient(selected, "Your " + selected.getHand().toString());
            }

            Card newCard;
            if(deck.IsDeckEmpty() == false){
               newCard = deck.drawCard();
            } else {
                newCard = deck.getTopCard();
            }
            selected.getHand().addToHand(newCard);
            server.sendMessageToOneClient(selected, "You drew a card.");
            server.sendMessageToAllActivePlayersExceptOne(selected, selected.getName() + " drew a card.");
            server.sendMessageToOneClient(selected, selected.getHand().toString());
            server.serverLog(Thread.currentThread(), "The effect of card 'Prince' was executed.");

        }

        if(player.getDiscaredCard().getCardNumber() == 1){

           int number =  player.getChosenNumber();

           playNumber(selected, number);
            server.serverLog(Thread.currentThread(), "The effect of card 'Guard' was executed.");

        }

        player.setPlayedSelection(true);
        player.setIsOnTurn(Thread.currentThread(),false);

        checkMoveOn(player);
        return true;
    }

    /**
     * Removes ServerThread from the round.
     * @param player is removed from the round.
     */
    public void knockOutOfRound(ServerThread player) {
        player.setIsInRound(false);
        this.playerInRound.put(player, false);
        Card hand = player.getHand().discardHand();
        if(hand != null){
            player.addToDiscardPile(hand);
            player.getHand().clearHand();
        }

        server.serverLog(Thread.currentThread(), player.getName() +" was kicked out of the round.");

    }

    //----------------------------TurnMethods---------------------------------------------------------------
    private void takeInitialTurn(ServerThread player) {

        player.setIsOnTurn(Thread.currentThread(),true);
        player.setIsProtected(false);
        player.setHasDiscardedPrince(false);
        Card newCard = deck.drawCard();
        player.getHand().addToHand(newCard);

        if(newCard.getCardNumber() == 7){
            player.setHasCountess(true);
        }

        server.sendMessageToOneClient(player, "You drew a card.");
        server.sendMessageToAllActivePlayersExceptOne(player,player.getName() + " drew a card.");
        server.sendMessageToOneClient(player, player.getHand().toString());
        player.setIsOnTurn(Thread.currentThread(),false);
        server.serverLog(Thread.currentThread(), player.getName() + " drew a card.");
    }
    private void takeTurn(ServerThread player){

        player.setIsOnTurn(Thread.currentThread(),true);
        player.setIsProtected(false);
        player.setHasDiscardedPrince(false);
        Card newCard = deck.drawCard();
        player.getHand().addToHand(newCard);
        server.sendMessageToOneClient(player, "You drew a card.");
        server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + " drew a card.");
        server.sendMessageToOneClient(player, player.getHand().toString());
        server.serverLog(Thread.currentThread(), player.getName() + " drew a card.");

        // If the new card is 'Countess' and the old card is 'King' or 'Prince'
        if(newCard.getCardNumber() == 7){
            player.setHasCountess(true);
            server.serverLog(Thread.currentThread(),player.getName() + " drew the card 'Countess'.");
            if(player.getHand().getCard1() != null) {
                if (player.getHand().getCard1().getCardNumber() == 5 ||
                        player.getHand().getCard1().getCardNumber() == 6) {
                    discardCard(player, newCard);
                    player.setHasCountess(false);

                    player.setIsOnTurn(Thread.currentThread(), false);
                    checkMoveOn(player);
                    return;
                }
            }

            if(player.getHand().getCard2() != null) {
                if (player.getHand().getCard2().getCardNumber() == 5 ||
                        player.getHand().getCard2().getCardNumber() == 6) {
                    discardCard(player, newCard);
                    player.setHasCountess(false);

                    player.setIsOnTurn(Thread.currentThread(), false);
                    checkMoveOn(player);
                    return;
                }
            }

        }

        // If the new card is 'King' or 'Prince' and the old card is 'Countess'
        if(newCard.getCardNumber() == 5 || newCard.getCardNumber() == 6) {
            if (player.getHasCountess() == true) {

                if (player.getHand().getCard1().getCardNumber() == 7) {
                    discardCard(player, player.getHand().getCard1());
                    player.setHasCountess(false);

                    player.setIsOnTurn(Thread.currentThread(),false);
                    checkMoveOn(player);
                    return;
                }

                if (player.getHand().getCard2().getCardNumber() == 7) {
                    discardCard(player, player.getHand().getCard2());
                    player.setHasCountess(false);

                    player.setIsOnTurn(Thread.currentThread(),false);
                    checkMoveOn(player);
                    return;
                }
            }
        }

        server.sendMessageToOneClient(player, "Which card do you want to discard? Type $card1 or $card2.");

    }
    synchronized void passTurn(ServerThread from){

        if(from.getIsOnTurn(Thread.currentThread()) == true){
            System.out.println("Call was from passTurn method.");
            return;
        }

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
            takeTurn(nextPlayer);
        }else {
            passTurn(nextPlayer);
        }
    }

    //-----------RoundMethods-------------------------------------------------------------------------------
    private void endRound() {


        //Inform all activePlayers that the round has ended.
        for (ServerThread player : this.listOfActivePlayers) {

            if (player.getIsInRound() == true) {

                server.sendMessageToOneClient(player, "The round has ended. Please reveal your hand!");
            }
            if (player.getIsInRound() == false) {

                server.sendMessageToOneClient(player, "The round has ended. All hands will be revealed.");
                server.serverLog(Thread.currentThread(),"The round has ended. All hands will be revealed.");
            }
        }

        //Reveal all hands
        for (ServerThread player : this.listOfActivePlayers) {
            server.sendMessageToOneClient(player, "Your " + player.getHand().toString() + ".");
            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() + "'s " + player.getHand().toString() + ".");
        }

        getRoundWinner();

        // All players are kicked out of the round
        for(ServerThread activePlayer : listOfActivePlayers) {
            activePlayer.setIsInRound(false);
            playerInRound.put(activePlayer, false);
        }

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

    /**
     * Initialize some players' and game attributes with default values.
     * Executes the initial drawing of cards for players at the start of the game.
     * Passes the turn to the first player.
     */
    public void startRound() {

        //Preparing for the start of a new round
        for (ServerThread activePlayer : this.listOfActivePlayers) {
            activePlayer.setIsInRound(true);
            playerInRound.put(activePlayer, true);
            activePlayer.setIsOnTurn(Thread.currentThread(),false);
        }

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
        clearRoundWinners();
        reSortListOfActivePlayers(initialPlayer);

        for (int i = 0; i < this.listOfActivePlayers.size(); i++) {
            takeInitialTurn(this.listOfActivePlayers.get(i));
        }

        takeTurn(initialPlayer);

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
        int countPlayersWithSameLastDate = 0;

        if(roundWinners.size() == 1) {
            initialPlayer = this.roundWinner;
            server.sendMessageToOneClient(initialPlayer, "You won in the last round. Now it's your turn to go first in this round.");
            server.sendMessageToAllActivePlayersExceptOne(initialPlayer, initialPlayer.getName() + " won the last round. Now " + initialPlayer.getName() + " goes first.");

            return initialPlayer;
        }

        if(roundWinners.size() < 1) {

            // Get count of players with same LastDate
            for (ServerThread player : listOfActivePlayers) {
                if (player.getDaysSinceLastDate() == getLowestDaysSinceDate()) {
                    countPlayersWithSameLastDate = countPlayersWithSameLastDate + 1;
                }
            }

            // if there is one player with lowest LastDate
                if(countPlayersWithSameLastDate == 1){
                    for(ServerThread player : listOfActivePlayers){
                        if (player.getDaysSinceLastDate() == getLowestDaysSinceDate()) {
                            initialPlayer = player;
                            server.sendMessageToOneClient(player, "You had recently a date. Now it's your turn to go first in this round.");
                            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() +
                                    " had recently a date. Now " + player.getName() + " goes first.");
                        }
                    }

                    return initialPlayer;
                }

                // there are multiple players with same lowest LastDate
                if(countPlayersWithSameLastDate > 1){
                    for(ServerThread player : listOfActivePlayers){
                        if (player.getDaysSinceLastDate() == getLowestDaysSinceDate()
                                && player.getAge() == getLowestAge()) {
                            initialPlayer = player;
                            server.sendMessageToOneClient(player, "You had recently a date and you are the youngest person. Now it's your turn to go first in this round.");
                            server.sendMessageToAllActivePlayersExceptOne(player, player.getName() +
                                    " is the youngest person and had recently a date. Now " + player.getName() + " goes first.");
                            return initialPlayer;
                        }
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

        int minDaysSinceLastDate = 2000;
        for (ServerThread client : server.getActivePlayersList()) {

            if (client.getDaysSinceLastDate() < minDaysSinceLastDate) {
                minDaysSinceLastDate = client.getDaysSinceLastDate();
            }
        }

        return minDaysSinceLastDate;

    }
    private int getLowestAge(){

        int minAge = 150;
        for (ServerThread client : server.getActivePlayersList()) {

            if (client.getAge() < minAge) {
                minAge = client.getDaysSinceLastDate();
            }
        }

        return minAge;

    }
    private synchronized void getRoundWinner(){

            int highestScore = 0;

            // Determine highestScore of a hand and winner if no tie
            for(ServerThread player : this.listOfActivePlayers){

                if(player.getIsInRound() == true) {
                    if (player.getHand().getHandScore() > highestScore) {
                        server.serverLog(Thread.currentThread(), player.getName() + "'s HandScore: " + player.getHand().getHandScore());
                        highestScore = player.getHand().getHandScore();
                        this.roundWinner = player;
                    }
                }
            }

            server.serverLog(Thread.currentThread(),"HighestScore: " + highestScore);


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
                    server.serverLog(Thread.currentThread(), String.valueOf(message));
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

