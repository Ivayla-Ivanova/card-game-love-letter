package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Countess extends Card{

    public Countess(){
        super(7, "Countess");
    }

    @Override
    public void applyCardEffect(ServerThread player) throws IOException {

        //not implemented
    }

    @Override
    public String toString() {
        return "Countess(7)";
    }


}
