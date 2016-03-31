package sdis;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;


public class DataSocket extends MulticastSocket {

    public DataSocket(int port) throws IOException {
        super(port);
    }

    @Deprecated
    public String receive(int size) throws IOException {
        byte[] buffer = new byte[size];
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
        this.receive(packet);
        return new String(packet.getData(),packet.getOffset(),packet.getLength());
    }

    public byte[] receiveData(int size) throws IOException {
        byte[] buffer = new byte[size];
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
        this.receive(packet);
        return Arrays.copyOfRange(packet.getData(),packet.getOffset(),packet.getLength());
    }

    public DatagramPacket receivePacket(int size) throws IOException {
        byte[] buffer = new byte[size];
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
        this.receive(packet);
        return packet;
    }

    public void send(String m, InetAddress ip, int port) throws IOException {
        byte[] buffer = m.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length,ip,port);
        this.send(packet);
    }

    public void sendPacket(byte[] m, InetAddress ip, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(m,m.length,ip,port);
        this.send(packet);
    }
}
