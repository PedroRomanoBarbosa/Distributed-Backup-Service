import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        if(args.length != 7){
            System.err.println("Invalid number of arguments");
            System.exit(1);
        }

        System.out.println("===== Distributed Backup Service =====");
        Scanner reader = new Scanner(System.in);

        //INPUT PARA O PATH DO LOCAL DE ARMAZENAMENTO DOS CHUNKS
        while (true) {
            //try {
                System.out.print("Path to the data stored: ");
                String path = reader.next();

                /*
                TODO Verificar o path e fazer load da database
                 */

                break;
            /*} catch(IOException e){
                System.out.println("Please choose a valid directory!");
            }*/
        }

        int ip = Integer.parseInt(args[0]);
        int port1 = Integer.parseInt(args[2]);
        int port2 = Integer.parseInt(args[4]);
        int port3 = Integer.parseInt(args[6]);
        Peer p = new Peer(ip,args[1],port1,args[3],port2,args[5],port3);
        p.initialize();
        p.start();

        //MENU INICIAL
        int option = 0;
        while (option != 5) {
            System.out.println("\n1. File Backup");
            System.out.println("2. File Restore");
            System.out.println("3. File Deletion");
            System.out.println("4. Space Reclaiming");
            System.out.println("5. Exit");
            System.out.print("Choice: ");

            try {
                option = Integer.parseInt(reader.next());

                switch (option) {
                    case 1:
                        System.out.println("---- File Backup ----");
                        //TODO Chamar a funcao/classe aqui
                        break;
                    case 2:
                        System.out.println("---- File Restore ----");
                        //TODO Chamar a funcao/classe aqui
                        break;
                    case 3:
                        System.out.println("---- File Deletion ----");
                        //TODO Chamar a funcao/classe aqui
                        break;
                    case 4:
                        System.out.println("---- Space Reclaiming ----");
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
                System.out.println("Select a valid option!");
            }
        }

    }
}