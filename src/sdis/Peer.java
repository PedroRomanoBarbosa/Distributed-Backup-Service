package sdis;

import sdis.MulticastChannels.MC;
import sdis.MulticastChannels.MDB;
import sdis.Protocols.BackupProtocol;
import sdis.Protocols.FileDeletionProtocol;
import sdis.Protocols.RestoreProtocol;
import sdis.Utils.Regex;

import java.io.*;
import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Peer {
    //Constants
    private final int ID;
    private final InetAddress MC_IP, MDB_IP, MDR_IP;
    private final int MC_PORT, MDB_PORT, MDR_PORT;
    private final String clientPattern = "^(BACKUP|RESTORE|DELETE|RECLAIM)\\s(.+?)(\\s([0-9]))?$";

    //TCP client connection
    Socket socket;
    DataInputStream is;
    DataOutputStream os;

    private boolean active;
    private DataSocket controlSocket, backupSocket, restoreSocket;
    private MulticastThread multicastDataRestore;
    private MC multicastControl;
    private MDB multicastDataBackup;
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

        //FONTE: http://stackoverflow.com/questions/3153337/get-current-working-directory-in-java
        final String path = System.getProperty("user.dir");
        //Load
        try {
            FileInputStream fis = new FileInputStream(path + java.io.File.separator + ID + ".info");
            ObjectInputStream ois = new ObjectInputStream(fis);

            fileStorage = (FileStorage) ois.readObject();

            ois.close();
            fis.close();

        } catch (Exception e) {
            fileStorage = new FileStorage(path);
        }

        multicastControl = new MC(this, fileStorage);
        multicastDataBackup = new MDB(this, fileStorage);
        restoreThread = new RestoreThread(this,"restore");

        //Initialize TCP socket
        try {
            serverSocket = new ServerSocket(ID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        restoreThread.start();
        multicastControl.start();
        multicastDataBackup.start();

        //Main loop for serving the client interface
        while (active){
            try {
                //Receive
                socket = serverSocket.accept();
                is = new DataInputStream(socket.getInputStream());
                os = new DataOutputStream(socket.getOutputStream());
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
                                filename = groups.get(1);
                                degree = Integer.parseInt(groups.get(3));
                                new BackupProtocol(this, fileStorage, filename, degree);
                                break;
                            case "RESTORE":
                                //Create file path
                                filename = groups.get(1);
                                String cwd = System.getProperty("user.dir");
                                String filePath = cwd + File.separator + filename;
                                RestoreProtocol rp = new RestoreProtocol(this);
                                rp.getChunks(filePath);
                                break;
                            case "DELETE":
                                filename = groups.get(1);
                                new FileDeletionProtocol(this, fileStorage, filename);
                                break;
                            case "RECLAIM":
                                //TODO
                                break;
                            default:
                                System.out.println("Invalid protocol!");
                                break;
                        }
                    }else {
                        sendToClient("Invalid Message");
                    }
                }else {
                    sendToClient("Invalid Message");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public FileStorage getFileStorage(){
        return fileStorage;
    }

    public RestoreThread getRestoreThread(){
        return restoreThread;
    }

    public void sendToClient(String message){
        message = "[SERVER] " + message;
        try {
            os.write(message.getBytes(),0,message.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
