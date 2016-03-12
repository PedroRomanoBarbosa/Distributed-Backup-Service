package com.sdis;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;

public class MulticastThread extends Thread{
    private String message;
    private MulticastSocket socket;
    private boolean active;

    public MulticastThread(MulticastSocket s){
        active = true;
        socket = s;
    }



    public void run() {
        while (active){
            byte[] buffer = new byte[65536];
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
