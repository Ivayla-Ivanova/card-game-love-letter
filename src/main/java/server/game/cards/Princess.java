package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Princess extends Card{

    public Princess(Game game){
        super(8, "Princess");
        this.game = game;
    }

    @Override
    public String [] applyCardEffect(ServerThread player) throws IOException {

        String [] cardEffect = new String[2];
        this.game.knockOutOfRound(player);

        cardEffect[0] = "You played the card 'Princess' and you are kicked out of the round.";
        cardEffect[1] = player.getName() + " played the card 'Princess' and was kicked out of the round.";
        return cardEffect;
    }

    @Override
    public String toString() {
        return "Princess(8)";
    }
}
