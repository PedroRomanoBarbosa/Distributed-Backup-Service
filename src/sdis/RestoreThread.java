package sdis;

import sdis.Utils.*;
import java.io.IOException;
import java.util.ArrayList;

public class RestoreThread extends MulticastThread{
    private String message;
    private Regex regex;

    public RestoreThread(Peer p){
        super(p);
        regex = new Regex("(\\w+)\\s+(.+)\\s+(.+)\\s+(.+)\\s+(.+)\\s+\\r\\n(.+)");
    }

    public void run() {
        while (active){
            try {
                message = "^GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> \r\nefqwqwfqwfqwfqwf$";
                ArrayList<String> args = regex.getGroups(message);
                if(args.size() != 0){
                    if(args.get(0).equals("GETCHUNK")){
                        process(args);
                    }
                }
                message = peer.getRestoreSocket().receive(64000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void process(ArrayList<String> args) {
        System.out.println(args);
    }

}
