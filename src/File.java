import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class File {
    private String id;
    private String pathFile;
    private int replicationDegree;
    private int chunksSize;

    public File(String filePath, int repDegree) throws NoSuchAlgorithmException {

        pathFile = filePath;
        replicationDegree = repDegree;

        chunksSize = (int) Math.ceil((new java.io.File(filePath).length() / (double) 64000));

        //CRIAR O CAMPO ID COM SHA256
        String idAux = pathFile + Long.toString(new java.io.File(pathFile).lastModified());
        id = sha256(idAux);

    }

    //FONT: http://www.sha1-online.com/sha256-java/
    static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }




    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setReplicationDegree(int repD) {
        replicationDegree = repD;
    }

    public int getChunksSize() {
        return chunksSize;
    }

    public String getFileID() {
        return id;
    }


}
