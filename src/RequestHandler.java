import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RequestHandler extends Remote {
    boolean isReady() throws RemoteException;
    boolean requestSyncPermission() throws RemoteException;
    boolean acceptSyncRequest() throws RemoteException;
    void setRemoteIsMyTurn(boolean isMyTurn) throws RemoteException;

    void setRemoteTurn(int turn) throws RemoteException;

    void stopSync() throws RemoteException;

}