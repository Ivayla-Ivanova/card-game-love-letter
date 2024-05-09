package server.cards;

import server.ServerThread;
import server.Game;

/**
 * Class for creating an instance of type 'Handmaid' card.
 */
public class Handmaid extends Card{

    public Handmaid(Game game){
        super(4, "Handmaid");
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

        player.setIsProtected(true);

        cardEffect[0] = "You played the card 'Handmaid'. " +
                "You are now protected against the effects of other played cards until your next turn.";
        cardEffect[1] = player.getName() + " played the card 'Handmaid'. " +
        player.getName() + " is now protected against the effects of other played cards until his or her next turn.";
        return cardEffect;
    }

    @Override
    public String toString() {
        return "Handmaid(4)";
    }
}
