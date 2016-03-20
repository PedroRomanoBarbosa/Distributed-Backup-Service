package sdis.Protocols;
import sdis.*;

public class RestoreProtocol {
    String message;
    DataSocket controlSocket, restoreSocket;

    public RestoreProtocol(DataSocket cs, DataSocket rs){
        message = "";
    }

}
