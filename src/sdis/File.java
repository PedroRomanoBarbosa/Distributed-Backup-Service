package sdis;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class File implements Serializable {
    private java.io.File actualFile;
    private String id;
    private String pathFile;
    private String fileName;
    private int replicationDegree;
    public int numChunks;
    private int chunkSize = 64 * 1000;
    private HashMap<Integer, Vector<InetAddress>> peersWithChunk;

    public File(String filePath, int repDegree) throws NoSuchAlgorithmException {
        peersWithChunk = new HashMap<Integer, Vector<InetAddress>>();

        pathFile = filePath;
        replicationDegree = repDegree;

        //CRIAR O CAMPO ID COM SHA256
        actualFile = new java.io.File(pathFile);
        System.out.println(actualFile.length());
        numChunks = (int)(actualFile.length()/64000);
        if((int)(actualFile.length()%64000) != 0){
            numChunks++;
        }
        System.out.println(numChunks);
        fileName = actualFile.getName();
        String idAux = pathFile + fileName + Long.toString(actualFile.lastModified());
        id = sha256(idAux);

       /* try {
            buildChunks(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    //Uusado para os ficheiros stored
    public File(String ident, int repDegree, int notUsed) {
        //chunks = new HashMap<Integer, byte[]>();
        replicationDegree = repDegree;
        id = ident;
        peersWithChunk = new HashMap<Integer, Vector<InetAddress>>();
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
    public HashMap<Integer, byte[]> buildChunks(String filePath) throws IOException {
        HashMap<Integer, byte[]> chunks = new HashMap<Integer, byte[]>();

        byte[] buffer = new byte[chunkSize];
        FileInputStream in = new FileInputStream(filePath);

        int bytesRead = in.read(buffer);

        int count = 0;

        while (bytesRead != -1) {
            if(bytesRead < chunkSize){
                buffer = Arrays.copyOf(buffer,bytesRead);
                chunks.put(chunks.size(), buffer);
            }else {
                chunks.put(chunks.size(), buffer);
            }

            //Cria vetor para graus de replicacao
            peersWithChunk.put(count, new Vector<>());

            buffer = new byte[chunkSize];
            bytesRead = in.read(buffer);
            count++;
        }
        return chunks;
    }

    public synchronized void addChunkReplication(int chuckNumb, InetAddress address) {
        if (!peersWithChunk.containsKey(chuckNumb))
            peersWithChunk.put(chuckNumb, new Vector<>());

        if (!peersWithChunk.get(chuckNumb).contains(address))
            peersWithChunk.get(chuckNumb).add(address);
    }

    public int getChunkReplication(int chuckNumb) {
        return peersWithChunk.get(chuckNumb).size();
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public String getFileID() {
        return id;
    }

    public String getPathFile() {
        return pathFile;
    }

    public synchronized int getNoChunks() {
        return peersWithChunk.size();
    }

    public synchronized HashMap<Integer,Vector<InetAddress>> getPeersWithChunk(){
        return peersWithChunk;
    }


    public synchronized void storeChunk(int chunkId, byte[] chunk) {
        try {
            String path = System.getProperty("user.dir") + java.io.File.separator + id;
            new java.io.File(path).mkdirs();

            FileOutputStream fos = new FileOutputStream(path + java.io.File.separator + chunkId);
            peersWithChunk.put(chunkId, new Vector<>());
            fos.write(chunk);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //http://stackoverflow.com/questions/20281835/how-to-delete-a-folder-with-files-using-java
    public synchronized void removeChunks() {
        java.io.File index = new java.io.File(System.getProperty("user.dir") + java.io.File.separator + id);
        String[]entries = index.list();
        for(String s: entries){
            java.io.File currentFile = new java.io.File(index.getPath(),s);
            currentFile.delete();
        }
        index.delete();
    }


    public synchronized boolean decreaseReplicationDegree(int chunkNumber, InetAddress addr){
        Vector<InetAddress> peersAddr = peersWithChunk.get(chunkNumber);
        for (int i = 0; i < peersAddr.size(); i++) {
            if(peersAddr.get(i).equals(addr)){
                peersAddr.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof File))return false;

        File otherMyClass = (File)other;

        if(otherMyClass.getFileID().equals(id))
            return true;
        else
            return false;
    }

}
