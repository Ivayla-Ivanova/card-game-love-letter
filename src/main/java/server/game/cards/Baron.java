package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Baron extends Card{

    public Baron(){
        super(3, "Baron");
    }

    @Override
    public String [] applyCardEffect(ServerThread player) throws IOException {

        //not implemented
        return null;
    }

    @Override
    public String toString() {
        return "Baron(3)";
    }
}
