import java.io.IOException;

public class MulticastThread extends Thread{
    private String message;
    private DataSocket socket;
    private boolean active;

    public MulticastThread(DataSocket s){
        active = true;
        socket = s;
    }

    public void run() {
        while (active){
            try {
                message = socket.receive(64000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
