package sdis.Protocols;

import sdis.*;

import java.io.*;

/**
 * A class to handle file restore commands and control restore threads
 */
public class RestoreProtocol {
    private Peer peer;
    private String filename;

    /**
     * Creates a restore thread.
     * @param p The peer associated with this thread
     */
    public RestoreProtocol(Peer p, String fn){
        peer = p;
        filename = fn;
    }

    /**
     * This method tries to send a message to the MC channel
     * restore a certain file and then fires the restore thread
     * mechanism to deal with the CHUNK messages other peers send
     */
    public void getChunks(){
        if(peer.getFileStorage().checkBackedUp(filename)){
            String message;
            String fileId = peer.getFileStorage().getBackedUpFilesByPath(filename).getFileID();
         //   int numChunks = peer.getFileStorage().getBackedUpFilesById(fileId).getChunks().size();
            String version = "1.0";
            peer.getRestoreThread().setFileId(fileId);
            peer.getRestoreThread().setRestore();
            /*try {
                for (int i = 0; i < numChunks; i++){
                    message = "GETCHUNK" + " " + version + " " + peer.getID() + " " + fileId + " " + i + " " + "\r\n\r\n";
                    peer.getControlSocket().send(message,peer.getMC_IP(),peer.getMC_PORT());
                }
            } catch (IOException e) {
                e.printStackTrace();
                peer.getRestoreThread().unsetRestore();
                peer.sendToClient("An error has occurred and the file could not be restored");
            }*/
        }else {
            peer.sendToClient("File doesn't exist in the system");
        }
    }
}
