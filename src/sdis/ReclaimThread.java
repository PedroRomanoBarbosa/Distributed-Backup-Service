package sdis;

import sdis.Utils.Regex;
import java.util.Arrays;
import java.util.Random;

/**
 *
 */
public class ReclaimThread extends Thread {
    private final Peer peer;
    private final String fileId;
    private final int chunkNumber;
    private final int time;
    private final Check check;
    private volatile boolean send;

    public ReclaimThread(Peer p,String fid,int n){
        peer = p;
        fileId = fid;
        chunkNumber = n;
        time = new Random().nextInt(400);
        check = new Check();
        send = true;
    }

    @Override
    public void run() {
        try {
            File f = peer.getFileStorage().getBackedUpFilesById(fileId);
            if(f != null){
                f.setReplicationDegree(f.getChunkReplication(chunkNumber)-1); //TODO Set replication degree of the chunk not the file
                if(f.getReplicationDegree() < f.getChunkReplication(chunkNumber)){ //TODO Same
                    check.start();
                    Thread.sleep(time);
                    if(send){
                        //TODO build PUTCHUNK message to send to the network
                    }
                    check.end();
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
                byte[] packet = peer.getMC().messageQueue.peek();
                if(packet != null){
                    getHeader(packet);
                    String[] groups = regex.getGroups(new String(header));
                    if(groups.length != 0){
                        if(Integer.parseInt(groups[2]) != peer.getID() && groups[0].equals("PUTCHUNK") && groups[1].equals("1.0")){
                            if(groups[3].equals(fileId) && Integer.parseInt(groups[4]) == chunkNumber){
                                send = false;
                            }
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
