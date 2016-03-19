import java.util.Scanner;

public class FileDeletionProtocol implements Protocol {
    private Scanner reader = new Scanner(System.in);

    public FileDeletionProtocol(Peer peer) {
        //TODO Verificar se a database de files for nula, termina porque nao tem ficheiros para apagar

        System.out.println("--- Backed Up Files ---");

        //TODO Mostrar a lista de ficheiros que fez backup
        System.out.print("Select which file you want delete? ");

        int choice = -1;

        int size = 1;

        //TODO alterar para um size real
        while(choice < 0 || choice > size)  {
            try {
                choice = Integer.parseInt(reader.next());

            } catch (NumberFormatException e) {
                System.out.print("Invalid choice! Please insert a valid number of a file to delete: ");
            }
        }

        //TODO apagar o ficheiro do sistema de ficheiros


}

    @Override
    public void send() {

    }

    @Override
    public void receive() {

    }
}
