package sdis;

import sdis.Protocols.*;

import java.io.*;
import java.util.Scanner;

public class Main {
    public static FileStorage fileStorage = null;

    public static void main(String[] args) {
        if(args.length != 7){
            System.err.println("Invalid number of arguments");
            System.exit(1);
        }

        //Inicia o Peer
        Peer p = new Peer(Integer.parseInt(args[0]),args[1],Integer.parseInt(args[2]),args[3],Integer.parseInt(args[4]),args[5],Integer.parseInt(args[6]));
        p.initialize();
        p.start();

        System.out.println("===== Distributed Backup Service =====");
        Scanner reader = new Scanner(System.in);

        //INPUT PARA O PATH DO LOCAL DE ARMAZENAMENTO DOS CHUNKS
        while (true) {
            try {
                System.out.print("Path to the data stored: ");
                String path = reader.next();

                /*
                Faz load da database
                 */
                java.io.File file = new java.io.File(path);

                if(!file.isDirectory())
                    throw new IOException();

                else {
                    //Load
                    try {
                        FileInputStream fis = new FileInputStream(path + java.io.File.separator + ".info");
                        ObjectInputStream ois = new ObjectInputStream(fis);

                        fileStorage = (FileStorage) ois.readObject();

                        ois.close();
                        fis.close();

                    } catch (Exception e) {
                        fileStorage = new FileStorage(path);
                    }

                    //Save
                    try {
                        FileOutputStream fos = new FileOutputStream(path + java.io.File.separator + ".info");
                        ObjectOutputStream oos = new ObjectOutputStream(fos);

                        oos.writeObject(fileStorage);

                        oos.close();
                        fos.close();

                    } catch (Exception e) {

                    }

                    break;
                }

            } catch(IOException e){
                System.out.println("Please choose a valid directory!\n");
            }
        }

        //MENU INICIAL
        int option = 0;
        while (option != 5) {
            System.out.println("\n\n----- Menu -----");
            System.out.println("1. Chunk Backup");
            System.out.println("2. Chunk Restore");
            System.out.println("3. File Deletion");
            System.out.println("4. Space Reclaiming");
            System.out.println("5. Exit");
            System.out.print("Choice: ");

            try {
                option = Integer.parseInt(reader.next());

                switch (option) {
                    case 1:
                        System.out.println("\n---- File Backup ----");
                        new BackupProtocol(p, fileStorage);
                        break;
                    case 2:
                        System.out.println("\n---- File Restore ----");
                        //TODO Chamar a funcao/classe aqui
                        break;
                    case 3:
                        System.out.println("\n---- File Deletion ----");
                        new FileDeletionProtocol(p, fileStorage);
                        break;
                    case 4:
                        System.out.println("\n---- Space Reclaiming ----");
                        //TODO Chamar a funcao/classe aqui
                        break;
                    case 5:
                        //TODO Guardar os dados importantes antes de terminar?
                        System.exit(0);
                        break;
                    default:
                        break;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Select a valid option!\n");
            }
        }

    }
}