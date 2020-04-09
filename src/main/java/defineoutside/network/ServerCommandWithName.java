package defineoutside.network;

import java.io.Serializable;

public class ServerCommandWithName implements Serializable {
    public String command;
    public String serverName;

    public ServerCommandWithName(String serverName, String command) {
        this.command = command;
        this.serverName = serverName;
    }
}
