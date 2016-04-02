package sdis;

import sdis.Utils.Regex;

import java.io.*;
import java.io.File;
import java.util.Arrays;

/**
 * Restore thread handles the MDR channel for a peer and
 * restores a file requested by the client
 */
public class RestoreThread extends MulticastThread{
    private final String pattern = "^(CHUNK)\\s+([0-9]\\.[0-9])\\s+([0-9]+)\\s+(.+?)\\s+([0-9]+)\\s+\r\n\r\n$";

    private byte[] header;
    private byte[] body;
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
     * Receives messages from the MDR message queue and
     * processes valid messages. From the valid ones it chooses
     * the chunk content which is relative to the file's id and
     * it's still missing from an array of chunks( this is done
     * because chunks can come unordered ) It also ignores
     * duplicates
     */
    public void run(){
        int i = 0;
        byte[][] chunks = null;
        int numChunks = 0;
        while(active){
            byte[] packet = peer.getMDR().messageQueue.poll();
            if(packet != null){
                /**
                 * Get message packet and process the header to check if its
                 * valid and if it is retrieve the body an store into an array
                 */
                if(restore){
                    splitMessage(packet);
                    String message = new String(header);
                    //System.out.println("[MDR] " + message);
                    if(regex.check(message)){
                        if (file == null){
                            file = new File(peer.getFileStorage().getBackedUpFilesById(fileId).getPathFile());
                           // numChunks = peer.getFileStorage().getBackedUpFilesById(fileId).getChunks().size();
                            chunks = new byte[numChunks][];
                        }
                        String[] groups = regex.getGroups(message);
                        int initiatorId = Integer.parseInt(groups[2]);
                        String version = groups[1];
                        String fid = groups[3];
                        if(version.equals("1.0") && initiatorId != peer.getID() && fid.equals(fileId)){
                            fileId = groups[3];
                            int chunkNumber = Integer.parseInt(groups[4]);
                            if(chunks != null && chunks[chunkNumber] == null){
                                chunks[chunkNumber] = body;
                                i++;
                            }
                        }

                        /**
                         * Create file and reset variables after file restored. The String
                         * array 'chunks' must be different than null for obvious reasons.
                         * After this block runs, a message is sent to the client telling
                         * that the file is restored if an error did not occur. All settings
                         * and flags are reseted and a new RESTORE command can be made by
                         * the client.
                         */
                        if(i >= numChunks && chunks != null){
                            try {
                                FileOutputStream fos = new FileOutputStream(file);
                                for (byte[] chunk : chunks) {
                                    fos.write(chunk,0,chunk.length);
                                }
                                fos.close();
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

    /**
     * Splits the packet array into an header array and a
     * body array stored in this class
     * @param m packet array received
     */
    public void splitMessage(byte[] m){
        for (int i = 0; i < m.length; i++){
            if(m[i] == (byte)'\r' &&
                    m[i+1] == (byte)'\n' &&
                    m[i+2] == (byte)'\r' &&
                    m[i+3] == (byte)'\n'
                    ){
                header = Arrays.copyOf(m,i+3+1);
                body = Arrays.copyOfRange(m,i+4,m.length);
            }
        }
    }

}
