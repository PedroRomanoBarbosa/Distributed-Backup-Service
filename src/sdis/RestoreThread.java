package sdis;

import sdis.Utils.Regex;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;

/**
 * Restore thread handles the MDR channel for a peer
 */
public class RestoreThread extends MulticastThread{
    private Regex regex;
    private String fileId;
    private volatile boolean restore;

    /**
     * Creates a new thread to collect chunks from the network to
     * restore the intended file
     * @param p Peer object
     * @param name Thread name
     */
    public RestoreThread(Peer p, String name){
        super(p,name);
        regex = new Regex("^(CHUNK)\\s+([0-9]\\.[0-9])\\s+([0-9]+)\\s+(.+?)\\s+([0-9]+)\\s+\r\n\r\n(.+)$");
        restore = false;
    }

    /**
     * Sets the file Id
     * @param fid file Id
     */
    public void setFileId(String fid){
        fileId = fid;
    }

    /**
     * Receives messages from the MDR multicast channel and
     * processes valid messages. From the valid ones it chooses
     * the chunk content which is relative to the file's id and
     * it's still missing from an array of chunks( this is done
     * because chunks can come unordered ) It also ignores
     * duplicates
     */
    public void run() {
        int i = 0;
        while (active){
            String example = "CHUNK 1.0 1 123abc 1 \r\n\r\nOlá,eu sou o mestre do kong foo!"; //message = peer.getRestoreSocket().receive(65536).trim();
            if(restore){
                int numChunks = 3; //peer.getFileStorage().getBackedUpFilesById(fileId).getChunks().size();
                String[] chunks = new String[numChunks];
                try {
                    ArrayList<String> groups = regex.getGroups(example);
                    //Check if its a valid message and its from version 1.0
                    int initiatorId = Integer.parseInt(groups.get(2));
                    String version = groups.get(1);
                    String fid = groups.get(3);
                    if(groups.get(0).equals("CHUNK") && version.equals("1.0") && initiatorId != peer.getID() && fid.equals(fileId)){
                        fileId = groups.get(3);
                        int chunkNumber = i;
                        if(chunks[chunkNumber] == null){
                            chunks[chunkNumber] = groups.get(5);
                            i++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    peer.sendToClient("File couldn't be restored");
                }
                //Reset restore flag after file restored
                if(i >= numChunks){
                    restore = false;
                    i = 0;
                    peer.sendToClient("File is fully restored");
                }
            }
        }
    }

    /**
     * Sets restore flag to true
     */
    public void setRestore(){
        restore = true;
    }

    @Deprecated
    private void process(ArrayList<String> args) {
        // Get the arguments from string representation
        String message;
        float version = Float.parseFloat(args.get(1));
        int senderId = Integer.parseInt(args.get(2));
        String fileId = args.get(3);
        int chunkNumber = Integer.parseInt(args.get(4));

        //Construct message header
        message = "CHUNK " + version + " " + senderId + " " + fileId + " " + chunkNumber + "\r\n\r\n";

        //Get file (incomplete)
        File f = new File("/Users/PedroBarbosa/Desktop/chunk1");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Create byte array to store the content of the chunk file.
        // Max size: 2147483647 bytes ~ 2 gigabytes(this can be changed later)
        byte[] content = new byte[(int)f.length()];
        try {
            fis.read(content,0,content.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Attach chunk to the message
        message += new String(content);

        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Create a random delay between
        // TODO: Check in a thread if this peer receives a CHUNK message.
        Random rand = new Random();
        int timeToWait = rand.nextInt(400);
        try {
            Thread.sleep(timeToWait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Send message containing the header and chunk content
        try {
            peer.getRestoreSocket().send(message,peer.getMDR_IP(),peer.getMDR_PORT());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Message sent!");
    }

}
