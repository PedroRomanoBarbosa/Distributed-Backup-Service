package sdis;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Peer {
    private final int ID;
    private boolean active;
    private final InetAddress MC_IP, MDB_IP, MDR_IP;
    private final int MC_PORT, MDB_PORT, MDR_PORT;
    private DataSocket controlSocket,backupSocket, restoreSocket;
    private MulticastThread multicastControl,multicastDataBackup,multicastDataRestore;
    private RestoreThread restoreThread;

    public Peer(int id, String mcIp, int mcPort, String mdbIp, int mdbPort, String mdrIp, int mdrPort){
        ID = id;
        InetAddress mc_ip=null;
        try {
            mc_ip = InetAddress.getByName(mcIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.err.println("Invalid multicast channel ip");
        }
        MC_IP = mc_ip;
        MC_PORT = mcPort;

        InetAddress mdb_ip=null;
        try {
            mdb_ip = InetAddress.getByName(mdbIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.err.println("Invalid multicast data backup ip");
        }
        MDB_IP = mdb_ip;
        MDB_PORT = mdbPort;

        InetAddress mdr_ip=null;
        try {
            mdr_ip = InetAddress.getByName(mdrIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.err.println("Invalid multicast data restore ip");
        }
        MDR_IP = mdr_ip;
        MDR_PORT = mdrPort;

        active = true;
    }

    public void initialize(){
        try {
            controlSocket = new DataSocket(MC_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error creating multicast channel socket");
        }
        try {
            controlSocket.joinGroup(MC_IP);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error joining multicast channel group");
        }

        try {
            backupSocket = new DataSocket(MDB_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error creating multicast data backup channel socket");
        }
        try {
            backupSocket.joinGroup(MDB_IP);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error joining multicast data backup channel group");
        }

        try {
            restoreSocket = new DataSocket(MDR_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error creating multicast data restore channel socket");
        }
        try {
            restoreSocket.joinGroup(MDR_IP);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error joining multicast data restore channel group");
        }

        multicastControl = new MulticastThread(this);
        multicastDataBackup = new MulticastThread(this);
        restoreThread = new RestoreThread(this);
    }

    public void start(){
        restoreThread.start();
        while (active){

        }
    }
	
	public final int getID() {
        return ID;
    }

    public DataSocket getRestoreSocket(){
        return restoreSocket;
    }

    public DataSocket getBackupSocket(){
        return backupSocket;
    }

    public DataSocket getControlSocket(){
        return controlSocket;
    }

    public InetAddress getMC_IP() {
        return MC_IP;
    }

    public InetAddress getMDB_IP() {
        return MDB_IP;
    }

    public InetAddress getMDR_IP() {
        return MDR_IP;
    }

    public int getMC_PORT() {
        return MC_PORT;
    }

    public int getMDB_PORT() {
        return MDB_PORT;
    }

    public int getMDR_PORT() {
        return MDR_PORT;
    }
}
