package sdis;

import java.io.*;
import java.util.Random;

public class ChunkThread extends Thread{
    private Peer peer;
    private String fileId;
    public int chunkNumber;
    private String message;
    public volatile boolean active;
    public volatile boolean send;

    public ChunkThread(Peer p,String fid,int n){
        peer = p;
        fileId = fid;
        chunkNumber = n;
        String chunk;
        send = true;
        java.io.File f = new java.io.File(fileId + java.io.File.separator + chunkNumber);
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
            message = "CHUNK " + "1.0" + " " + peer.getID() + " " + fileId + " " + chunkNumber + " \r\n\r\n" + chunk;
        }else{
            active = false;
        }
    }

    @Override
    public void run() {
        try {
            if(active){
                int time = new Random().nextInt(400);
                sleep(time);
                if (send) {
                    peer.getRestoreSocket().send(message, peer.getMDR_IP(), peer.getMDR_PORT());
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

}
