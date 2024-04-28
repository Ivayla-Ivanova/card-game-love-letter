package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Priest extends Card {

    public Priest(){
        super(2, "Priest");
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
