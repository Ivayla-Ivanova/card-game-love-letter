package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class King extends Card{

    public King(Game game){
        super(6, "King");
        this.game = game;
    }

    @Override
    public String [] applyCardEffect(ServerThread player) throws IOException {

        String [] cardEffect = new String[2];
        game.checkSelectable(player);
        if(game.getSelectableList().size() ==  1){
            cardEffect[0] = "You played the card 'King'. You are trading your card with the card of another player.";
            cardEffect[1] = player.getName() + " played the card 'King' and has traded his or her card with the card of another player.";
            return cardEffect;
        }

        if(game.getSelectableList().isEmpty()){

            cardEffect[0] = "You played the card 'King'. This card is discarded without any effect.";
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
