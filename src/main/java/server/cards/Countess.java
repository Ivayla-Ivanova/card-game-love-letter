package server.cards;

import server.ServerThread;
import server.Game;


/**
 * Class for creating an instance of type 'Countess' card.
 */
public class Countess extends Card{

    public Countess(Game game){
        super(7, "Countess");
        this.game = game;
    }

    /**
     * Applies part of the card effect. The rest of the card effect is implemented in the class Game.
     * @param player The player who played the card.
     * @return String representation of the card effect.
     */
    @Override
    public String [] applyCardEffect(ServerThread player){

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
