

import java.io.IOException;
import java.net.MulticastSocket;


public class DataSocket extends MulticastSocket {

    public DataSocket(int port) throws IOException {
        super(port);
    }
}
