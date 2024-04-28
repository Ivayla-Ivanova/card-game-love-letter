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

        //not implemented
        return null;
    }

    @Override
    public String toString() {
        return "King(6)";
    }
}
