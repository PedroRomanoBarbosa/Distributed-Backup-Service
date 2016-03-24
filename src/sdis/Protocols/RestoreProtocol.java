package sdis.Protocols;

import sdis.*;
import sdis.Utils.Regex;
import java.io.File;

public class RestoreProtocol {
    private final String pattern = "^(CHUNK)\\s+([0-9]\\.[0-9])\\s+([0-9]+)\\s+(.+)\\s+([0-9]+)\\s+\r\n\r\n$";
    private Regex regex;
    private Peer peer;

    public RestoreProtocol(Peer p){
        peer = p;
        regex = new Regex(pattern);
    }

    public void getChunk(String filePath){
        //Check if the file exists in the database
        if(!peer.getFileStorage().checkBackedUp(filePath)){
            peer.getRestoreThread().start();
            try {
                peer.getRestoreThread().join();
                System.out.println("Thread com o nome " + peer.getRestoreThread().getName() + " terminou");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {
            peer.sendToClient("File doesn't exist in the system");
        }
    }

}
