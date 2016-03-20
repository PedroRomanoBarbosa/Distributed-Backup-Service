

public class RestoreProtocol implements Protocol{
    String message;
    DataSocket controlSocket, restoreSocket;

    public RestoreProtocol(DataSocket cs, DataSocket rs){
        message = "";
        controlSocket = cs;
        restoreSocket = rs;
    }

    @Override
    public void send() {

    }

    @Override
    public void receive() {

    }
}
