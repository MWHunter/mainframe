package server;

import java.util.List;
import java.util.Vector;

public class AllGamesList {
    static List<GameInfo> games = new Vector<>();

    public void addGame(GameInfo gameInfo) {
        games.add(gameInfo);
    }

    public void removeGame(GameInfo gameInfo) {
        games.remove(gameInfo);
    }

    public GameInfo findBestGame(String gameType) {
        Integer mostPlayers = 0;
        GameInfo bestGame = null;

        for (GameInfo gameInfo : games) {
            if (gameInfo.getGameType().equalsIgnoreCase(gameType)) {
                Integer gamePlayers = gameInfo.getPlayers();

                if (gamePlayers > mostPlayers && gamePlayers <= GameTypeInfo.getMaxPlayers(gameType)) {
                    mostPlayers = gamePlayers;
                    bestGame = gameInfo;
                }
            }
        }

        return bestGame;
    }
}
