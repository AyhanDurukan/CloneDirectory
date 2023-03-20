import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DirectorySynchronizer extends Remote {
    List<String[]> listSourceDirectory() throws RemoteException, IOException;
    void updateDestinationDirectory(List<String[]> files) throws RemoteException;
}