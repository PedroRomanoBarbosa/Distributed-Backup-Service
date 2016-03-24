package sdis.MulticastChannels;

import sdis.FileStorage;
import sdis.Peer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MC implements Runnable {

    private Peer peer;
    private FileStorage fileStorage;

    public MC(Peer p, FileStorage fileSt) {
        peer = p;
        fileStorage = fileSt;
    }

    @Override
    public void run() {
        while (true) {

            try {
                DatagramPacket packet = peer.getControlSocket().receive();
                String message = new String(packet.getData(), 0, packet.getLength());

                try {
                    messageDealer(packet.getAddress(), message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {

            }
        }
    }

    private void messageDealer(InetAddress address, String messag) {
        String message[] = messag.split(" ");

        switch (message[0]) {
            case "STORED": {
                sdis.File file = fileStorage.getBackedUpFilesById(message[3]);

                if (file != null) {
                    try {
                        file.addChunkReplication(Integer.parseInt(message[4]), InetAddress.getByName(message[2]));
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }

            default:
                break;
        }
        }

    }
}
