import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DirectorySynchronizer extends Remote {
    List<String[]> listSourceDirectory() throws IOException, RemoteException;
    void updateSourceDirectory(List<String[]> files) throws RemoteException, IOException;
    public List<String[]> listDestinationDirectory() throws IOException;

}