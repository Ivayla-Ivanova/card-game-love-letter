package server;

import server.cards.*;
import java.util.*;

/**
 * The Deck class represents a deck of cards used in the game.
 * It manages the cards in the deck,
 * including initializing the deck with a standard set of cards and shuffling.
 */
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

    /**
     * Creates unique Deck instance.
     */
    public static synchronized Deck getInstance(Game game) {
        if (instance == null)
            instance = new Deck(game);

        return instance;
    }

    /**
     * Initialize the Deck with default values and prepares it for the start of a new round.
     */
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

    /**
    @return the top card of the deck
     */
    public Card getTopCard(){
        return this.topCard;
    }


    /**
     * @return the top card of the Deck
     */
    public Card drawCard(){

        if(listOfCards.isEmpty() || listOfCards == null){
            return null;
        }

        Card card;
        card = listOfCards.get(listOfCards.size() - 1);
        listOfCards.remove(listOfCards.size() -1);
        return card;


    }

    boolean IsDeckEmpty(){

        if(listOfCards.isEmpty() || listOfCards == null){
            return true;
        }

        return false;

    }

    /**
     * @return String representation of the second top three cards
     */
    public String printTopThreeCards(){

        return "The next three top cards are " + nextThreeTopCards[0].toString() + ", " +
               nextThreeTopCards[1].toString() + ", " +
               nextThreeTopCards[2].toString();

    }




}
