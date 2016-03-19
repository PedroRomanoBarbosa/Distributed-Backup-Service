

public class RestoreProtocol implements Protocol{
    String message;
    DataSocket controlSocket, restoreSocket;

    public RestoreProtocol(DataSocket cs, DataSocket rs){
        message = "";
    }

    @Override
    public void send() {

    }

    @Override
    public void receive() {

    }
}
