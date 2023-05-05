import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RequestHandler extends Remote {
    boolean isReady() throws RemoteException;
    boolean requestSyncPermission() throws RemoteException;
    boolean acceptSyncRequest() throws RemoteException;
    void setRemoteIsMyTurn(boolean isMyTurn) throws RemoteException;

    void setRemoteTurn(int turn) throws RemoteException;

}