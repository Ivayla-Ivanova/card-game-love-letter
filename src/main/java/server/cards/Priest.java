package server.cards;

import server.ServerThread;
import server.Game;


/**
 * Class for creating an instance of type 'Priest' card.
 */
public class Priest extends Card {

    public Priest(Game game){
        super(2, "Priest");
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
        game.checkSelectable(player);

        if(game.getSelectableList().isEmpty()){

            cardEffect[0] = "You played the card 'Priest'. This card is discarded without any effect. Type any player $name to end your turn.";
            cardEffect[1] = player.getName() + " played the card 'Priest'. This card is discarded without any effect.";
            return cardEffect;
        }

        cardEffect[0] = "You played the card 'Priest'. Choose a player and secretly look at his or her hand of cards.\n"
                        + game.printSelectable() + "\nType $name to choose a player.";
        cardEffect[1] = player.getName() + " played the card 'Priest'. "+
                        player.getName() +" will choose a player and secretly look at his or her hand of cards.";
        return cardEffect;
    }

    @Override
    public String toString() {
        return "Priest(2)";
    }
}
