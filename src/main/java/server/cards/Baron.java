package server.cards;

import server.ServerThread;
import server.Game;


/**
 * Class for creating an instance of type 'Baron' card.
 */
public class Baron extends Card{

    public Baron(Game game){
        super(3, "Baron");
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
        game.checkSelectable(player);

        if(game.getSelectableList().isEmpty()){

            cardEffect[0] = "You played the card 'Baron'. This card is discarded without any effect. Type any player $name to end your turn.";
            cardEffect[1] = player.getName() + " played the card 'Baron'. This card is discarded without any effect. ";
            return cardEffect;
        }

        cardEffect[0] = "You played the card 'Baron'. Choose a player and secretly compare hands with another player; lower number is out.\n"
                        + game.printSelectable() + "\nType $name to choose a player.";
        cardEffect[1] = player.getName() + " played the card 'Baron'. "+
                        player.getName() +" will choose a player and secretly compare hands; lowe number is out.";
        return cardEffect;
    }

    @Override
    public String toString() {
        return "Baron(3)";
    }
}
