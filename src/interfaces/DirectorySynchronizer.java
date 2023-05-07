package interfaces;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.util.List;
import java.util.Set;

/**
 * This interface provides methods for synchronizing directories between two directories.
 */
public interface DirectorySynchronizer extends Remote {

    /**
     * Lists the files and directories in the destination directory.
     *
     * @return A list of string arrays containing information about the files and directories.
     * @throws IOException If an I/O error occurs.
     */
    List<String[]> listDestinationDirectory() throws IOException;

    /**
     * Updates the source directory with new files from the provided list.
     *
     * @param files The list of files to update in the source directory.
     * @throws IOException If an I/O error occurs.
     */
    void updateSourceDirectory(List<String[]> files) throws IOException;

    /**
     * Removes obsolete files from the destination directory.
     *
     * @param filePaths The paths of the files to delete.
     * @throws IOException If an I/O error occurs.
     */
    void removeFiles(Set<String> filePaths) throws IOException;

    /**
     * Handles files deleted from the source directory.
     *
     * @param deletedFiles The files deleted from the source directory.
     * @throws IOException If an I/O error occurs.
     */
    void handleDeletedFiles(Set<String> deletedFiles) throws IOException;

    /**
     * Initializes the synchronization by setting the path of the source directory.
     *
     * @param sourcePath The path of the source directory.
     * @throws IOException If an I/O error occurs.
     * @throws NotBoundException If the RMI server is not found.
     */
    void initialize(String sourcePath) throws IOException, NotBoundException;

    /**
     * Lists the files and directories in the source directory.
     *
     * @return A list of string arrays containing information about the files and directories.
     * @throws IOException If an I/O error occurs.
     */
    List<String[]> listSourceDirectory() throws IOException;
}
