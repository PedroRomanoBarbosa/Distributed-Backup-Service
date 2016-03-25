package sdis.MulticastChannels;

import sdis.FileStorage;
import sdis.Peer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;

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

                file = fileStorage.getStoredFilesById(message[3]);

                //Se ainda nao tiver recebido nenhum chunk do ficheiro
                if (file == null) {
                    file = new sdis.File(message[4], Integer.parseInt(message[5]), 0);

                    file.getChunks().put(Integer.parseInt(message[4]), message[8].getBytes());
                    file.storeChunk(Integer.parseInt(message[4]));
                    fileStorage.addStoredFile(file);
                }

                //Se ja tiver recebido algum chunk do ficheiro apenas adiciona o chunk
                else {
                    file.getChunks().put(Integer.parseInt(message[4]), message[8].getBytes());
                    file.storeChunk(Integer.parseInt(message[4]));
                }


                //Cria resposta
                String responseHeader = "STORED " + "1.0" + " " + peer.getID() + " " + message[3] + " " + message[4] + " " + "\r\n\r\n";

                //Faz o sleep
                try {
                    Thread.sleep((long) (Math.random() * 400));
                } catch (InterruptedException e) {
                }

                //Tenta enviar
                try {
                    peer.getBackupSocket().send(responseHeader, peer.getMC_IP(), peer.getMC_PORT());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            default:
                break;
        }
    }
}