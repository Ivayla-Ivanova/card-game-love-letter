package server.cards;

import server.ServerThread;
import server.Game;

/**
 * Class for creating an instance of type 'Princess' card.
 */
public class Princess extends Card{

    public Princess(Game game){
        super(8, "Princess");
        this.game = game;
    }

    /**
     * Apply part of the card effect. The rest of the card effect is implemented in the class Game.
     * @param player The player who played the card.
     * @return String representation of the card effect.
     */
    @Override
    public String [] applyCardEffect(ServerThread player){

        String [] cardEffect = new String[2];
        this.game.knockOutOfRound(player);

        cardEffect[0] = "You played the card 'Princess'.";
        cardEffect[1] = player.getName() + " played the card 'Princess'.";
        return cardEffect;
    }

    @Override
    public String toString() {
        return "Princess(8)";
    }
}
