package sdis.Protocols;

import sdis.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Scanner;

public class FileDeletionProtocol {

    public FileDeletionProtocol(Peer peer, FileStorage fileStorage, String filename) {

        //Tenta apagar do sistema de ficheiros
        try {
            Files.delete(Paths.get(fileStorage.getBackedUpFilesByPath(filename).getPathFile()));
        } catch (NoSuchFileException e) {
            //Ficheiro ja nao existe
        } catch (IOException e) {
            System.out.println("Error while deleting file.");
        }


        //Prepara a mensagem para a enviar
        //TODO Alterar o campo 1.0 para Versao Real
        String messageHeader = "DELETE " + "1.0" + " " + peer.getID() + " " + fileStorage.getBackedUpFilesByPath(filename).getFileID() + "\r\n\r\n";
        System.out.println(messageHeader);

        try {
            send(peer, messageHeader.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("File " + fileStorage.getBackedUpFilesByPath(filename) + " deleted.");

        //Elimina da "base de dados"
        fileStorage.getBackedUpFiles().remove(fileStorage.getBackedUpFilesByPath(filename));
        fileStorage.updateDataBase();
}

    public void send(Peer peer, byte[] message) {
        DatagramPacket packet = new DatagramPacket(message, message.length, peer.getMC_IP(), peer.getMC_PORT());

        try {
            peer.getBackupSocket().send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
