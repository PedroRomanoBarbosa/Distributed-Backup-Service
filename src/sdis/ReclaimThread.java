package sdis;

import sdis.Utils.Regex;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;


/**
 *
 */
public class ReclaimThread extends Thread {
    private Peer peer;
    private String fileId;
    private int chunkNumber;
    private volatile boolean send;

    public ReclaimThread(Peer p,String fid,int n){
        peer = p;
        fileId = fid;
        chunkNumber = n;
        send = true;
    }

    @Override
    public void run() {
        try {
            File f = peer.getFileStorage().getBackedUpFilesById(fileId);
            if(f != null){
                f.setReplicationDegree(f.getChunkReplication(chunkNumber)-1); //TODO Set replication degree of the chunk not the file
                if(f.getReplicationDegree() < f.getChunkReplication(chunkNumber)){ //TODO Same
                    Check check = new Check();
                    check.start();
                    int time = new Random().nextInt(400);
                    Thread.sleep(time);
                    if(send){
                        //TODO Initiate backup subprotocol for this chunk
                    }
                    check.end();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     */
    public class Check extends Thread{
        private final String pattern = "^(PUTCHUNK)\\s+([0-9]\\.[0-9])\\s+([0-9]+)\\s+(.+?)\\s+([0-9]+)\\s+([0-9]+)\\s+\r\n\r\n$";
        private Regex regex;
        private byte[] header;
        private volatile boolean active;

        public Check(){
            regex = new Regex(pattern);
            active = true;
        }

        @Override
        public void run() {
            //TODO
            while (active){
                try {
                    //TODO PROBLEM BECAUSE OF THE WAY WE HANDLE SOCKETS. MULTICASTSOCKETS CAN'T BE PEEKED
                    //TODO WITHOUT CONSUMING THE PACKET, THEREFORE WE SHOULD IMPLEMENT A QUEUE
                    byte[] packet = peer.getBackupSocket().receiveData(70000);
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
                } catch (IOException e) {
                    e.printStackTrace();
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
