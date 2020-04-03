package server;

public class GameTypeInfo {
    public static int getMaxPlayers(String gameType) {
        switch (gameType) {
            case "bedwars":
                return 16;
        }
        return -1;
    }
}
