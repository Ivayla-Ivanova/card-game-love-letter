package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Priest extends Card {

    public Priest(Game game){
        super(2, "Priest");
        this.game = game;
    }

    @Override
    public String[] applyCardEffect(ServerThread player) throws IOException {
        String [] cardEffect = new String[2];
        game.checkSelectable(player);

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
