package server.cards;

import server.ServerThread;
import server.Game;


/**
 * Class for creating an instance of type 'King' card.
 */
public class King extends Card{

    public King(Game game){
        super(6, "King");
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
        game.checkSelectable(player);

        if(game.getSelectableList().isEmpty()){

            cardEffect[0] = "You played the card 'King'. This card is discarded without any effect. Type any player $name to end your turn.";
            cardEffect[1] = player.getName() + " played the card 'King'. This card is discarded without any effect.";
            return cardEffect;
        }

        cardEffect[0] = "You played the card 'King'. Choose a player and trade your card with his or her card.\n"
                        + game.printSelectable() + "\nType $name to choose a player.";
        cardEffect[1] = player.getName() + " played the card 'King'. "+
                        player.getName() +" will choose a player and trade his or her hand of cards with another player.";
        return cardEffect;
    }

    @Override
    public String toString() {
        return "King(6)";
    }
}
