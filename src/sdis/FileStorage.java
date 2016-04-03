package sdis;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
public class FileStorage implements Serializable {

    private List<File> storedFiles = null;
    private List<File> backedUpFiles = null;
    private String path = null;
    private final int maxSize = 2000000000; //2GB
    private int actualSize;

    public FileStorage(String p) {
        path = p;
        storedFiles = new Vector<File>();
        backedUpFiles = new Vector<File>();
        actualSize = 0;
    }

    public boolean verifyCapacity(int newChunkSize) {
        int temp = actualSize + newChunkSize;

        if (temp <= maxSize) {
            actualSize = temp;
            return true;
        }

        return false;
    }

    public void reloadSize() {
        actualSize = 0;
        for (File file : storedFiles){
            for (Map.Entry<Integer, Integer> entry : file.getChunksSize().entrySet()) {
                actualSize =+ entry.getValue();
            }
        }
    }

    public synchronized void addBackedUpFile(File file) {
        if (!backedUpFiles.contains(file))
            backedUpFiles.add(file);
    }

    public synchronized void addStoredFile(File file) {
        if (!storedFiles.contains(file))
            storedFiles.add(file);
    }

    public synchronized boolean checkBackedUp(String filename){
        String filePath = System.getProperty("user.dir") + java.io.File.separator + filename;
        for (File file : backedUpFiles){
            if(file.getPathFile().equals(filePath)){
                return true;
            }
        }
        return false;
    }

    public synchronized List<File> getStoredFiles() {
        return storedFiles;
    }

    public synchronized List<File> getBackedUpFiles() {
        return backedUpFiles;
    }

    public synchronized File getBackedUpFilesById(String Id) {
        for (File f : backedUpFiles)
            if (f.getFileID().equals(Id))
                return f;
        return null;
    }

    public synchronized File getBackedUpFilesByPath(String filename) {
        String filePath = System.getProperty("user.dir") + java.io.File.separator + filename;
        for (File f : backedUpFiles)
            if (f.getPathFile().equals(filePath))
                return f;
        return null;
    }

    public synchronized File getStoredFilesById(String Id) {
        for (File f : storedFiles)
            if (f.getFileID().equals(Id))
                return f;
        return null;
    }

    public synchronized void updateDataBase(int ID) {
        //Save
        try {
            FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + java.io.File.separator + ID + ".info");
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(this);

            oos.close();
            fos.close();

        } catch (Exception e) {

        }
    }

    public void printFiles(){
        for (File f : backedUpFiles) {
            System.out.println(f.getPathFile());
        }
    }

}
