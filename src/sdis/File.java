package sdis;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class File {
    private java.io.File actualFile;
    private String id;
    private String pathFile;
    private int replicationDegree;
    private int chunksSize;
    private int chunkSize = 64 * 1000;
    private HashMap<Integer, byte[]> chunks = new HashMap<Integer, byte[]>();
    Vector<Vector<InetAddress>> peersWithChunk = new Vector<Vector<InetAddress>>();


    public File(String filePath, int repDegree) throws NoSuchAlgorithmException {

        //ArrayList<ArrayList<InetAddress>> nome = new ArrayList<ArrayList<InetAddress>>();
       // nome.get(0).size();

        pathFile = filePath;
        replicationDegree = repDegree;

        chunksSize = (int) Math.ceil((new java.io.File(filePath).length() / (double) chunkSize));

        //CRIAR O CAMPO ID COM SHA256
        actualFile = new java.io.File(pathFile);
        String idAux = pathFile + actualFile.getName() + Long.toString(actualFile.lastModified());
        id = sha256(idAux);

        try {
            buildChunks(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //FONTE: http://www.sha1-online.com/sha256-java/
    private static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    //FONTE: http://stackoverflow.com/questions/9588348/java-read-file-by-chunks
    private void buildChunks(String filePath) throws IOException {
        byte[] buffer = new byte[chunkSize];
        FileInputStream in = new FileInputStream(filePath);

        int bytesRead = in.read(buffer);

        while (bytesRead != -1) {

            chunks.put(chunks.size(), buffer);

            //Cria vetor para graus de replicacao
            peersWithChunk.add(new Vector<>());

           // chunks.put(chunksSize, buffer);
            bytesRead = in.read(buffer);

        }
    }

    public void addChunkReplication(int chuckNumb, InetAddress address) {
        peersWithChunk.get(chuckNumb).add(address);
    }

    public void getChunkReplication(int chuckNumb) {
        peersWithChunk.get(chuckNumb).size();
    }

    public HashMap<Integer, byte[]> getChunks(){
        return chunks;
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

    public String getPathFile() {
        return pathFile;
    }



}
