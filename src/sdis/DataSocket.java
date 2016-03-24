package sdis;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class DataSocket extends MulticastSocket {

    public DataSocket(int port) throws IOException {
        super(port);
    }

    public synchronized String receive(int size) throws IOException {
        byte[] buffer = new byte[size];
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
        this.receive(packet);
        return new String(packet.getData()).trim();
    }

    public synchronized DatagramPacket receivePacket(int size) throws IOException {
        byte[] buffer = new byte[size];
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
        this.receive(packet);
        return packet;
    }

    public synchronized void send(String m, InetAddress ip, int port) throws IOException {
        byte[] buffer = m.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length,ip,port);
        this.send(packet);
    }
}
