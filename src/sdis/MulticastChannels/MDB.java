package sdis.MulticastChannels;

import sdis.FileStorage;
import sdis.Peer;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MDB extends Thread {

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
                DatagramPacket packet = peer.getBackupSocket().receivePacket(64000);
                //String message = new String(packet.getData(), 0, packet.getLength());

                try {
                    messageDealer(packet.getAddress(), Arrays.copyOf(packet.getData(), packet.getLength()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {

            }
        }
    }

    private void messageDealer(InetAddress address, byte[] messag) throws UnsupportedEncodingException {
        String message[] = new String(messag, "UTF-8").split(" ");

        switch (message[0]) {
            case "PUTCHUNK": {

                //Se for owner do ficheiro, nao o armazena
                sdis.File file = fileStorage.getBackedUpFilesById(message[3]);

                if (file != null)
                    return;

                file = fileStorage.getStoredFilesById(message[3]);


                //http://stackoverflow.com/questions/642897/removing-an-element-from-an-array-java
                int bytesRemoved = 0;
                for (int i = 0; i < messag.length; i++) {
                    if (messag[i] == (byte)'\r' &&
                            messag[i + 1] == (byte)'\n' &&
                            messag[i + 2] == (byte)'\r' &&
                            messag[i + 3] == (byte)'\n') {
                        for (int j = 0; j <= i + 3; j++, bytesRemoved++)
                            System.arraycopy(messag, 0 + 1, messag, 0, messag.length - 1 - 0);
                        break;
                    }
                }
                messag = Arrays.copyOf(messag, messag.length - bytesRemoved);


                //Se ainda nao tiver recebido nenhum chunk do ficheiro
                if (file == null) {
                    file = new sdis.File(message[3], Integer.parseInt(message[5]), 0);

                    file.getChunks().put(Integer.parseInt(message[4]), messag);
                    file.storeChunk(Integer.parseInt(message[4]));
                    fileStorage.addStoredFile(file);
                }

                //Se ja tiver recebido algum chunk do ficheiro apenas adiciona o chunk
                else {
                    file.getChunks().put(Integer.parseInt(message[4]), messag);
                    file.storeChunk(Integer.parseInt(message[4]));
                }

                //Update dos dados a guardar
                fileStorage.updateDataBase();

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