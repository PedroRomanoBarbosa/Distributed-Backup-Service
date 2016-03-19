import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;


public class DataSocket extends MulticastSocket {

    public DataSocket(int port) throws IOException {
        super(port);
    }

    public String receive(int size) throws IOException {
        byte[] buffer = new byte[size];
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
        this.receive(packet);
        return new String(packet.getData()).trim();
    }

    public void send(int size) throws IOException {
        byte[] buffer = new byte[size];
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
        this.send(packet);
    }
}
