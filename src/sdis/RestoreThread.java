package sdis;

import sdis.Utils.Regex;

import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Restore thread handles the MDR channel for a peer and
 * restores a file requested by the client
 */
public class RestoreThread extends MulticastThread{
    private final String pattern = "^(CHUNK)\\s+([0-9]\\.[0-9])\\s+([0-9]+)\\s+(.+?)\\s+([0-9]+)\\s+\r\n\r\n(.+)$";

    private Regex regex;
    private String fileId;
    private File file;
    private volatile boolean restore;

    /**
     * Creates a new thread to collect chunks from the network to
     * restore the intended file
     * @param p Peer object
     * @param name Thread name
     */
    public RestoreThread(Peer p, String name){
        super(p,name);
        regex = new Regex(pattern);
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
    public void run2() {
        /*
        int i = 0;
        String[] chunks = null;
        int numChunks = 0;
        /**
         * This code block is active whenever the client sends a RESTORE
         * command. It then tries to create a file and write to it with
         * the messages it receives from the network that reference the
         * file identification number
         *
        while (active){
            String example = "CHUNK 1.0 1 123abc 1 \r\n\r\nOlá,eu sou o mestre do kong foo!"; //TODO message = peer.getRestoreSocket().receive(65536).trim();

            if(restore){
                //Create file
                if (file == null){
                    //TODO file = new File(peer.getFileStorage().getBackedUpFilesById(fileId).getPathFile());
                    file = new File("test");
                    numChunks = 3; //TODO peer.getFileStorage().getBackedUpFilesById(fileId).getChunks().size();
                    chunks = new String[numChunks];
                }
                try {
                    ArrayList<String> groups = regex.getGroups(example);
                    int initiatorId = Integer.parseInt(groups.get(2));
                    String version = groups.get(1);
                    String fid = groups.get(3);
                    if(groups.get(0).equals("CHUNK") && version.equals("1.0") && initiatorId != peer.getID() && fid.equals(fileId)){
                        fileId = groups.get(3);
                        int chunkNumber = i;
                        if(chunks != null && chunks[chunkNumber] == null){
                            chunks[chunkNumber] = groups.get(5);
                            i++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    peer.sendToClient("File couldn't be restored");
                }

                /**
                 * Create file and reset variables after file restored. The String
                 * array 'chunks' must be different than null for obvious reasons.
                 * After this block runs a message is sent to the client telling
                 * that the file is restored if an error did not occur. All settings
                 * are restored and a new RESTORE command can be made by the client.
                 *
                if(i >= numChunks && chunks != null){
                    try {
                        FileWriter fw;
                        fw = new FileWriter(file,true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        for (String chunk : chunks) {
                                bw.write(chunk);
                        }
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        peer.sendToClient("An error has occurred and file couldn't be restored");
                    }
                    try {
                        //If file doesn't exist
                        if(file.createNewFile()){
                            peer.sendToClient("File is fully restored");
                        //If file already exists
                        }else{
                            peer.sendToClient("File is fully restored");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    restore = false;
                    i = 0;
                    file = null;
                    numChunks = 0;
                    chunks = null;
                }
            }
        }*/
    }

    /**
     * Gets the messages from the concurrent queue
     */
    public void run(){
        int i = 0;
        String[] chunks = null;
        int numChunks = 0;
        while(active){
            message = peer.getMDR().messageQueue.poll();
            if(message != null){
                if(restore){
                    if (file == null){
                        file = new File("test");    //TODO file = new File(peer.getFileStorage().getBackedUpFilesById(fileId).getPathFile());
                        numChunks = 3;              //TODO peer.getFileStorage().getBackedUpFilesById(fileId).getChunks().size();
                        chunks = new String[numChunks];
                    }
                    ArrayList<String> groups = regex.getGroups(message);
                    int initiatorId = Integer.parseInt(groups.get(2));
                    String version = groups.get(1);
                    String fid = groups.get(3);
                    if(version.equals("1.0") && initiatorId != peer.getID() && fid.equals(fileId)){
                        fileId = groups.get(3);
                        int chunkNumber = i;
                        if(chunks != null && chunks[chunkNumber] == null){
                            chunks[chunkNumber] = groups.get(5);
                            i++;
                        }
                    }
                    /**
                     * Create file and reset variables after file restored. The String
                     * array 'chunks' must be different than null for obvious reasons.
                     * After this block runs, a message is sent to the client telling
                     * that the file is restored if an error did not occur. All settings
                     * are restored and a new RESTORE command can be made by the client.
                     */
                    if(i >= numChunks && chunks != null){
                        try {
                            FileWriter fw;
                            fw = new FileWriter(file,true);
                            BufferedWriter bw = new BufferedWriter(fw);
                            for (String chunk : chunks) {
                                bw.write(chunk);
                            }
                            bw.close();
                            if(file.createNewFile()){
                                peer.sendToClient("File is fully restored");
                            }else {
                                peer.sendToClient("File is fully restored");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            peer.sendToClient("An error has occurred and file couldn't be restored");
                        }

                        restore = false;
                        i = 0;
                        file = null;
                        numChunks = 0;
                        chunks = null;
                    }
                }
            }
        }
    }

    /**
     * Sets restore flag to true so that the run method can
     * evaluate CHUNK messages and restore the file intended
     * by the client
     */
    public void setRestore(){
        restore = true;
    }

    /**
     * Unsets the restore flag so that anyone can interrupt
     * the reading of CHUNK messages of a certain file requested
     * by the user
     */
    public void unsetRestore(){
        restore = false;
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
