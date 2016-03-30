package sdis.MulticastChannels;

import sdis.DataSocket;
import sdis.MulticastThread;
import sdis.Peer;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This thread reads the MDR socket and puts the message
 * in a concurrent queue to be processed by other threads
 */
public class MDR {
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
                new QueueThread(packet).start();
            } catch ( IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class QueueThread extends Thread{
        private byte[] packet;

        public QueueThread(byte[] p){
            packet = p;
        }

        @Override
        public void run() {
            messageQueue.offer(packet);
            System.out.println("THREAD!");
        }
    }
}
