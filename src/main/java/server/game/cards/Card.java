package server.game.cards;

import server.ServerThread;

import java.io.IOException;

public abstract class Card {

    private int cardNumber;
    private String cardName;

    public Card(int cardNumber, String cardName) {

        this.cardNumber = cardNumber;
        this.cardName = cardName;

    }

    public abstract void applyCardEffect(ServerThread player) throws IOException;

    public int getCardNumber() {

        if (this == null) {
            return 0;
        } else {
            return this.cardNumber;
        }
    }
}




