package sdis.Protocols;

import sdis.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Scanner;

public class FileDeletionProtocol {
    private Scanner reader = new Scanner(System.in);

    public FileDeletionProtocol(Peer peer, FileStorage fileStorage) {
        System.out.println("--- Backed Up Files ---");

        //Verifica se ja fez backup de algum ficheiro para a rede
        if (fileStorage.getBackedUpFiles().size() == 0) {
            System.out.println("No backed up files to show!");
            return;
        }

        //Mostra a lista de ficheiros que ja fez backup para a rede
        int i = 1;
        for (File file : fileStorage.getBackedUpFiles())
            System.out.println(i + ". " + file.getPathFile() + " - " + file.getFileID());

        System.out.print("Select which file you want delete: ");

        int choice = -1;

        //Escolhe o index do que deseja apagar
        while(choice < 0 || choice > fileStorage.getBackedUpFiles().size() )  {
            try {
                choice = Integer.parseInt(reader.next());

            } catch (NumberFormatException e) {
                System.out.print("Invalid choice! Please insert a valid number of a file to delete: ");
            }
        }

        if (choice == 0)
            return;

        //Tenta apagar do sistema de ficheiros
        try {
            Files.delete(Paths.get(fileStorage.getBackedUpFiles().get(choice-1).getPathFile()));
        } catch (NoSuchFileException e) {
            //Ficheiro ja nao existe
        } catch (IOException e) {
            System.out.println("Error while deleting file.");
        }


        //Prepara a mensagem para a enviar
        //TODO Alterar o campo 1.0 para Versao Real
        String messageHeader = "DELETE " + "1.0" + " " + peer.getID() + " " + fileStorage.getBackedUpFiles().get(choice-1).getFileID() + "\r\n\r\n";
        System.out.println(messageHeader);

        try {
            send(peer, messageHeader.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("File " + fileStorage.getBackedUpFiles().get(choice-1) + " deleted.");

        //Elimina da "base de dados"
        fileStorage.getBackedUpFiles().remove(choice-1);
}

    public void send(Peer peer, byte[] message) {
        DatagramPacket packet = new DatagramPacket(message, message.length, peer.getMC_IP(), peer.getMC_PORT());

        try {
            peer.getBackupSocket().send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receive() {

    }
}
