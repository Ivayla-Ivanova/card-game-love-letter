package server.cards;

import server.ServerThread;
import server.Game;

/**
 * Class for creating an instance of type 'Prince' card.
 */
public class Prince extends Card{

    public Prince(Game game){
        super(5, "Prince");
        this.game = game;
    }

    /**
     * Applies part of the card effect. The rest of the card effect is implemented in the class Game.
     * @param player The player who played the card.
     * @return String representation of the card effect.
     */
    @Override
    public String[] applyCardEffect(ServerThread player){

        String [] cardEffect = new String[2];
        player.setHasDiscardedPrince(true);
        game.checkSelectable(player);

        if(game.getSelectableList().isEmpty()){

            cardEffect[0] = "You played the card 'Prince'. This card is discarded without any effect.";
            cardEffect[1] = player.getName() + " played the card 'Prince'. " +
                    "This card is discarded without any effect.";
            return cardEffect;
        }

        cardEffect[0] = "You played the card 'Prince'. Choose a player. " +
                "Their hand will be discarded without any effect, unless the card is 'Princess'." +
                " They will draw a new card.\n"
                + game.printSelectable() + "\nType $name to choose a player.";
        cardEffect[1] = player.getName() + " played the card 'Prince'. "+
                player.getName() +" will choose a player. " +
                "Their hand will be discarded without any effect, unless the card is 'Princess'. " +
                "They will draw a new card.";
        return cardEffect;

    }

    @Override
    public String toString() {
        return "Prince(5)";
    }


}
