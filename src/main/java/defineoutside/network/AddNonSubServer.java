package defineoutside.network;

import java.io.Serializable;
import java.net.InetAddress;

public class AddNonSubServer implements Serializable {
    String name;
    InetAddress inetAddress;
    int port;
    public AddNonSubServer(String name, InetAddress inetAddress, int port) {
        this.name = name;
        this.inetAddress = inetAddress;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
