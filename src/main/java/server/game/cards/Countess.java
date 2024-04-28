package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Countess extends Card{

    public Countess(Game game){
        super(7, "Countess");
        this.game = game;
    }

    @Override
    public String [] applyCardEffect(ServerThread player) throws IOException {

        String [] cardEffect = new String[2];

        player.setHasCountess(false);

        cardEffect[0] = "You played the card 'Countess'.";
        cardEffect[1] = player.getName() + " played the card 'Countess'.";
        return cardEffect;
    }

    @Override
    public String toString() {
        return "Countess(7)";
    }


}
