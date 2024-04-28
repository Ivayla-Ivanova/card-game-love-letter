package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

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

    public abstract String [] applyCardEffect(ServerThread player) throws IOException;

    public int getCardNumber() {

        if (this == null) {
            return 0;
        } else {
            return this.cardNumber;
        }
    }
}




