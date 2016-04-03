package sdis.Protocols;


import sdis.File;
import sdis.Peer;

import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Random;

public class ReclaimProtocol {
    private long size;
    private Peer peer;
    private String version;
    private PriorityQueue<Chunk> priorityQueue;

    public ReclaimProtocol(Peer p, long n){
        peer = p;
        size = n;
        version = "1.0";
        priorityQueue = new PriorityQueue<>();
    }

    /**
     * Deletes the first chunks in the priority queue
     * until the wanted size is reached
     */
    public void reclaimSpace(){
        long acc = 0L;
        fileTreeToPriorityQueue();
        while(!priorityQueue.isEmpty() && acc < size) {
            Chunk c = priorityQueue.poll();
            String filename = c.fileId + java.io.File.separator + c.chunkNumber;
            java.io.File file = new java.io.File(filename);
            long fileSize = file.length();
            if(file.exists()){
                System.out.println(filename + " Was deleted! SIZE: " + file.length());
                if(file.delete()){
                    try {
                        peer.getMDB().addIgnoreChunk(c.fileId + "/" + c.chunkNumber);
                        String message = "REMOVED " + version + " " + peer.getID() +  " " + c.fileId + " " + c.chunkNumber + " \r\n\r\n";
                        peer.getControlSocket().sendPacket(message.getBytes(),peer.getMC_IP(),peer.getMC_PORT());
                        acc += fileSize;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    System.out.println("Delete operation failed for " + filename);
                }
            }
        }
        try {
            Thread.sleep(2000);
            peer.getMDB().clearIgnoreChunks();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        priorityQueue.clear();
        peer.sendToClient("Space reclaiming successful");
    }

    private void fileTreeToPriorityQueue(){
        for (File f: peer.getFileStorage().getStoredFiles()) {
            for (Integer i : f.getPeersWithChunk().keySet()) {
                Chunk p = new Chunk(f.getFileID(),i,f.getChunkReplication(i));
                priorityQueue.add(p);
            }
        }
    }

    /**
     *
     */
    private void fillQueue(String fid){
        for (int i = 0; i < 10; i++)
            priorityQueue.add(new Chunk(fid,i,new Random().nextInt(5)));
    }

    /**
     * Helper class to store the file identification, chunk
     * number and replication degree associated
     */
    public class Chunk implements Comparable{
        public String fileId;
        public int chunkNumber;
        public int degree;

        public Chunk(String fid,int n, int d){
            fileId = fid;
            chunkNumber = n;
            degree = d;
        }

        @Override
        public int compareTo(Object other) {
            int d = ((Chunk)other).degree;
            if (degree > d)
                return -1;
            else if(degree < d)
                return 1;
            return 0;
        }
    }

}
