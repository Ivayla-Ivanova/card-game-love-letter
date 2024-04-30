package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Prince extends Card{

    public Prince(Game game){
        super(5, "Prince");
        this.game = game;
    }

    @Override
    public String[] applyCardEffect(ServerThread player) throws IOException {

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
