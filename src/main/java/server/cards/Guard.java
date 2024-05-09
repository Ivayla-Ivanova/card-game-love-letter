package server.cards;

import server.ServerThread;
import server.Game;

/**
 * Class for creating an instance of type 'Guard' card.
 */
public class Guard extends Card{

    public Guard(Game game){
        super(1, "Guard");
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
        player.setHasChosenNumber(false);

        if(game.getSelectableList().isEmpty()){

            cardEffect[0] = "You played the card 'Guard'. This card is discarded without any effect. Type any $number and any player $name to end your turn.";
            cardEffect[1] = player.getName() + " played the card 'Guard'. This card is discarded without any effect. ";
            return cardEffect;
        }

        cardEffect[0] = "You played the card 'Guard'. Name a number and choose a player.If the number matches the card number of the selected player - they are out.\n"
                + "Type $number to pick a number from 2 to 8.\n" + game.printSelectable() + "\nType $name to choose a player.";
        cardEffect[1] = player.getName() + " played the card 'Guard'. "+
                player.getName() +" will choose a player and pick a number.\nIf the selected player's card matches the number they are out.";
        return cardEffect;
    }

    @Override
    public String toString() {
        return "Guard(1)";
    }
}
