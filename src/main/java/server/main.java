package server;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.mongodb.connection.Server;
import defineoutside.network.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class main {
    private static char[] token = "P-G0GvUxVWlhDOL8v_0UF6PjjSWCnaiHqyxX9JhB5cBI7x4a8H0St42gfr0hTawZVfA5m2_dYLYpkS-eqvC4oQ==".toCharArray();
    public static HashMap<String, Integer> templateVersions = new HashMap<>();
    public static HashMap<String, File> templateZips = new HashMap<>();

    //static InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://192.168.1.123:9999", token);

    // Simple program that outputs everything that it receives
    public static void main(String args[]) throws IOException {
        File templateList = new File(Paths.get("").toAbsolutePath() + File.separator + "version.txt");

        // Get all the templates and their version
        if (templateList.exists()) {
            Scanner hostnameReader = new Scanner(templateList);
            while (hostnameReader.hasNextLine()) {
                String parseVersion = hostnameReader.nextLine();
                String template = parseVersion.substring(0, parseVersion.indexOf(","));
                int version = Integer.parseInt(parseVersion.substring(parseVersion.indexOf(",") + 1));

                templateVersions.put(template, version);
            }
        }

        // Make the template folder
        File templateFolder = new File(Paths.get("").toAbsolutePath() + File.separator + "templates");
        if (!templateFolder.exists()) {
            templateFolder.mkdir();
        }

        // Read all the templates and make zips based on version
        for (String template : templateVersions.keySet()) {
            File templateDir = new File(templateFolder + File.separator + template);
            File zippedDir = new File(templateFolder + File.separator + template + templateVersions.get(template) + ".zip");

            // There is a new template version, but it doesn't exist yet!  So make a zip!
            if (templateDir.isDirectory() && !zippedDir.exists()) {
                compress(templateDir.getPath());
            }

            // Add the template file to the template files
            templateZips.put(template, zippedDir);
        }

        try {
            ServerSocket server = new ServerSocket(27469);

            Thread input = new Input();
            input.start();

            QueueLogic findGamesThread = new QueueLogic();
            findGamesThread.start();

            while (true) {
                Socket s = server.accept();

                //System.out.println("Assigning new thread for this client");

                // create a new thread object
                Thread t = new ClientHandler(s);

                // Invoking the start() method
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Borrowed from https://stackoverflow.com/questions/1091788/how-to-create-a-zip-file-in-java
    public static void compress(String dirPath) {
        final Path sourceDir = Paths.get(dirPath);
        String zipFileName = dirPath.concat(".zip");
        try {
            final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = sourceDir.relativize(file);
                        outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //public static InfluxDBClient getInfluxDBClient() {
    //    return influxDBClient;
    //}
}

class Input extends Thread {
    @Override
    public void run() {
        Scanner input = new Scanner(System.in);
        while (true) {
            String command = input.nextLine();
            System.out.println("Received command " + command);

            if (command.equalsIgnoreCase("openhosts")) {
                for (String host : ClientHandler.openHosts) {
                    System.out.println("Open host: " + host);
                }
            }

            if (command.equalsIgnoreCase("queue")) {
                for (String type : QueueLogic.queueForGametype.keySet()) {
                    for (UUID playerUUID : QueueLogic.queueForGametype.get(type)) {
                        System.out.println("Player " + playerUUID + " is in queue for " + type);
                    }
                }
            }

            if (command.equalsIgnoreCase("servers")) {
                for (String type : QueueLogic.allGamesOnNetwork.keySet()) {
                    for (ServerInfo serverInfo : QueueLogic.allGamesOnNetwork.get(type)) {
                        System.out.println(type + " is on host " + serverInfo.serverUUID);
                    }
                }
            }
        }
    }
}

class ClientHandler extends Thread {
    final Socket s;

    static List<PlayerTransferData> transferPlayer = new Vector<>();
    static ConcurrentHashMap<String, List<ServerCommand>> sendCommand = new ConcurrentHashMap<>();
    static List<AddNonSubServer> addRegularServer = new ArrayList<>();
    static List<HubAddOrRemove> addHub = new ArrayList<>();

    static List<String> openHosts = new Vector<>();

    HashSet<ServerInfo> lastReceivedServerInfo = new HashSet<>();

    //static HashMap<String, DataStatistics> hostInfo = new HashMap<>();

    // Constructor
    public ClientHandler(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {

        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;

        String received = "";
        String hostName = "";

        // Make sure that the client is correct, after sending secret pass code
        try {
            dataOutputStream = new DataOutputStream(s.getOutputStream());
            dataOutputStream.writeUTF("CRva7SfCPaiBrS7cZh6bNXuupO0qnfOYrgOCZceQFWcFjbiksI1mgcUyhO31AZtz10k6Kj8Ji5XQ0pMObC2BXEKg2XptcVjFdGf");

            dataInputStream = new DataInputStream(s.getInputStream());
            received = dataInputStream.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!received.equals("4v2NZ8RTar54k4PoYEsnjxpL0IObNMgediJQP65QwUwmm9hBw1hQCJvxcSo6tIDwiHY2RkYzmVMWIpN8Oe4rrmPxVum2PBwBnL6")) {
            return;
        }

        System.out.println("Authentication complete");

        DataInputStream dataInputStream1 = null;
        try {
            dataInputStream1 = new DataInputStream(s.getInputStream());
            hostName = dataInputStream1.readUTF();

            //System.out.println(hostName + " is the hostname");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String internalName = hostName.substring(0, hostName.indexOf("#"));

        // This is the only output this app currently has
        if (hostName.equalsIgnoreCase("MainBungee#Input")) {
            System.out.println(hostName + " is now receiving commands");

            //transferPlayer.add(new PlayerTransferData(UUID.fromString("aadd63d0-e545-4cc2-8449-734a6dba0b85"), "test2"));
            //sendCommand.add(new ServerCommand("sub command Lobby say Hello world!!1"));

            try {

                while (true) {
                    // Add servers before transfer players to prevent a bad server from breaking everything
                    synchronized (addRegularServer) {
                        for (AddNonSubServer addNonSubServer : addRegularServer) {
                            // This keep spamming the log every minute, so it's commented out for now
                            //System.out.println("Adding " + addNonSubServer.getName() + " To the proxy");
                            objectOutputStream = new ObjectOutputStream(s.getOutputStream());
                            objectOutputStream.writeObject(addNonSubServer);
                        }
                        addRegularServer.clear();
                    }

                    // Send the queue'd stuff
                    synchronized (transferPlayer) {

                        for (PlayerTransferData playerTransferData : transferPlayer) {
                            // Only send players to ready servers
                            System.out.println(playerTransferData.getPlayerUUID() + playerTransferData.getSubServer() + " is a player transfer object");
                            objectOutputStream = new ObjectOutputStream(s.getOutputStream());
                            objectOutputStream.writeObject(playerTransferData);
                        }
                        transferPlayer.clear();
                    }



                    /*synchronized (addHub) {
                        for (HubAddOrRemove hubAddOrRemove : addHub) {
                            objectOutputStream = new ObjectOutputStream(s.getOutputStream());
                            objectOutputStream.writeObject(hubAddOrRemove);
                        }
                        addRegularServer.clear();
                    }*/

                    Thread.sleep(50);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }

        if (hostName.contains("#Input")) {
            try {
                objectOutputStream = new ObjectOutputStream(s.getOutputStream());

                List<ServerCommand> filledList = new ArrayList<>();
                filledList.add(new ServerCommand("version"));
                sendCommand.put(internalName, filledList);

                //System.out.println("1");

                while (true) {
                    //System.out.println("2");
                    List<ServerCommand> commands = sendCommand.get(internalName);


                    //System.out.println("3");

                    for (ServerCommand serverCommand : commands) {
                        System.out.println("SENDING SERVER COMMAND " + serverCommand.getCommand());

                        //System.out.println("4");
                        objectOutputStream.writeObject(serverCommand);
                        objectOutputStream.reset();
                        //System.out.println("5");
                    }

                    //System.out.println("6");
                    sendCommand.put(internalName, new ArrayList<ServerCommand>());
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (hostName.contains("#Register")) {
            try {
                objectInputStream = new ObjectInputStream(s.getInputStream());
                Object receivedObject = objectInputStream.readObject();

                AddNonSubServer addNonSubServer = (AddNonSubServer) receivedObject;
                addNonSubServer(addNonSubServer);

                //System.out.println("Successfully added a nonsub server");

                // This is all #Register sends
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        //WriteApi writeApi = main.influxDBClient.getWriteApi();

        // Bukkit is just the InfluxDB bucket, but this is Minecraft so it's Bukkit to be cool.
        String bukkit = "Abyss";
        String org = "AbyssMC";

        System.out.println(hostName + " has began to send data");

        if (hostName.contains("#Statistics")) {
            while (true) {
                try {
                    objectInputStream = new ObjectInputStream(s.getInputStream());
                    Object receivedObject = null;

                    try {
                        receivedObject = objectInputStream.readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (receivedObject instanceof DataStatistics) {
                        DataStatistics data = (DataStatistics) receivedObject;

                        /*writeApi.writeRecord(bukkit, org, WritePrecision.NS, "AppCPU,location=" + hostName + " percent=" + data.getAppCPU());
                        writeApi.writeRecord(bukkit, org, WritePrecision.NS, "SystemCPU,location=" + hostName + " percent=" + data.getSystemCPU());
                        writeApi.writeRecord(bukkit, org, WritePrecision.NS, "AppMemoryUsed,location=" + hostName + " bytes=" + data.getAppMemoryUsed());
                        writeApi.writeRecord(bukkit, org, WritePrecision.NS, "AppMemoryAvailable,location=" + hostName + " bytes=" + data.getAppMemoryAvailable());
                        writeApi.writeRecord(bukkit, org, WritePrecision.NS, "SystemMemoryAvailable,location=" + hostName + " bytes=" + data.getSystemMemoryAvailable());
                        writeApi.writeRecord(bukkit, org, WritePrecision.NS, "SystemMemoryTotal,location=" + hostName + " bytes=" + data.getSystemMemoryTotal());*/

                        HashSet<ServerInfo> givenServerInfo = data.getGamesInfo();

                        if (givenServerInfo.size() == 1) {
                            ServerInfo onlyInfo = givenServerInfo.iterator().next();

                            //System.out.println("Player count is " + onlyInfo.playerCount);
                            if (onlyInfo.gameType.equalsIgnoreCase("gamelobby") && onlyInfo.playerCount == 0) {

                                if (!openHosts.contains(internalName)) {
                                    System.out.println(internalName + " has been added as an open host");
                                    openHosts.add(internalName);
                                }
                            } else {
                                openHosts.remove(internalName);
                            }
                        }

                        // This means that it changed (Yay optimization!)
                        if (!lastReceivedServerInfo.equals(givenServerInfo)) {
                            for (ServerInfo serverInfo : lastReceivedServerInfo) {
                                Boolean isEqual = false;

                                //System.out.println("Player count of " + serverInfo.gameType + " is " + serverInfo.playerCount);

                                if (lastReceivedServerInfo != null) {
                                    for (ServerInfo newInfo : givenServerInfo) {
                                        //System.out.println("Player count of gametype " + newInfo.lobbyType + " is " + newInfo.playerCount);
                                        if (serverInfo.gameType.equals(newInfo.gameType) &&
                                                serverInfo.playerCount == newInfo.playerCount &&
                                                (serverInfo.lobbyType == newInfo.lobbyType || serverInfo.lobbyType.equals(newInfo.lobbyType))
                                                && serverInfo.gameUUID.equals(newInfo.gameUUID)
                                                && serverInfo.serverUUID.equals(newInfo.serverUUID)) {
                                            isEqual = true;
                                        }
                                    }
                                }

                                if (!isEqual) {
                                    System.out.println(serverInfo.gameType + " gametype has been removed!as lobby " + serverInfo.playerCount);
                                    QueueLogic.removeGame(serverInfo);
                                }

                            }

                            for (ServerInfo serverInfo : givenServerInfo) {
                                Boolean isEqual = false;

                                if (lastReceivedServerInfo != null) {
                                    for (ServerInfo newInfo : lastReceivedServerInfo) {
                                        if (serverInfo.gameType.equals(newInfo.gameType) &&
                                                serverInfo.playerCount == newInfo.playerCount &&
                                                (serverInfo.lobbyType == newInfo.lobbyType || serverInfo.lobbyType.equals(newInfo.lobbyType))
                                                && serverInfo.gameUUID.equals(newInfo.gameUUID)
                                                && serverInfo.serverUUID.equals(newInfo.serverUUID)) {
                                            isEqual = true;
                                        }
                                    }
                                }

                                if (!isEqual) {
                                    //System.out.println(serverInfo.serverUUID + " " + serverInfo.playerCount + " " + serverInfo.gameType + " " + serverInfo.gameUUID + " " + serverInfo.lobbyType);

                                    System.out.println(serverInfo.gameType + " gametype has been added! as lobby " + serverInfo.lobbyType);
                                    QueueLogic.addGame(serverInfo);
                                }
                            }

                            lastReceivedServerInfo = givenServerInfo;
                        }
                    }

                    Thread.sleep(50);

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();

                    for (ServerInfo deadServer : lastReceivedServerInfo) {
                        QueueLogic.removeGame(deadServer);
                    }
                    return;

                }
            }
        }

        if (hostName.contains("#Output")) {
            while (true) {
                try {
                    objectInputStream = new ObjectInputStream(s.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while (true) {
                    try {
                        Object receivedObject = null;

                        try {
                            receivedObject = objectInputStream.readObject();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (receivedObject instanceof QueueData) {
                            QueueData data = (QueueData) receivedObject;
                            UUID player = data.getPlayer().getPlayerUUID();
                            String gameType = data.getServer();

                            QueueLogic.addPlayerToQueue(player, gameType);

                            System.out.println(data.getPlayer() + " has joined the queue for " + data.getServer());
                        }

                        Thread.sleep(50);

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("Connection closed");
                        return;
                    }
                }
            }
        }

        if (hostName.contains("#Host")) {
            while (true) {
                try {
                    objectInputStream = new ObjectInputStream(s.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while (true) {
                    Object receivedObject = null;

                    try {
                        receivedObject = objectInputStream.readObject();
                    } catch (ClassNotFoundException | IOException e) {
                        return;
                    }

                    if (receivedObject instanceof HashMap) {
                        HashMap<String, Integer> templateVersions = (HashMap<String, Integer>) receivedObject;

                        for (String key : templateVersions.keySet()) {
                            System.out.println("Key " + key + " Version " + templateVersions.get(key));
                        }

                        try {
                            InputStream in = s.getInputStream();
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(main.templateZips.get("test")));

                            int c=0;
                            byte[] buff=new byte[2048];

                            while((c=in.read(buff))>0){ // read something from inputstream into buffer
                                // if something was read
                                bos.write(buff, 0, c);
                            }

                            in.close();
                            bos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    static void addCommand(String host, ServerCommand serverCommand) {
        List<ServerCommand> previousCommands = sendCommand.get(host);

        System.out.println("Added a new command! " + serverCommand.getCommand());
        if (previousCommands == null) {
            List<ServerCommand> list = new ArrayList<>();
            sendCommand.put(host, list);

            previousCommands = sendCommand.get(host);
        }
        previousCommands.add(serverCommand);

        sendCommand.put(host, previousCommands);
    }

    static void addPlayerTransfer(PlayerTransferData playerTransferData) {
        transferPlayer.add(playerTransferData);
    }

    static void addNonSubServer(AddNonSubServer addNonSubServer) {
        addRegularServer.add(addNonSubServer);
    }
}

