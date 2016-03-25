package sdis;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public class FileStorage implements Serializable {

    private List<File> storedFiles = null;
    private List<File> backedUpFiles = null;
    private String path = null;

    public FileStorage(String p) {
        path = p;
        storedFiles = new Vector<File>();
        backedUpFiles = new Vector<File>();
    }

    public void addBackedUpFile(File file) {
        if (!backedUpFiles.contains(file))
            backedUpFiles.add(file);
    }

    public void addStoredFile(File file) {
        if (!storedFiles.contains(file))
            storedFiles.add(file);
    }

    public boolean checkBackedUp(String filePath){
        for (File file : backedUpFiles){
            if(file.getPathFile().equals(filePath)){
                return true;
            }
        }
        return false;
    }

    public List<File> getStoredFiles() {
        return storedFiles;
    }

    public List<File> getBackedUpFiles() {
        return backedUpFiles;
    }

    public File getBackedUpFilesById(String Id) {
        for (File f : backedUpFiles)
            if (f.getFileID().equals(Id))
                return f;
        return null;
    }

    public File getStoredFilesById(String Id) {
        for (File f : storedFiles)
            if (f.getFileID().equals(Id))
                return f;
        return null;
    }

    public File getBackedUpFilesByFilePath(String filePath) {
        for (File f : backedUpFiles)
            if (f.getPathFile().equals(filePath))
                return f;
        return null;
    }

}
