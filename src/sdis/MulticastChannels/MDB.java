package sdis.MulticastChannels;

import sdis.FileStorage;
import sdis.Peer;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class MDB implements Runnable {

    private Peer peer;
    private FileStorage fileStorage;

    public MDB(Peer p, FileStorage fileSt) {
        peer = p;
        fileStorage = fileSt;
    }

    @Override
    public void run() {
        while (true) {

            try {
                DatagramPacket packet = peer.getControlSocket().receivePacket(64000);
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
            case "PUTCHUNK": {

                //Se for owner do ficheiro, nao o armazena
                sdis.File file = fileStorage.getBackedUpFilesById(message[3]);

                if (file != null)
                    return;
            }

            default:
                break;
        }
    }
}