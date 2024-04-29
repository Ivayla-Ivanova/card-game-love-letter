package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Baron extends Card{

    public Baron(Game game){
        super(3, "Baron");
        this.game = game;
    }

    @Override
    public String [] applyCardEffect(ServerThread player) throws IOException {

        String [] cardEffect = new String[2];
        game.checkSelectable(player);

        if(game.getSelectableList().size() ==  1){
            cardEffect[0] = "You played the card 'Baron'. You are comparing secretly hands with another player; lower number is out.";
            cardEffect[1] = player.getName() + " played the card 'Baron' and compared secretly hands with another player; lowe number is out.";
            return cardEffect;
        }

        if(game.getSelectableList().isEmpty()){

            cardEffect[0] = "You played the card 'Baron'. This card is discarded without any effect.";
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
