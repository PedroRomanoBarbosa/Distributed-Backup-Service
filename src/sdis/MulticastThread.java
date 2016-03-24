package sdis;

public class MulticastThread extends Thread{
    private String message;
    protected Peer peer;
    protected boolean active;

    public MulticastThread(Peer p,String name){
        super(name);
        peer = p;
        active = true;
    }

}
