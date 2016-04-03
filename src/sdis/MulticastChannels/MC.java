package sdis.MulticastChannels;

import sdis.ChunkThread;
import sdis.FileStorage;
import sdis.Peer;
import sdis.ReclaimThread;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MC extends Thread {
    private Peer peer;
    public ConcurrentLinkedQueue<DatagramPacket> messageQueue = new ConcurrentLinkedQueue<>();
    private ArrayList<String> ignoreList;
    private FileStorage fileStorage;
    private volatile boolean reclaiming;

    public MC(Peer p, FileStorage fileSt) {
        peer = p;
        fileStorage = fileSt;
        reclaiming = false;
    }

    @Override
    public void run() {
         while (true) {
            try {
                DatagramPacket packet = peer.getControlSocket().receivePacket(64512);
                messageQueue.offer(packet);
                new ReceiveThread(packet.getAddress(), new String(packet.getData(), 0, packet.getLength())).start();
                messageQueue.poll();
            } catch (Exception e) {
                e.printStackTrace();
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
            if (Integer.parseInt(message[2]) != peer.getID()) {
                switch (message[0]) {
                    case "STORED": {
                        sdis.File file = fileStorage.getBackedUpFilesById(message[3]);
                        if (file != null) {
                            try {
                                file.addChunkReplication(Integer.parseInt(message[4]), InetAddress.getByName(message[2]));
                                System.out.println(InetAddress.getByName(message[2]) + " STORED chunk " + Integer.parseInt(message[4]) + " of file " + file.getFileID());
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                        }

                        file = fileStorage.getStoredFilesById(message[3]);
                        if (file != null) {
                            try {
                                file.addChunkReplication(Integer.parseInt(message[4]), InetAddress.getByName(message[2]));
                                System.out.println(InetAddress.getByName(message[2]) + " STORED chunk " + Integer.parseInt(message[4]) + " of file " + file.getFileID());
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }
                    case "DELETE": {
                            sdis.File file = fileStorage.getStoredFilesById(message[3].trim());
                            if (file != null) {
                                file.removeChunks();
                                fileStorage.getStoredFiles().remove(file);
                                System.out.println("DELETED file " + file.getFileID());
                            }
                            break;
                    }
                    case "GETCHUNK": {
                        new ChunkThread(peer, message[3], Integer.parseInt(message[4])).start();
                        break;
                    }
                    case "REMOVED": {
                            new ReclaimThread(peer, message[3], Integer.parseInt(message[4]), address).start();
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }
}
