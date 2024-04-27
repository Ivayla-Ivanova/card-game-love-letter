package server.game.cards;

import server.ServerThread;
import server.game.Game;

import java.io.IOException;

public class Princess extends Card{

    public Princess(){
        super(8, "Princess");
    }

    @Override
    public void applyCardEffect(ServerThread player) throws IOException {

        /*
        player.sendingMessageToOwnClient("I played the card Princess and am kicked out of the round.");
        player.sendingToAllPlayersExceptMe(player.getName()
                + " played the card Princess and was kicked out of the round.");
*/
        Game.knockOutOfRound(player);
    }

    @Override
    public String toString() {
        return "Princess(8)";
    }
}
