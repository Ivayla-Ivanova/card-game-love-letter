package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Guard extends Card{

    public Guard(){
        super(1, "Guard");
    }

    @Override
    public void applyCardEffect(ServerThread player) throws IOException {
        // not implemented
    }

    @Override
    public String toString() {
        return "Guard(1)";
    }
}
