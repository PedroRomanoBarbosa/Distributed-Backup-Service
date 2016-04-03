package sdis.MulticastChannels;

import sdis.FileStorage;
import sdis.Peer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class MDB extends Thread {

    private Peer peer;
    private FileStorage fileStorage;
    private ConcurrentHashMap<String,String> ignoreChunks;

    public MDB(Peer p, FileStorage fileSt) {
        peer = p;
        fileStorage = fileSt;
        ignoreChunks = new ConcurrentHashMap<>();
    }

    public synchronized void addIgnoreChunk(String id){
        ignoreChunks.put(id,id);
    }

    public synchronized boolean checkIgnoredChunk(String id){
        return !(ignoreChunks.get(id) == null);
    }

    public synchronized void clearIgnoreChunks(){
        ignoreChunks.clear();
    }

    @Override
    public void run() {
        while (true) {
            try {
                DatagramPacket packet = peer.getBackupSocket().receivePacket(64512);
                byte[] data = Arrays.copyOfRange(packet.getData(),packet.getOffset(),packet.getLength());
                //String message = new String(packet.getData(), 0, packet.getLength());
                new ReceiveThread(packet.getAddress(), data).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class ReceiveThread extends Thread {
        InetAddress address;
        byte[] messag;

        public ReceiveThread(InetAddress addr, byte[] mess){
            address = addr;
            messag = mess;
        }

        public void run() {
            try {
                messageDealer(address, messag);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        private void messageDealer(InetAddress address, byte[] messag) throws UnsupportedEncodingException, UnknownHostException {
            String message[] = new String(messag, "UTF-8").split(" ");

            if (Integer.parseInt(message[2]) != peer.getID()) {
                switch (message[0]) {
                    case "PUTCHUNK": {
                        System.out.println("PUTCHUNK");
                        if (!checkIgnoredChunk(message[3] + "/" + message[4])) {
                            sdis.File file = fileStorage.getStoredFilesById(message[3]);

                            //http://stackoverflow.com/questions/642897/removing-an-element-from-an-array-java
                            int bytesRemoved = 0;
                            for (int i = 0; i < messag.length; i++) {
                                if (messag[i] == (byte) '\r' &&
                                        messag[i + 1] == (byte) '\n' &&
                                        messag[i + 2] == (byte) '\r' &&
                                        messag[i + 3] == (byte) '\n') {
                                    for (int j = 0; j <= i + 3; j++, bytesRemoved++)
                                        System.arraycopy(messag, 1, messag, 0, messag.length - 1);
                                    break;
                                }
                            }
                            messag = Arrays.copyOf(messag, messag.length - bytesRemoved);

                            if (!fileStorage.verifyCapacity(messag.length))
                                return;

                            //Se ainda nao tiver recebido nenhum chunk do ficheiro
                            if (file == null) {
                                file = new sdis.File(message[3], Integer.parseInt(message[5]), 0);

                                //file.getChunks().put(Integer.parseInt(message[4]), messag);
                                file.storeChunk(Integer.parseInt(message[4]), messag);

                                file.addChunkReplication(Integer.parseInt(message[4]), InetAddress.getByName(Integer.toString(peer.getID())));
                                file.setChunkSize(Integer.parseInt(message[4]), messag.length);

                                if (!fileStorage.getStoredFiles().contains(file)) {
                                    fileStorage.addStoredFile(file);
                                }
                            }

                            //Se ja tiver recebido algum chunk do ficheiro apenas adiciona o chunk
                            else {
                                // file.getChunks().put(Integer.parseInt(message[4]), messag);
                                file.storeChunk(Integer.parseInt(message[4]), messag);
                                file.addChunkReplication(Integer.parseInt(message[4]), InetAddress.getByName(Integer.toString(peer.getID())));
                                file.setChunkSize(Integer.parseInt(message[4]), messag.length);
                            }

                            //Update dos dados a guardar
                            fileStorage.updateDataBase(peer.getID());

                            //Cria resposta
                            String responseHeader = "STORED " + "1.0" + " " + peer.getID() + " " + message[3] + " " + message[4] + " " + "\r\n\r\n";

                            //Faz o sleep
                            try {
                                Thread.sleep((long) (Math.random() * 400));
                            } catch (InterruptedException e) {
                            }

                            //Envio da mensagem
                            try {
                                DatagramPacket packet = new DatagramPacket(responseHeader.getBytes(), responseHeader.getBytes().length, peer.getMC_IP(), peer.getMC_PORT());

                                try {
                                    peer.getControlSocket().send(packet);
                                    System.out.println(responseHeader);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    default:
                        break;
                }
            }
        }
    }
}