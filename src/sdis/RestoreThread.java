package sdis;

import sdis.Utils.Regex;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;

public class RestoreThread extends MulticastThread{
    private String message;
    private Regex regex;

    public RestoreThread(Peer p){
        super(p);
        regex = new Regex("^(CHUNK)\\s+([0-9]\\.[0-9])\\s+([0-9]+)\\s+(.+)\\s+([0-9]+)\\s+\r\n\r\n$");
    }

    public void run() {
        while (active){
            message = "CHUNK 1.2 1234 fwefw6fw5ef6we5f6e 2 \r\n\r\n";
            ArrayList<String> args = regex.getGroups(message);
            if(args.size() != 0){
                if(args.get(0).equals("CHUNK")){
                    process(args);
                }
            }
            try {
                message = peer.getControlSocket().receive(64000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void process(ArrayList<String> args) {
        // Get the arguments from string representation
        String message;
        float version = Float.parseFloat(args.get(1));
        int senderId = Integer.parseInt(args.get(2));
        String fileId = args.get(3);
        int chunkNumber = Integer.parseInt(args.get(4));

        //Construct message header
        message = "CHUNK " + version + " " + senderId + " " + fileId + " " + chunkNumber + "\r\n\r\n";

        //Get file (incomplete)
        File f = new File("/Users/PedroBarbosa/Desktop/chunk1");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Create byte array to store the content of the chunk file.
        // Max size: 2147483647 bytes ~ 2 gigabytes(this can be changed later)
        byte[] content = new byte[(int)f.length()];
        try {
            fis.read(content,0,content.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Attach chunk to the message
        message += new String(content);

        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Create a random delay between
        // TODO: Check in a thread if this peer receives a CHUNK message.
        Random rand = new Random();
        int timeToWait = rand.nextInt(400);
        try {
            Thread.sleep(timeToWait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Send message containing the header and chunk content
        try {
            peer.getRestoreSocket().send(message,peer.getMDR_IP(),peer.getMDR_PORT());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Message sent!");
    }

}
