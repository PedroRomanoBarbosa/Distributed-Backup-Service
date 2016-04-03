package sdis.Protocols;

import sdis.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Vector;

public class BackupProtocol {
    private int maxReplicationDegree = 9;
    private int maxTriesPerChunk = 5;

    public BackupProtocol(Peer peer, FileStorage fileStorage, String filename, int degree) {

        final String filePath = System.getProperty("user.dir") + java.io.File.separator + filename;

        try {
            if (Files.isDirectory(Paths.get(filePath)) || !Files.exists(Paths.get(filePath)))
                throw new IOException();

        } catch(IOException e){
            System.out.println("Error: Not a valid path for a file!");
        }

        //Grau de replicacao
        int replicationDegree = degree;

        File fileToBackup = null;
        try {
            fileToBackup = new File(filePath, replicationDegree);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if(!fileStorage.getBackedUpFiles().contains(fileToBackup)) {
            fileStorage.addBackedUpFile(fileToBackup);
            fileStorage.updateDataBase(peer.getID());
        }

        HashMap<Integer, byte[]> chunks = new HashMap<Integer, byte[]>();

        try {
            chunks = fileToBackup.buildChunks(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int lastSize = 0;

        for (int i = 0; i < chunks.size(); i++) {

            //TODO Alterar o campo 1.0 para Versao Real
            String messageHeader = "PUTCHUNK " + "1.0" + " " + peer.getID() + " " + fileToBackup.getFileID() + " " + i + " " +
                    fileToBackup.getReplicationDegree() + " " + "\r\n\r\n";

            byte[] fullMessage = new byte[messageHeader.getBytes().length + chunks.get(i).length];
            //System.out.println(messageHeader);
            //System.out.println(new String(fileToBackup.getChunks().get(i)));

            //FONTE: http://stackoverflow.com/questions/5368704/appending-a-byte-to-the-end-of-another-byte
            System.arraycopy(messageHeader.getBytes(), 0, fullMessage, 0, messageHeader.getBytes().length);
            System.arraycopy(chunks.get(i), 0, fullMessage, messageHeader.getBytes().length, chunks.get(i).length);
            // System.out.println(fullMessage.toString());
            lastSize = chunks.get(i).length;
            new SendThread(peer, fullMessage, replicationDegree, fileToBackup, i).start();
        }

        if (lastSize == 64000) {
            String messageHeader = "PUTCHUNK " + "1.0" + " " + peer.getID() + " " + fileToBackup.getFileID() + " " + chunks.size() + " " +
                    fileToBackup.getReplicationDegree() + " " + "\r\n\r\n";
            fileToBackup.getPeersWithChunk().put(chunks.size(), new Vector<>());
            new SendThread(peer, messageHeader.getBytes(), replicationDegree, fileToBackup, chunks.size()).start();
        }


        System.out.println("File " + filename + " sent to the network!\n");
    }


    /*
    SUBCLASSE para a thread de envio dos chunks
     */
    public class SendThread extends Thread {

        private Peer peer;
        private byte[] fullMessage;
        private int replicationDegree;
        private File file;
        private int chunkNo;

        public SendThread(Peer p, byte[] fullMessag, int replicationDg, File f, int chunkN){
            peer = p;
            fullMessage = fullMessag;
            replicationDegree = replicationDg;
            file = f;
            chunkNo = chunkN;
        }

        public void run() {
            int sleepTime = 1000;
            for (int tries = 0; file.getChunkReplication(chunkNo) < replicationDegree && tries < maxTriesPerChunk; tries++) {
                //Envio da mensagem
                try {
                    DatagramPacket packet = new DatagramPacket(fullMessage, fullMessage.length, peer.getMDB_IP(), peer.getMDB_PORT());

                    try {
                        peer.getBackupSocket().send(packet);
                        //System.out.println("Chunk number " + chunkNo + " of file " + file.getFileID() + " sent to the network");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep((long) (sleepTime));
                    sleepTime = 2*sleepTime;
                } catch (InterruptedException e) {
                }
            }

        }

    }
}