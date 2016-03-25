package sdis.Protocols;

import sdis.*;

public class RestoreProtocol {
    private Peer peer;

    public RestoreProtocol(Peer p){
        peer = p;
    }

    public void getChunks(String filePath){
        //Check if the file exists in the database
        if(!peer.getFileStorage().checkBackedUp(filePath)){
            //String fileId = peer.getFileStorage().getBackedUpFilesByFilePath(filePath).getFileID();
            peer.getRestoreThread().setFileId("123abc"); //peer.getRestoreThread().setFileId(fileId);
            peer.getRestoreThread().setRestore();
        }else {
            peer.sendToClient("File doesn't exist in the system");
        }
    }

}
