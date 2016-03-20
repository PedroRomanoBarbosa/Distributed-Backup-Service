package sdis.Interface;

import sdis.Utils.Regex;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class TestApp {
    private static Regex regex;
    private static InetAddress serverIp;
    private static int serverPort;
    private static String protocol,op1,op2;

    public static void main(String args[]){
        if(args.length != 4 && args.length != 3){
            System.out.println("Invalid number of arguments");
            System.out.println("usage: TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            System.exit(1);
        }

        try {
            Socket client = new Socket(serverIp,serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
