package server;

import server.game.cards.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerThread extends Thread{

    private String name;
    private List<ServerThread> activePlayers;
    private ServerThread serverThread;
    private Server server;
    private Hand hand;
    private ArrayList<Card> discardPile;
    private int tokens;
    private boolean isInRound;
    private boolean wonLastRound;
    private boolean isOnTurn;
    private Card playedCard;
    private int daysSinceLastDate;
    private String receivedCard;

    private boolean hasPlayedCard;
    private static Random randomGenerator = new Random();

    public PlayerThread(ServerThread serverThread, Server server){

        this.serverThread = serverThread;
        this.name = serverThread.getName();
        this.activePlayers = new ArrayList<>();
        this.server = server;


        resetPlayerAttributes();
    }

    public void run(){

        while (true){

        }

    }

    public void resetPlayerAttributes(){

        this.hand = new Hand();
        this.discardPile = new ArrayList<>();
        this.tokens = 0;
        this.isInRound = false;
        this.wonLastRound = false;
        this.isOnTurn = false;
        this.playedCard = null;
        this.daysSinceLastDate = randomGenerator.nextInt(366);
        this.receivedCard = null;
        this.hasPlayedCard = false;

    }
    public void setIsInRound(boolean value){
        this.isInRound = value;
    }
    public boolean getHasPlayedCard(){
        return this.hasPlayedCard;
    }
    public void setHasPlayedCard(boolean value){
        this.hasPlayedCard = value;
    }

    public String getReceivedCard(){
        return this.receivedCard;
    }

    public void setReceivedCard(String value){
        this.receivedCard = value;
    }

    public int getDaysSinceLastDate(){
        return this.daysSinceLastDate;
    }

    public boolean getWonLastRound(){
        return this.wonLastRound;
    }

    public void setIsOnTurn(boolean value){
        this.isOnTurn = value;
    }

    public Hand getHand(){
        return this.hand;
    }
}
