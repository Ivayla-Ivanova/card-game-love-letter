package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Prince extends Card{

    public Prince(Game game){
        super(5, "Prince");
        this.game = game;
    }

    @Override
    public String[] applyCardEffect(ServerThread player) throws IOException {

        //Not implemented
        return null;
    }

    @Override
    public String toString() {
        return "Prince(5)";
    }


}
