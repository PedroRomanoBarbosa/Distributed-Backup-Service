package sdis.MulticastChannels;

import sdis.MulticastThread;
import sdis.Peer;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MDR extends MulticastThread{
    public final ConcurrentLinkedQueue<byte[]> messageQueue = new ConcurrentLinkedQueue<>();

    public MDR(Peer p, String name) {
        super(p, name);
    }

    public void run(){
        while (active){
            try {
                byte[] packet = peer.getRestoreSocket().receiveData(100000);
                messageQueue.offer(packet);
            } catch ( IOException e) {
                e.printStackTrace();
            }
        }
    }
}
