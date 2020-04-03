package server;

import java.util.UUID;

public class GameInfo {
    String host;
    String gameType;
    UUID gameUUID;
    int Players;
    public GameInfo(String host, String gameType, UUID gameUUID, int Players) {
        this.host = host;
        this.gameType = gameType;
        this.gameUUID = gameUUID;
        this.Players = Players;
    }

    public String getHost() {
        return host;
    }

    public String getGameType() {
        return gameType;
    }

    public UUID getGameUUID() {
        return gameUUID;
    }

    public int getPlayers() {
        return Players;
    }
}
