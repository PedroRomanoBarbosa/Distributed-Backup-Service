import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class BackupProtocol {
    private Scanner reader = new Scanner(System.in);

    public BackupProtocol() {
        //Verificar a validade do path/ficheiro
        System.out.print("Path to file to backup: ");
        String filePath = reader.next();
        while (true) {
            try {
                if (Files.isDirectory(Paths.get(filePath)) || !Files.exists(Paths.get(filePath)))
                    throw new IOException();

                break;
            } catch(IOException e){
                System.out.println("Error: Not a valid path for a file.");
                continue;
            }
        }
        //Grau de replicacao
        System.out.print("Replication Degree (1-9): ");
        int replicationDegree = 0;
        while(replicationDegree < 1 || replicationDegree > 9)  {
            try {
                replicationDegree = reader.nextInt();
                break;

            } catch (NumberFormatException e) {
                System.out.println("Invalid choice! Please insert a number between 1 and 9: ");
            }
        }

        File fileToBackup = new File(filePath, replicationDegree);
    }

    public void send(){

    }

    public void receive(){

    }
}
