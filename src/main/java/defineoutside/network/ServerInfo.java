package defineoutside.network;

import java.io.Serializable;

public class ServerInfo implements Serializable {
    public String gameUUID;
    public String serverUUID;
    public String gameType;
    public String lobbyType;
    public int playerCount;

    public ServerInfo(String gameUUID, String serverUUID, String gameType, String lobbyType, int playerCount) {
        this.gameUUID = gameUUID;
        this.serverUUID = serverUUID;
        this.gameType = gameType;
        this.playerCount = playerCount;
        this.lobbyType = lobbyType;
    }
}
