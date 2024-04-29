package server.game;

import server.game.cards.*;

import java.util.*;


public class Deck {

    private static Deck instance = null;

    private final Game game;
    private  Card[] cardDeck;
    private Card topCard;

    List<Card> listOfCards;

    Random random;

    private Card[] nextThreeTopCards;

    private Deck(Game game) {
        this.game = game;
        this.cardDeck = new Card[16];
        this.topCard = null;
        this.nextThreeTopCards = new Card[3];
        this.listOfCards = new ArrayList<>();
        this.random = new Random();
    }

    public static synchronized Deck getInstance(Game game) {
        if (instance == null)
            instance = new Deck(game);

        return instance;
    }

    public void setUp(){

        cardDeck[0] = new Princess(game);
        cardDeck[1] = new Countess(game);
        cardDeck[2] = new King(game);
        cardDeck[3] = new Prince(game);
        cardDeck[4] = new Prince(game);
        cardDeck[5] = new Handmaid(game);
        cardDeck[6] = new Handmaid(game);
        cardDeck[7] = new Baron(game);
        cardDeck[8] = new Baron(game);
        cardDeck[9] = new Priest(game);
        cardDeck[10] = new Priest(game);
        cardDeck[11] = new Guard(game);
        cardDeck[12] = new Guard(game);
        cardDeck[13] = new Guard(game);
        cardDeck[14] = new Guard(game);
        cardDeck[15] = new Guard(game);

        List<Card> tempList = Arrays.asList(cardDeck);

        for(int i = 0; i < random.nextInt(12, 17); i++){
            Collections.shuffle(tempList);
            System.out.println(i + ": " + tempList);
        }

        listOfCards.addAll(tempList);
        this.topCard = drawCard();

        if(game.getGameServer().getActivePlayerCount() == 2){

            for(int i = 0; i < this.nextThreeTopCards.length; i++) {
                this.nextThreeTopCards[i] = drawCard();
            }
        }
    }

    public Card getTopCard(){
        return this.topCard;
    }


    public Card[] getNextThreeTopCards(){
        return this.nextThreeTopCards;
    }

    public Card drawCard(){

        if(listOfCards.isEmpty() || listOfCards == null){
            return null;
        }

        Card card;
        card = listOfCards.get(listOfCards.size() - 1);
        listOfCards.remove(listOfCards.size() -1);
        return card;


    }

    public boolean IsDeckEmpty(){

        if(listOfCards.isEmpty() || listOfCards == null){
            return true;
        }

        return false;

    }

    public String printTopThreeCards(){

        return "The next three top cards are " + nextThreeTopCards[0].toString() + ", " +
               nextThreeTopCards[1].toString() + ", " +
               nextThreeTopCards[2].toString();

    }




}
