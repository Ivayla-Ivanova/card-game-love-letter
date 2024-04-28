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

        // not implemented
        return null;
    }

    @Override
    public String toString() {
        return "Priest(2)";
    }
}
