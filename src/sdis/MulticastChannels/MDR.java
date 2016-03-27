package sdis.MulticastChannels;

import sdis.MulticastThread;
import sdis.Peer;
import sdis.Utils.Regex;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MDR extends MulticastThread{
    private final String pattern = "^(CHUNK)\\s+([0-9]\\.[0-9])\\s+([0-9]+)\\s+(.+?)\\s+([0-9]+)\\s+\r\n\r\n(.+)$";
    public final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private final Regex regex;

    public MDR(Peer p, String name) {
        super(p, name);
        regex = new Regex(pattern);
    }

    public void run(){
        while (active){
            try {
                message = peer.getRestoreSocket().receive(100000);
                if(regex.check(message)) {
                    messageQueue.add(message);
                }
            } catch ( IOException e) {
                e.printStackTrace();
            }
        }
    }
}
