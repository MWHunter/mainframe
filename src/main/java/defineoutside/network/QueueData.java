package defineoutside.network;

import defineoutside.creator.DefinePlayer;

import java.io.Serializable;
import java.util.UUID;

public class QueueData implements Serializable {
    DefinePlayer player;
    String server;
    public QueueData(DefinePlayer player, String server) {
        this.player = player;
        this.server = server;
    }

    public DefinePlayer getPlayer() {
        return player;
    }

    public String getServer() {
        return server;
    }
}
