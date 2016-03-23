package sdis;

import sdis.Protocols.BackupProtocol;
import sdis.Utils.Regex;

import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Peer {
    private final int ID;
    private final InetAddress MC_IP, MDB_IP, MDR_IP;
    private final int MC_PORT, MDB_PORT, MDR_PORT;
    private final String clientPattern = "^(BACKUP|RESTORE|DELETE|RECLAIM)\\s(.+?)(\\s([0-9]))?$";

    private boolean active;
    private DataSocket controlSocket,backupSocket, restoreSocket;
    private MulticastThread multicastControl,multicastDataBackup,multicastDataRestore;
    private RestoreThread restoreThread;
    private ServerSocket serverSocket;
    private FileStorage fileStorage;

    public Peer(int id, String mcIp, int mcPort, String mdbIp, int mdbPort, String mdrIp, int mdrPort, FileStorage fileStor){
        fileStorage = fileStor;

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
        //Initialize UDP multicast sockets
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

        //Initialize TCP socket
        try {
            serverSocket = new ServerSocket(ID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        //FONTE: http://stackoverflow.com/questions/3153337/get-current-working-directory-in-java
        final String path = System.getProperty("user.dir");

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

        //Start multicast channels threads
       // restoreThread.start();

        //Main loop for serving the client interface
        while (active){
            try {
                //Receive
                Socket socket = serverSocket.accept();
                DataInputStream is = new DataInputStream(socket.getInputStream());
                DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                byte[] packet = new byte[512];
                int n = is.read(packet);
                if(n != -1){
                    Regex regex = new Regex();
                    regex.setPattern(clientPattern);
                    String clientMessage = new String(packet).trim();
                    ArrayList<String> groups = regex.getGroups(clientMessage);
                    if(!groups.isEmpty()){
                        String protocol = groups.get(0);
                        String filename = "";
                        int degree;
                        //TODO: Decide which parameter is used in each of the protocols and call the functions
                        switch (protocol){
                            case "BACKUP":
                                System.out.println("backup");
                                filename = groups.get(1);
                                degree = Integer.parseInt(groups.get(3));
                                new BackupProtocol(this, fileStorage, filename, degree);
                                break;
                            case "RESTORE":
                                filename = groups.get(1);
                                break;
                            case "DELETE":
                                filename = groups.get(1);
                                break;
                            case "RECLAIM":
                                break;
                        }
                    }else {
                        System.err.println("Invalid client message");
                    }
                }else {
                    System.err.println("Could not read client's message correctly");
                }

                //Send back
                String clientResponse = "Deu!";
                os.write(clientResponse.getBytes(),0,clientResponse.length());
                is.close();
                os.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
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
