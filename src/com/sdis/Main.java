package com.sdis;

public class Main {

    public static void main(String[] args) {
        if(args.length != 7){
            System.err.println("Invalid number of arguments");
            System.exit(1);
        }
        int ip = Integer.parseInt(args[0]);
        int port1 = Integer.parseInt(args[2]);
        int port2 = Integer.parseInt(args[4]);
        int port3 = Integer.parseInt(args[6]);
        Peer p = new Peer(ip,args[1],port1,args[3],port2,args[5],port3);
        p.initialize();
        System.out.println("Finished");
    }
}
