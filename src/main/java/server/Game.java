package server;

public class Game {

    private static Game instance = null;
    private Server server;


    private Game(Server server) {

        this.server = server;
    }

    public static synchronized Game getInstance(Server server) {
        if (instance == null)
            instance = new Game(server);

        return instance;
    }

    public static synchronized void resetInstance() {
        instance = null;
    }

}
