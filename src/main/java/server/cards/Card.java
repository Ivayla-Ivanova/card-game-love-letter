package server.cards;

import server.ServerThread;
import server.Game;

/**
 * Abstract Card class with two constructors and default get-Method for the card number.
 */
public abstract class Card {

    private int cardNumber;
    private String cardName;

    protected Game game;

    public Card(int cardNumber, String cardName, Game game) {

        this.cardNumber = cardNumber;
        this.cardName = cardName;
        this.game = game;

    }

    public Card(int cardNumber, String cardName){
        this.cardNumber = cardNumber;
        this.cardName = cardName;
    }

    /**
     * This method have to be overridden by all type of cards.
     * @param player is the ServerThread, who wants to play the card.
     * @return String representation of the card effect.
     */
    public abstract String [] applyCardEffect(ServerThread player);

    public int getCardNumber() {

        if (this == null) {
            return 0;
        } else {
            return this.cardNumber;
        }
    }
}




