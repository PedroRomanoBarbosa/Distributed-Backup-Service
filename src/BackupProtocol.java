import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class BackupProtocol {
    private Scanner reader = new Scanner(System.in);
    private int MaxReplicationDegree = 9;

    public BackupProtocol() {
        //Verificar a validade do path/ficheiro
        String filePath;

        while (true) {
            System.out.print("Path to file to backup: ");
            filePath = reader.next();
            //filePath = "C:\\Users\\User\\Desktop\\SDIS.txt";

            try {
                if (Files.isDirectory(Paths.get(filePath)) || !Files.exists(Paths.get(filePath)))
                    throw new IOException();

                break;
            } catch(IOException e){
                System.out.println("Error: Not a valid path for a file!");
                continue;
            }
        }

        //Grau de replicacao
        System.out.print("Replication Degree (1-9): ");
        int replicationDegree = 0;
        while(replicationDegree < 1 || replicationDegree > MaxReplicationDegree)  {
            try {
                replicationDegree = reader.nextInt();
                break;

            } catch (NumberFormatException e) {
                System.out.println("Invalid choice! Please insert a number between 1 and 9: ");
            }
        }

        File fileToBackup = null;
        try {
            fileToBackup = new File(filePath, replicationDegree);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < fileToBackup.getChunksSize(); i++) {
            //TODO Alterar o campo 1.0 para Versao Real e Adicionar SenderID
            String messageHeader = "PUTCHUNK " + "1.0" + " " + fileToBackup.getFileID() + " " + i + " " +
                   fileToBackup.getReplicationDegree() + " " + "\r\n\r\n";

            System.out.println(messageHeader);
            //String fullMessage = concatByteArrays(messageHeader.getBytes(), fileToBackup.getChunk(i));
        }
    }

    public void send(){

    }

    public void receive(){

    }
}
