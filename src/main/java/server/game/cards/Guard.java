package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Guard extends Card{

    public Guard(Game game){
        super(1, "Guard");
        this.game = game;
    }

    @Override
    public String [] applyCardEffect(ServerThread player) throws IOException {
        // not implemented
        return null;
    }

    @Override
    public String toString() {
        return "Guard(1)";
    }
}
