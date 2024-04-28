package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Handmaid extends Card{

    public Handmaid(Game game){
        super(4, "Handmaid");
        this.game = game;
    }

    @Override
    public String [] applyCardEffect(ServerThread player) throws IOException {

        //not implemented
        return null;
    }

    @Override
    public String toString() {
        return "Handmaid(4)";
    }
}
