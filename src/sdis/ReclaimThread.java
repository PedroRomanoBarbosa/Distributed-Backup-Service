package sdis;

import sdis.Utils.Regex;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;

/**
 * Reclaim thread that updates replication degrees
 * and sends PUTCHUNK messages id the replication degree of chunk
 * is smaller than the minimal
 */
public class ReclaimThread extends Thread {
    private final Peer peer;
    private final String fileId;
    private final int chunkNumber;
    private final InetAddress senderAddress;
    private final int time;
    private final Check check;
    private volatile boolean send;

    /**
     * Constructs a reclaim thread
     * @param p The associated peer
     * @param fid The file identification
     * @param n The chunk number
     * @param addr The sender's address
     */
    public ReclaimThread(Peer p,String fid,int n,InetAddress addr){
        peer = p;
        fileId = fid;
        chunkNumber = n;
        senderAddress = addr;
        time = new Random().nextInt(400);
        check = new Check();
        send = true;
    }

    /**
     * Runs the thread algorithm
     */
    @Override
    public void run() {
        try {
            File f = peer.getFileStorage().getStoredFilesById(fileId);
            if(f != null){
                FileInputStream fis;
                try {
                    java.io.File file = new java.io.File(fileId + java.io.File.separator + Integer.toString(chunkNumber));
                    if(file.exists()){
                        fis = new FileInputStream(file);
                        if(f.decreaseReplicationDegree(chunkNumber,senderAddress)){
                            System.out.println("CHUNK degree: " + f.getChunkReplication(chunkNumber) + "FILE: " + f.getReplicationDegree());
                            /**
                             * If the current chunk replication degree is smaller than the minimal
                             * replication degree it should send a PUTCHUNK message with the same chunk
                             */
                            if(f.getChunkReplication(chunkNumber) < f.getReplicationDegree()){
                                check.start();
                                Thread.sleep(time);
                                /**
                                 * If after the random delay of (0-400)ms the peer doesn't receive
                                 * a PUTCHUNK message with the same fileId and chunkNumber then it will
                                 * send such a message to the network
                                 */
                                if(send){
                                    try {
                                        String version = "1.0";
                                        String message = "PUTCHUNK "+version+" "+peer.getID()+" "+fileId+" "+chunkNumber+" "+f.getReplicationDegree()+" \r\n\r\n";
                                        byte[] header = message.getBytes();
                                        byte[] body = new byte[64000];
                                        int bytesRead = fis.read(body);
                                        if(bytesRead < 64000)
                                            body = new byte[bytesRead];
                                        byte[] data = new byte[header.length + bytesRead];
                                        System.arraycopy(header,0,data,0,header.length);
                                        System.arraycopy(body,0,data,header.length,body.length);
                                        peer.getBackupSocket().sendPacket(data,peer.getMDB_IP(),peer.getMDB_PORT());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                /**
                                 * IMPORTANT! End the thread so that there are no memory leaks
                                 */
                                check.end();
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    /**
     * Inner class to check for the same PUTCHUNK messages
     * that the main thread will send
     */
    public class Check extends Thread{
        private volatile boolean active;
        private final String pattern = "^(PUTCHUNK)\\s+([0-9]\\.[0-9])\\s+([0-9]+)\\s+(.+?)\\s+([0-9]+)\\s+\r\n\r\n$";
        private Regex regex;
        private byte[] header;

        public Check(){
            active = true;
            regex = new Regex(pattern);
        }

        /**
         * Watches the head of the queue and checks if the a
         * PUTCHUNK message received is the same for the file
         * chunk and if that's the case puts the send flag to false
         */
        @Override
        public void run() {
            while (active){
                DatagramPacket packet = peer.getMC().messageQueue.peek();
                if(packet != null){
                    byte[] data = Arrays.copyOfRange(packet.getData(),packet.getOffset(),packet.getLength());
                    getHeader(data);
                    String[] g = regex.getGroups(new String(header));
                    if(g.length != 0){
                        if(g[0].equals("PUTCHUNK") && g[3].equals(fileId) && Integer.parseInt(g[4]) == chunkNumber){
                            send = false;
                        }
                    }
                }
            }
        }

        /**
         * End this thread
         */
        public void end(){
            active = false;
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
