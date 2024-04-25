package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class King extends Card{

    public King(){
        super(6, "King");
    }

    @Override
    public void applyCardEffect(ServerThread player) throws IOException {

        //not implemented
    }

    @Override
    public String toString() {
        return "King(6)";
    }
}
