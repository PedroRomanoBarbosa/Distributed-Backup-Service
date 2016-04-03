package sdis.MulticastChannels;

import sdis.DataSocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This thread reads the MDR socket and puts the message
 * in a concurrent queue to be processed by other threads
 */
public class MDR extends Thread{
    DataSocket restoreSocket;
    private boolean active;
    public ConcurrentLinkedQueue<byte[]> messageQueue = new ConcurrentLinkedQueue<>();

    public MDR(DataSocket rs) {
        restoreSocket = rs;
        active = true;
    }

    public void run(){
        while (active){
            try {
                byte[] packet = restoreSocket.receiveData(64512);
                messageQueue.offer(packet);
            } catch ( IOException e) {
                e.printStackTrace();
            }
        }
    }
}
