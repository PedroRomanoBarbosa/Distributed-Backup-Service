import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public class FileStorage implements Serializable {

    private List<File> storedFiles = new Vector<File>();
    private List<File> backedUpFiles = new Vector<File>();
    private String path = null;

    public FileStorage(String p) {
        path = p;
    }

    public void addBackedUpFile(File file) {
        if (!backedUpFiles.contains(file))
            backedUpFiles.add(file);
    }

    public List<File> getStoredFiles() {
        return storedFiles;
    }

    public List<File> getBackedUpFiles() {
        return backedUpFiles;
    }

}
