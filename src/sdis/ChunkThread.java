package sdis;

import sdis.Utils.Regex;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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
            FileInputStream fis;
            try {
                byte[] content = new byte[(int)f.length()];
                fis = new FileInputStream(f);
                fis.read(content,0,content.length);
                fis.close();
                chunk = new String(content);
                message = "CHUNK " + "1.0" + " " + peer.getID() + " " + fileId + " " + chunkNumber + " \r\n\r\n" + chunk;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            active = false;
        }
    }

    @Override
    public void run() {
        try {
            if(active){
                int time = new Random().nextInt(400);
                new CheckChunk().start();
                sleep(time);
                if (send) {
                    peer.getRestoreSocket().send(message, peer.getMDR_IP(), peer.getMDR_PORT());
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public class CheckChunk extends Thread{
        private boolean active;
        private final String pattern = "^(CHUNK)\\s+([0-9]\\.[0-9])\\s+([0-9]+)\\s+(.+?)\\s+([0-9]+)\\s+\r\n\r\n$";
        private Regex regex;
        private byte[] header;

        public CheckChunk(){
            active = true;
            regex = new Regex(pattern);
        }

        @Override
        public void run() {
            while (active){
                byte[] packet = peer.getMDR().messageQueue.peek();
                if(packet != null){
                    getHeader(packet);
                    ArrayList<String> groups = regex.getGroups(new String(header));
                    if(!groups.isEmpty()){
                        if(Integer.parseInt(groups.get(2)) == peer.getID() && groups.get(0).equals("CHUNK") && groups.get(1).equals("1.0")){ //TODO ID !=
                            if(groups.get(3).equals(fileId) && Integer.parseInt(groups.get(4)) == chunkNumber){
                                send = false;
                            }
                        }
                    }
                }
            }
        }

        /**
         * Get the header of a datagram packet
         * @param m packet array received
         */
        public void getHeader(byte[] m){
            for (int i = 0; i < m.length; i++){
                if(m[i] == (byte)'\r' &&
                        m[i+1] == (byte)'\n' &&
                        m[i+2] == (byte)'\r' &&
                        m[i+3] == (byte)'\n'
                        ){
                    header = Arrays.copyOf(m,i+3+1);
                }
            }
        }
    }

}
