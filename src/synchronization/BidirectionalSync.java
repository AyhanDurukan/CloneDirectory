package synchronization;

import gui.MainFrame;
import interfaces.DirectorySynchronizer;
import interfaces.RequestHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * BidirectionalSync class provides the functionality to synchronize two directories bidirectionally
 * using RMI (Remote Method Invocation) and a separate synchronization thread.
 * This class implements DirectorySynchronizer, RequestHandler, and Runnable interfaces.
 */
public class BidirectionalSync extends UnicastRemoteObject implements DirectorySynchronizer, RequestHandler, Runnable {
    private String sourcePath;
    private String destinationPath;
    private File sourceDirectory;
    private File destinationDirectory;
    private long syncInterval;
    private boolean updateSource;
    private int instanceId;
    private int turn = 1;
    private String remoteIP;
    private int localPort;
    private int remotePort;

    /**
     * Constructor for BidirectionalSync class.
     *
     * @param syncInterval The interval between synchronization checks in milliseconds.
     * @param updateSource A boolean flag indicating if the source directory should be updated.
     * @throws RemoteException If a remote exception occurs during the RMI process.
     */
    public BidirectionalSync(long syncInterval, boolean updateSource) throws RemoteException {
        this.syncInterval = syncInterval;
        this.updateSource = updateSource;
    }

    /**
     * Initializes the destination path and directory.
     *
     * @param destinationPathe The destination path for synchronization.
     */
    public void initialize (String destinationPathe) {
        this.destinationPath = destinationPathe;
        this.destinationDirectory = new File(destinationPath);
    }

    /**
     * Initializes the synchronization process with the specified remote IP.
     *
     * @param remoteIp The IP address of the remote machine.
     * @throws Exception If an exception occurs during the initialization process.
     */
    public void initializeSynchronization(String remoteIp) throws Exception {
        try {
            DirectorySynchronizer other = (DirectorySynchronizer) Naming.lookup("rmi://" + remoteIp + ":"+remotePort+"/interfaces.DirectorySynchronizer");

            List<String[]> remoteFiles = other.listSourceDirectory();
            List<String[]> localFiles = listSourceDirectory();

            for (String[] remoteFile : remoteFiles) {
                boolean fileExists = localFiles.stream().anyMatch(localFile -> localFile[0].equals(remoteFile[0]));
                if (!fileExists) {
                    String filePath = remoteFile[0];
                    String fileContent = remoteFile[1];
                    long remoteTimestamp = Long.parseLong(remoteFile[2]);

                    File localFile = new File(sourceDirectory, filePath);

                    if (isDirectory(filePath, remoteFiles)) {
                        localFile.mkdirs();
                    } else {
                        localFile.getParentFile().mkdirs();
                        try (FileWriter fileWriter = new FileWriter(localFile)) {
                            fileWriter.write(fileContent);
                        }
                    }
                    localFile.setLastModified(remoteTimestamp);
                }
            }
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the synchronization process by starting the RMI server and the synchronization thread.
     */
    public void startSynchronization() {
        try {
            Registry registry = LocateRegistry.createRegistry(localPort);

            Naming.rebind("rmi://" + this.remoteIP + ":" + localPort + "/interfaces.DirectorySynchronizer", this);
            Naming.rebind("rmi://" + this.remoteIP + ":" + localPort + "/interfaces.RequestHandler", this);

            try {
                Thread.sleep(10000);
                DirectorySynchronizer destDirectory = (DirectorySynchronizer) Naming.lookup("rmi://" + this.remoteIP + ":" + remotePort + "/interfaces.DirectorySynchronizer");
                destDirectory.initialize(this.sourcePath);
            } catch (MalformedURLException | RemoteException | NotBoundException e) {
                System.err.println("PC unreachable waiting time exceeded: " + e.getMessage());
                MainFrame.message("PC unreachable waiting time exceeded");
                e.printStackTrace();
            }
            MainFrame.message("Serveur RMI lancé");

            this.waitForRemote(this.remoteIP);
            this.initializeSynchronization(this.remoteIP);

            Thread syncThread = new Thread(this);
            syncThread.start();

        } catch (Exception e) {
            System.err.println("Error when starting the RMI server : " + e.getMessage());
            MainFrame.message("Synchronisation is already in progress !");
            e.printStackTrace();
        }
    }

    /**
     * Waits for the remote machine to be ready for synchronization.
     *
     * @param remoteIp The IP address of the remote machine.
     * @throws RemoteException If a remote exception occurs during the RMI process.
     * @throws MalformedURLException If a malformed URL is encountered during the RMI process.
     * @throws NotBoundException If a NotBoundException occurs during the RMI process.
     */
    public void waitForRemote(String remoteIp) throws RemoteException, MalformedURLException, NotBoundException {
        RequestHandler other = (RequestHandler) Naming.lookup("rmi://" + remoteIp + ":"+localPort+"/interfaces.RequestHandler");

        try {
            MainFrame.message("Wait 10 seconds before checking if the other PC is ready...");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.err.println("Error on initial wait : " + e.getMessage());
            MainFrame.message("Error on initial wait");
            e.printStackTrace();
        }

        while (!other.isReady()) {
            try {
                MainFrame.message("Waiting for the other PC to connect...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Error while waiting for the other PC to connect : " + e.getMessage());
                MainFrame.message("Error while waiting for the other PC to connect");
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if the given path represents a directory in the list of files provided.
     *
     * @param path The relative path of the file or directory to be checked.
     * @param files A list of files with their paths, contents, and timestamps, in the format String[]{relativePath, content, timestamp}.
     * @return true if the path represents a directory, false otherwise.
     */
    private boolean isDirectory(String path, List<String[]> files) {
        for (String[] file : files) {
            if (file[0].equals(path) && file[1].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of string arrays representing the files in the source directory.
     * Each array contains the relative path, file content, and the last modified timestamp.
     *
     * @return A list of string arrays representing the files in the source directory.
     * @throws IOException If an I/O error occurs during file reading.
     */
    public List<String[]> listSourceDirectory() throws IOException {
        List<String[]> files = new ArrayList<>();
        listSourceDirectoryRecursive(sourceDirectory, files);
        return files;
    }

    /**
     * Recursively lists the files in the source directory and adds their information to the files list.
     *
     * @param directory The directory to be processed.
     * @param files The list of files to be populated with file information.
     * @throws IOException If an I/O error occurs during file reading.
     */
    private void listSourceDirectoryRecursive(File directory, List<String[]> files) throws IOException {
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles != null) {
            for (File file : directoryFiles) {
                String relativePath = sourceDirectory.toURI().relativize(file.toURI()).getPath();
                long timestamp = file.lastModified();

                if (file.isFile()) {
                    byte[] content = Files.readAllBytes(file.toPath());
                    String contentString = new String(content, StandardCharsets.UTF_8);
                    files.add(new String[]{relativePath, contentString, String.valueOf(timestamp)});
                } else if (file.isDirectory()) {
                    files.add(new String[]{relativePath, "", String.valueOf(timestamp)});
                    listSourceDirectoryRecursive(file, files);
                }
            }
        }
    }

    /**
     * Returns a list of string arrays representing the files in the destination directory.
     * Each array contains the relative path, file content, and the last modified timestamp.
     *
     * @return A list of string arrays representing the files in the destination directory.
     * @throws IOException If an I/O error occurs during file reading.
     */
    @Override
    public List<String[]> listDestinationDirectory() throws IOException {
        List<String[]> files = new ArrayList<>();
        listDestinationDirectoryRecursive(destinationDirectory, files);
        return files;
    }

    /**
     * Recursively lists the files in the destination directory and adds their information to the files list.
     *
     * @param directory The directory to be processed.
     * @param files The list of files to be populated with file information.
     * @throws IOException If an I/O error occurs during file reading.
     */
    private void listDestinationDirectoryRecursive(File directory, List<String[]> files) throws IOException {
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles != null) {
            for (File file : directoryFiles) {
                String relativePath = destinationDirectory.toURI().relativize(file.toURI()).getPath();
                long timestamp = file.lastModified();

                if (file.isFile()) {
                    byte[] content = Files.readAllBytes(file.toPath());
                    String contentString = new String(content, StandardCharsets.UTF_8);
                    files.add(new String[]{relativePath, contentString, String.valueOf(timestamp)});
                } else if (file.isDirectory()) {
                    files.add(new String[]{relativePath, "", String.valueOf(timestamp)});
                    listDestinationDirectoryRecursive(file, files);
                }
            }
        }
    }

    /**
     * Handles the remote files for synchronization.
     *
     * @param remoteFiles A list of remote files to be processed for synchronization.
     * @param updateSource A boolean flag indicating if the source directory should be updated.
     * @throws IOException If an I/O error occurs during file writing.
     */
    public void handleRemoteFiles(List<String[]> remoteFiles, boolean updateSource) throws IOException {
        Set<String> updatedFilePaths = new HashSet<>();
        for (String[] remoteFile : remoteFiles) {
            String filePath = remoteFile[0];
            String fileContent = remoteFile[1];
            long remoteTimestamp = Long.parseLong(remoteFile[2]);

            File localFile = new File(destinationDirectory, filePath);

            if (!localFile.exists() || localFile.lastModified() < remoteTimestamp) {
                if (isDirectory(filePath, remoteFiles)) {
                    localFile.mkdirs();
                } else {
                    localFile.getParentFile().mkdirs();
                    try (FileWriter fileWriter = new FileWriter(localFile)) {
                        fileWriter.write(fileContent);
                    }
                }
                localFile.setLastModified(remoteTimestamp);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            updatedFilePaths.add(filePath);
        }
        removeFiles(getDeletedFiles(listSourceDirectory(), remoteFiles));
    }


    /**
     * Handles the deleted files for synchronization.
     *
     * @param deletedFilePaths A set of file paths that need to be deleted.
     * @throws IOException If an I/O error occurs during file deletion.
     */
    public void handleDeletedFiles(Set<String> deletedFilePaths) throws IOException {
        for (String relativePath : deletedFilePaths) {
            File deletedFile = new File(destinationDirectory, relativePath);
            if (deletedFile.exists()) {
                Files.delete(deletedFile.toPath());
            }
        }
    }

    /**
     * Removes obsolete files from the destination directory.
     *
     * @param obsoleteFilePaths A set of relative file paths that are considered obsolete.
     * @throws IOException If an I/O error occurs while deleting files.
     */
    @Override
    public void removeFiles(Set<String> obsoleteFilePaths) throws IOException {
        for (String relativePath : obsoleteFilePaths) {
            File obsoleteFile = new File(destinationDirectory, relativePath);
            if (obsoleteFile.exists()) {
                Files.delete(obsoleteFile.toPath());
            }
        }
    }

    /**
     * Identifies deleted files by comparing local and remote files.
     *
     * @param localFiles A list of local files in the format String[]{relativePath, content, timestamp}.
     * @param remoteFiles A list of remote files in the same format as localFiles.
     * @return A set of relative file paths that are present in the remoteFiles but not in the localFiles.
     */
    private Set<String> getDeletedFiles(List<String[]> localFiles, List<String[]> remoteFiles) {
        Set<String> localFilePaths = localFiles.stream().map(file -> file[0]).collect(Collectors.toSet());
        Set<String> remoteFilePaths = remoteFiles.stream().map(file -> file[0]).collect(Collectors.toSet());
        remoteFilePaths.removeAll(localFilePaths);
        return remoteFilePaths;
    }

    /**
     * Deletes obsolete files from the given directory, based on the updated file paths.
     *
     * @param directory The directory where obsolete files should be deleted.
     * @param updatedFilePaths A set of relative file paths that have been updated.
     * @throws IOException If an I/O error occurs while deleting files.
     */
    private void deleteObsoleteFiles(File directory, Set<String> updatedFilePaths) throws IOException {
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles != null) {
            for (File file : directoryFiles) {
                String relativePath = sourceDirectory.toURI().relativize(file.toURI()).getPath();
                if (!updatedFilePaths.contains(relativePath)) {
                    if (file.isDirectory()) {
                        deleteObsoleteFiles(file, updatedFilePaths);
                        if (file.listFiles().length == 0) {
                            Files.delete(file.toPath());
                        }
                    }
                    else {
                        Files.delete(file.toPath());
                    }
                }
            }
        }
    }

    /**
     * Updates the source directory with the new files provided.
     *
     * @param newFiles A list of new files in the format String[]{relativePath, content, timestamp}.
     * @throws IOException If an I/O error occurs while updating the source directory.
     */
    public void updateSourceDirectory(List<String[]> newFiles) throws IOException {
        Set<String> updatedFilePaths = new HashSet<>();
        for (String[] newFile : newFiles) {
            String filePath = newFile[0];
            String fileContent = newFile[1];
            long remoteTimestamp = Long.parseLong(newFile[2]);

            File localFile = new File(destinationDirectory, filePath);

            if (!localFile.exists() || localFile.lastModified() < remoteTimestamp) {
                if (isDirectory(filePath, newFiles)) {
                    localFile.mkdirs();
                } else {
                    localFile.getParentFile().mkdirs();
                    try (FileWriter fileWriter = new FileWriter(localFile)) {
                        fileWriter.write(fileContent);
                    }
                }
                localFile.setLastModified(remoteTimestamp);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            updatedFilePaths.add(filePath);
        }
        deleteObsoleteFiles(sourceDirectory, updatedFilePaths);
    }

    /**
     * Checks if the remote server is ready for synchronization.
     *
     * @return true if the remote server is ready, false otherwise.
     * @throws RemoteException If a remote communication error occurs.
     */
    @Override
    public boolean isReady() throws RemoteException {
        return true;
    }

    /**
     * Sets the local port number for RMI communication.
     *
     * @param localPort The local port number to be set.
     */
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
        if (localPort == 1099)
            remotePort = 1100;
        else remotePort = 1099;
    }

    /**
     * Sets the instance ID to identify the current instance of the application.
     *
     * @param instanceId The instance ID to be set.
     */
    public void setinstanceId(int instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Sets the source path for the source directory.
     *
     * @param sourcePath The source path to be set.
     */
    public void setSourcePath (String sourcePath) {
        this.sourcePath = sourcePath;
        this.sourceDirectory = new File(sourcePath);
    }

    /**
     * Sets the remote IP address for RMI communication.
     *
     * @param remoteIP The remote IP address to be set.
     */
    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    /**
     * Sets the remote turn for the synchronization process.
     *
     * @param turn The turn number to be set.
     * @throws RemoteException If a remote communication error occurs.
     */
    @Override
    public void setRemoteTurn(int turn) throws RemoteException {
        this.turn = turn;
    }

    /**
     * Runs the synchronization process between the local and remote directories.
     */
    @Override
    public void run() {
        try {
            waitForRemote(remoteIP);

            while (true) {
                if (turn == instanceId) {
                    MainFrame.message("It's your turn !");
                    Thread.sleep(34000);
                    MainFrame.message("It's almost over !");
                    Thread.sleep(3000);
                    List<String[]> localFiles = listSourceDirectory();
                    DirectorySynchronizer otherSynchronizer = (DirectorySynchronizer) Naming.lookup("rmi://"+remoteIP+":"+remotePort+"/interfaces.DirectorySynchronizer");
                    List<String[]> remoteFiles = otherSynchronizer.listDestinationDirectory();

                    if (!localFiles.equals(remoteFiles)) {
                        handleRemoteFiles(remoteFiles, updateSource);
                        otherSynchronizer.updateSourceDirectory(localFiles);
                        removeFiles(getDeletedFiles(localFiles, remoteFiles));
                        otherSynchronizer.handleDeletedFiles(getDeletedFiles(localFiles, remoteFiles));
                    }

                    Thread.sleep(1000);

                    RequestHandler otherHandler = (RequestHandler) Naming.lookup("rmi://"+remoteIP+":"+remotePort+"/interfaces.RequestHandler");
                    otherHandler.setRemoteTurn((turn == 1) ? 2 : 1);
                    turn = (turn == 1) ? 2 : 1;
                } else {
                    MainFrame.message("Waiting for your turn...");
                    Thread.sleep(syncInterval);
                }
            }
        } catch (InterruptedException | NotBoundException | IOException e) {
            System.err.println("The synchronisation thread has been interrupted : " + e.getMessage());
            MainFrame.message("The synchronisation thread has been interrupted");
        }
    }
}