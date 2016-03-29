package sdis;

import sdis.Utils.Regex;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

/**
 * Thread responsible for sending a CHUNK message
 * to the network
 */
public class ChunkThread extends Thread{
    private Peer peer;
    private String fileId;
    public int chunkNumber;
    private String message;
    private byte[] packet;
    public volatile boolean active;
    public volatile boolean send;

    /**
     * Creates a new chunk thread that applies the RESTORE
     * protocol to send a CHUNK message with a chunks's content
     * @param p the peer associated with this thread
     * @param fid the file's identification
     * @param n the chunk number of the chunk
     */
    public ChunkThread(Peer p,String fid,int n){
        peer = p;
        fileId = fid;
        chunkNumber = n;
        byte[] chunk;
        send = true;
        java.io.File f = new java.io.File(fileId + java.io.File.separator + chunkNumber);
        if(f.exists()){
            active = true;
            FileInputStream fis;
            try {
                byte[] content = new byte[(int)f.length()];
                fis = new FileInputStream(f);
                if(fis.read(content,0,content.length) != -1){
                    chunk = content;
                    message = "CHUNK " + "1.0" + " " + peer.getID() + " " + fileId + " " + chunkNumber + " \r\n\r\n";
                    packet = new byte[message.getBytes().length + chunk.length];
                    System.arraycopy(message.getBytes(),0,packet,0,message.getBytes().length);
                    System.arraycopy(chunk,0,packet,message.getBytes().length,chunk.length);

                }else {
                    active = false;
                }
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            active = false;
        }
    }

    /**
     * Sends a CHUNK messsage with this thread chunk content
     * after a random sleep time between 0 and 400 milliseconds.
     * Only sends the message if the MDR didn't received the same
     * CHUNK message
     */
    @Override
    public void run() {
        try {
            if(active){
                int time = new Random().nextInt(400);
                new CheckChunk().start();
                sleep(time);
                if (send) {
                    peer.getRestoreSocket().sendPacket(packet, peer.getMDR_IP(), peer.getMDR_PORT());
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inner class to check for the same CHUNK messages
     * that the main thread will send
     */
    public class CheckChunk extends Thread{
        private boolean active;
        private final String pattern = "^(CHUNK)\\s+([0-9]\\.[0-9])\\s+([0-9]+)\\s+(.+?)\\s+([0-9]+)\\s+\r\n\r\n$";
        private Regex regex;
        private byte[] header;

        public CheckChunk(){
            active = true;
            regex = new Regex(pattern);
        }

        /**
         * Watches the head of the queue and checks if the same
         * CHUNK message received is the same as the one that the
         * main thread sends
         */
        @Override
        public void run() {
            while (active){
                byte[] packet = peer.getMDR().messageQueue.peek();
                if(packet != null){
                    getHeader(packet);
                    String[] groups = regex.getGroups(new String(header));
                    if(groups.length != 0){
                        if(Integer.parseInt(groups[2]) != peer.getID() && groups[0].equals("CHUNK") && groups[1].equals("1.0")){
                            if(groups[3].equals(fileId) && Integer.parseInt(groups[4]) == chunkNumber){
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
