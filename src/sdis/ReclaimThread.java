package sdis;

import sdis.Utils.Regex;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 */
public class ReclaimThread extends Thread {
    private final Peer peer;
    private final String fileId;
    private final int chunkNumber;
    private final InetAddress senderAddress;
    private final int time;
    private final Check check;
    private volatile boolean send;

    public ReclaimThread(Peer p,String fid,int n,InetAddress addr){
        peer = p;
        fileId = fid;
        chunkNumber = n;
        senderAddress = addr;
        time = new Random().nextInt(400);
        check = new Check();
        send = true;
    }

    @Override
    public void run() {
        try {
            File f = peer.getFileStorage().getBackedUpFilesById(fileId);
            if(f != null){
                if(f.decreaseReplicationDegree(chunkNumber,senderAddress)){
                    if(f.getReplicationDegree() < f.getChunkReplication(chunkNumber)){
                        check.start();
                        Thread.sleep(time);
                        if(send){
                            //TODO build PUTCHUNK message to send to the network
                        }
                        check.end();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inner class to check for the same CHUNK messages
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
