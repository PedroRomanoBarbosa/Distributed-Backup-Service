package sdis.Interface;

import sdis.Utils.Regex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class TestApp {
    private static final String pattern = "^(([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}):)?([0-9]+)$";

    private static boolean local;
    private static InetAddress serverIp;
    private static int serverPort;
    private static String protocol,op1,op2;
    private static Socket socket;
    private static DataOutputStream os;
    private static DataInputStream is;

    public static void main(String args[]){
        if(args.length != 4 && args.length != 3){
            System.err.println("Invalid number of arguments");
            System.err.println("usage: TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            System.exit(1);
        }

        getArguments(args);

        initalize();

        communicate();
    }

    private static void getArguments(String[] args){
        // Get peer ip and port
        Regex regex = new Regex(pattern);
        ArrayList<String> groups = regex.getGroups(args[0]);
        if(groups.isEmpty()){
            System.err.println("Invalid Ip and port address representation");
            System.exit(1);
        }else if(groups.get(1) == null){
            local = true;
            try {
                serverIp = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            serverPort = Integer.parseInt(groups.get(2));
        }else {
            local = false;
            try {
                serverIp = InetAddress.getByName(groups.get(1));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            serverPort = Integer.parseInt(groups.get(2));
        }

        // Get protocol
        if(args[1].equals("BACKUP") | args[1].equals("RESTORE") | args[1].equals("DELETE") | args[1].equals("RECLAIM")){
            protocol = args[1];
        }else {
            System.err.println("Invalid protocol. Try:\n"  +
                    "RESTORE\n" +
                    "BACKUP\n" +
                    "DELETE\n" +
                    "RECLAIM\n");
            System.exit(1);
        }

        // Get operands
        if(args.length == 3){
            if(!protocol.equals("BACKUP")){
                op1 = args[2];
                op2 = null;
            }else {
                System.err.println(usage());
                System.exit(1);
            }
        }
        if(args.length == 4){
            if(protocol.equals("BACKUP")){
                op1 = args[2];
                op2 = args[3];
            }else {
                System.err.println(usage());
                System.exit(1);
            }
        }
    }

    private static void initalize(){
        // Initialize socket and streams
        try {
            socket = new Socket(serverIp,serverPort);
            os = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void communicate(){
        String message = protocol + " " + op1;
        if(protocol.equals("BACKUP")){
            message += " " + op2;
        }
        try {
            //Write message
            os.write(message.getBytes(),0,message.length());

            //Read message
            byte[] packet = new byte[100];
            is.read(packet,0,packet.length);
            System.out.println(new String(packet).trim());
            is.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String usage(){
        return "Invalid arguments for the protocol: " + protocol + ". Try " +
                "RESTORE\n" +
                "BACKUP\n" +
                "DELETE\n" +
                "RECLAIM\n";
    }

}
