package defineoutside.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class NetworkInfo implements Serializable {
    InputStream dataInputStream;
    OutputStream dataOutputStream;

    public NetworkInfo(InputStream inputStream, OutputStream outputStream) {
        this.dataInputStream = inputStream;
        this.dataOutputStream = outputStream;
    }

    public InputStream getDataInputStream() {
        return dataInputStream;
    }

    public OutputStream getDataOutputStream() {
        return dataOutputStream;
    }
}
