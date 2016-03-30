package sdis.MulticastChannels;

import sdis.ChunkThread;
import sdis.FileStorage;
import sdis.Peer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MC extends Thread {

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
                DatagramPacket packet = peer.getControlSocket().receivePacket(64000);
                String message = new String(packet.getData(), 0, packet.getLength());
                new ReceiveThread(packet.getAddress(), message).start();

            } catch (Exception e) {

            }
        }
    }

    public class ReceiveThread extends Thread {

        InetAddress address;
        String messag;

        public ReceiveThread(InetAddress addr, String mess) {
            address = addr;
            messag = mess;
        }

        public void run() {
            messageDealer(address, messag);
        }

        private void messageDealer(InetAddress address, String messag) {
            String message[] = messag.split(" ");

            switch (message[0]) {
                case "STORED": {
                    sdis.File file = fileStorage.getBackedUpFilesById(message[3]);

                    if (file != null) {
                        try {
                            file.addChunkReplication(Integer.parseInt(message[4]), InetAddress.getByName(message[2]));
                            System.out.println(InetAddress.getByName(message[2]) + " STORED chunk " + Integer.parseInt(message[4]) + " of file "+ file.getFileID());
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }

                case "DELETE": {
                    sdis.File file = fileStorage.getStoredFilesById(message[3]);

                    if (file != null) {
                        file.removeChunks();
                        fileStorage.getStoredFiles().remove(file);
                        System.out.println("DELETED file " + file.getFileID());
                    }

                    break;
                }

                case "GETCHUNK": {
                    if(Integer.parseInt(message[2]) != peer.getID()){
                        System.out.println(messag);
                        new ChunkThread(peer,message[3],Integer.parseInt(message[4])).start();
                    }
                    break;
                }

                case "REMOVED": {
                    if(Integer.parseInt(message[2]) != peer.getID()){
                        new ChunkThread(peer,message[3],Integer.parseInt(message[4])).start();
                    }
                    break;
                }

                default:
                    break;
            }
        }
    }
}
