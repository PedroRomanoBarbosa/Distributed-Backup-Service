package sdis.Protocols;
import sdis.*;
import sdis.Utils.Regex;

public class RestoreProtocol {
    private String message;
    private Regex regex;
    private Peer peer;

    public RestoreProtocol(Peer p){
        peer = p;
        regex = new Regex("^(CHUNK)\\s+([0-9]\\.[0-9])\\s+([0-9]+)\\s+(.+)\\s+([0-9]+)\\s+\r\n\r\n$");
    }

    public void getChunk(){

    }

}
