public class File {
    private String id;
    private String pathFile;
    private int replicationDegree;

    public File(String filePath, int repDegree) {

        pathFile = filePath;
        replicationDegree = repDegree;
    }




    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setReplicationDegree(int repD) {
        replicationDegree = repD;
    }


}
