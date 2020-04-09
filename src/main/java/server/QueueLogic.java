package server;

import defineoutside.network.PlayerTransferData;
import defineoutside.network.ServerCommand;
import defineoutside.network.ServerInfo;

import java.util.*;

public class QueueLogic extends Thread {
    static HashMap<String, List<UUID>> queueForGametype = new HashMap<>();

    static HashMap<String, HashSet<ServerInfo>> allGamesOnNetwork = new HashMap<>();

    // Find games five times a second
    @Override
    public void run() {
        while (true) {
            for (String gameType : queueForGametype.keySet()) {
                searchForGames(queueForGametype.get(gameType), gameType);
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    // SearchForGames and findBestGame are separated to allow multiple game lobbies to be filled at once obeying max players
    private static void searchForGames(List<UUID> players, String gameType) {
        if (players.size() != 0) {
            ServerInfo bestGame = findBestGame(players, gameType);

            if (bestGame != null) {
                System.out.println(bestGame.serverUUID);
                String host = bestGame.serverUUID;

                Integer availableSlots = getMaxPlayers(gameType) - bestGame.playerCount;
                Boolean callAgain = false;

                System.out.println("Available slots are of " + availableSlots);

                if (players.size() > availableSlots) {
                    callAgain = true;
                    List<UUID> playersCanJoin = players.subList(0, availableSlots);
                    transferPlayers(playersCanJoin, host);

                } else {
                    transferPlayers(players, host);
                }

                // Prevent concurrent modification exception
                List<UUID> playerClone = new ArrayList<>();
                for (UUID player : players) {
                    playerClone.add(player);
                }

                for (UUID player : playerClone) {
                    removePlayerFromQueue(player);
                }

                // Do another loop if all players couldn't find a game
                if (callAgain) {
                    searchForGames(players.subList(availableSlots, players.size()), gameType);
                }
            } else {
                if (players.size() >= getMinPlayers(gameType)) {
                    if (players.size() >= getMinPlayers(gameType) && ClientHandler.openHosts.size() != 0) { // Don't waste servers or transfer without enough players to begin a new game
                        // Create a host with this gamemode
                        String randomOpenHost = ClientHandler.openHosts.get(0);
                        ClientHandler.addCommand(randomOpenHost, new ServerCommand("setgamemode " + gameType));

                        System.out.println("Created new game of gameType " + gameType);

                    } else {
                        System.out.println("We have ran out of open hosts!  Order more servers!");
                    }
                }
            }
        } else {
            queueForGametype.remove(gameType);
        }
    }

    private static ServerInfo findBestGame(List<UUID> players, String gameType) {
        HashSet<ServerInfo> availableGames = allGamesOnNetwork.get(gameType);
        HashSet<ServerInfo> availableLobbies = allGamesOnNetwork.get("gamelobby");

        List<String> gamemodesWithoutLobbies = new ArrayList<>();
        gamemodesWithoutLobbies.add("lobby");

        if (availableGames == null) {
            availableGames = new HashSet<ServerInfo>();
            allGamesOnNetwork.put(gameType, availableGames);
        }

        if (availableLobbies == null) {
            availableLobbies = new HashSet<ServerInfo>();
            allGamesOnNetwork.put("gamelobby", availableLobbies);
        }

        if (!gamemodesWithoutLobbies.contains(gameType)) {
            for (ServerInfo serverInfo : allGamesOnNetwork.get("gamelobby")) {
                System.out.println(serverInfo.lobbyType);
                if (serverInfo.lobbyType.equals(gameType) && serverInfo.playerCount < getMaxPlayers(gameType)) {
                    System.out.println("Returning 1 as " + serverInfo.lobbyType + " compared to " + gameType);
                    return serverInfo;
                }
            }
        }

        for (ServerInfo serverInfo : availableGames) {
            //Integer openSlots = getMaxPlayers(gameType) - serverInfo.playerCount;
            if (serverInfo.playerCount < getMaxPlayers(gameType)) {
                System.out.println("Returning 2");
                return serverInfo;
            }
        }

        return null;
    }

    public static void addPlayerToQueue(UUID uuid, String gametype) {
        List<UUID> currentPlayers = queueForGametype.get(gametype);

        if (currentPlayers == null) {

            List<UUID> list = new ArrayList<>();

            queueForGametype.put(gametype, list);
            currentPlayers = queueForGametype.get(gametype);
        }

        if (!currentPlayers.contains(uuid)) {
            currentPlayers.add(uuid);

            queueForGametype.put(gametype, currentPlayers);
            searchForGames(currentPlayers, gametype);
        }
    }

    public static void removePlayerFromQueue(UUID playerUUID) {
        for (String string : queueForGametype.keySet()) {
            queueForGametype.get(string).remove(playerUUID);
        }
    }

    public static int getMinPlayers(String gametype) {
        switch (gametype) {
            case "bedwars":
                return 2;
            case "lobby":
                return 1;
        }
        return Integer.MAX_VALUE;
    }

    public static int getMaxPlayers(String gametype) {
        switch (gametype) {
            case "bedwars":
                return 4;
        }
        return Integer.MAX_VALUE;
    }

    public static void transferPlayers(List<UUID> players, String host) {
        for (UUID uuid : players) {
            ClientHandler.addPlayerTransfer(new PlayerTransferData(uuid, host));
        }
    }

    public static void addGame(ServerInfo internalServerInfo) {
        HashSet<ServerInfo> gameList = allGamesOnNetwork.get(internalServerInfo.gameType);
        if (gameList == null) {
            gameList = new HashSet<>();
        }
        gameList.add(internalServerInfo);
        allGamesOnNetwork.put(internalServerInfo.gameType, gameList);
    }

    public static void removeGame(String serverName) {
        for (HashSet<ServerInfo> serverInfo : allGamesOnNetwork.values()) {
            for (ServerInfo servers : serverInfo) {
                if (servers.serverUUID.equalsIgnoreCase(serverName)) {
                    removeGame(servers);
                }
            }
        }
    }

    public static void removeGame(ServerInfo internalServerInfo) {
        HashSet<ServerInfo> gameList = allGamesOnNetwork.get(internalServerInfo.gameType);

        for (ServerInfo serverInfo : (HashSet<ServerInfo>) gameList.clone()) {
            if (serverInfo.gameType.equals(internalServerInfo.gameType) &&
                    serverInfo.playerCount == internalServerInfo.playerCount &&
                    (serverInfo.lobbyType == internalServerInfo.lobbyType || serverInfo.lobbyType.equals(internalServerInfo.lobbyType))
                    && serverInfo.gameUUID.equals(internalServerInfo.gameUUID)
                    && serverInfo.serverUUID.equals(internalServerInfo.serverUUID)) {
                gameList.remove(serverInfo);
            }
        }

        allGamesOnNetwork.put(internalServerInfo.gameType, gameList);
    }
}
