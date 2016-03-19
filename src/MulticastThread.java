import Utils.Regex;

import java.io.IOException;

public class MulticastThread extends Thread{
    private String message;
    protected Peer peer;
    protected boolean active;

    public MulticastThread(Peer p){
        peer = p;
        active = true;
    }

}
