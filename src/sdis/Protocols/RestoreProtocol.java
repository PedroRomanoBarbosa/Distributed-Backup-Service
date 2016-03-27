package sdis.Protocols;

import sdis.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.io.File;

/**
 * A class to handle file restore commands and control restore threads
 */
public class RestoreProtocol {
    private Peer peer;

    /**
     * Creates a restore thread.
     * @param p The peer associated with this thread
     */
    public RestoreProtocol(Peer p){
        peer = p;
    }

    /**
     * This method tries to send a message to the MC channel
     * restore a certain file and then fires the restore thread
     * mechanism to deal with the CHUNK messages other peers send
     * @param filePath path of the file to be restored
     */
    public void getChunks(String filePath){
        if(!peer.getFileStorage().checkBackedUp(filePath)){
            String message;
            int numChunks = 3; //TODO peer.getFileStorage().getBackedUpFilesById(fileId).getChunks().size();
            String fileId = "123abc"; //TODO String fileId = peer.getFileStorage().getBackedUpFilesByFilePath(filePath).getFileID();
            String version = "1.0";
            peer.getRestoreThread().setFileId(fileId);
            peer.getRestoreThread().setRestore();
            try {
                for (int i = 0; i < numChunks; i++){
                    message = "GETCHUNK" + " " + version + " " + peer.getID() + " " + fileId + " " + i + " " + "\r\n\r\n";
                    peer.getControlSocket().send(message,peer.getMC_IP(),peer.getMC_PORT());
                }
            } catch (IOException e) {
                e.printStackTrace();
                peer.getRestoreThread().unsetRestore();
                peer.sendToClient("An error has occurred and the file could not be restored");
            }
        }else {
            peer.sendToClient("File doesn't exist in the system");
        }
    }

    /**
     * Sends all the chunks from the file with the file
     * identification fileId that the peer associated with
     * this protocol has
     * @param fileId File identification
     */
    public void sendChunks(String fileId){
        File[] files = new File(fileId).listFiles();
        if(files != null){
            for (File f :files){
                new ChunkThread(peer,fileId,Integer.parseInt(f.getName()));
            }
        }
    }

    public class ChunkThread extends Thread{
        private Peer peer;
        private String fileId;
        private String chunk;
        private String message;
        public volatile int chunkNumber;
        public volatile boolean active;
        public volatile boolean send;

        public ChunkThread(Peer p,String fid,int n){
            peer = p;
            fileId = fid;
            chunkNumber = n;
            send = true;
            File f = new File(fileId + File.separator + chunkNumber);
            if(f.exists()){
                active = true;
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                byte[] content = new byte[(int)f.length()];
                try {
                    fis.read(content,0,content.length);
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                chunk = new String(content);
            }else{
                active = false;
            }
        }

        @Override
        public void run() {
            while (active) {
                try {
                    int time = new Random().nextInt(400);
                    sleep(time);
                    if (send) {
                        String version = "1.0";
                        message = "CHUNK " + version + " " + peer.getID() + " " + fileId + " " + chunkNumber + " \r\n\r\n" + chunk;
                        peer.getRestoreSocket().send(message, peer.getMDB_IP(), peer.getMDR_PORT());
                    }
                    System.out.println("Message sent!");
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
