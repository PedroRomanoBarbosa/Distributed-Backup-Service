package sdis;

import sdis.Protocols.*;

import java.io.*;
import java.net.ServerSocket;
import java.util.Scanner;

public class Main {
    public static FileStorage fileStorage = null;
    private static ServerSocket serverSocket;

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

    }
}