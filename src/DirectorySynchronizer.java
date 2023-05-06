import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface DirectorySynchronizer extends Remote {
    List<String[]> listDestinationDirectory() throws IOException, RemoteException;
    void updateSourceDirectory(List<String[]> files) throws IOException, RemoteException;
    void removeFiles(Set<String> filePaths) throws IOException, RemoteException;
    void handleDeletedFiles(Set<String> deletedFiles) throws RemoteException, IOException;

    void initialize (String sourcePath)  throws IOException, NotBoundException, RemoteException ;
    List<String[]> listSourceDirectory() throws IOException;

}
