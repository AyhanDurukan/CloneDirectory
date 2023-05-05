import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.stream.Collectors;
public class BidirectionalSync extends UnicastRemoteObject implements DirectorySynchronizer, RequestHandler, Runnable {
    private String sourcePath;
    private String destinationPath;
    private File sourceDirectory;
    private File destinationDirectory;
    private long syncInterval;
    private boolean syncAllowed;
    private boolean syncRequested;
    private boolean updateSource;
    private boolean isMyTurn;
    private final int instanceId;
    private int turn = 1;


    public BidirectionalSync(String sourcePath, String destinationPath, long syncInterval, boolean updateSource, int instanceId) throws RemoteException {
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
        this.sourceDirectory = new File(sourcePath);
        this.destinationDirectory = new File(destinationPath);
        this.syncInterval = syncInterval;
        this.syncRequested = false;
        this.syncAllowed = false;
        this.instanceId = instanceId;
        this.updateSource = updateSource;
        System.out.println("\nRépertoire source : " + sourceDirectory + "\n");
        System.out.println("\nRépertoire destination : " + destinationDirectory + "\n");
    }
    @Override
    public boolean requestSyncPermission() throws RemoteException {
        return syncAllowed;
    }
    public void setSyncAllowed(boolean syncAllowed) {
        this.syncAllowed = syncAllowed;
        this.syncRequested = true;
    }
    public List<String[]> listSourceDirectory() throws IOException {
        List<String[]> files = new ArrayList<>();
        listSourceDirectoryRecursive(sourceDirectory, files);
        return files;
    }
    @Override
    public boolean isReady() throws RemoteException {
        return true;
    }
    public void waitForRemote(String remoteIp) throws RemoteException, MalformedURLException, NotBoundException {
        RequestHandler other = (RequestHandler) Naming.lookup("rmi://" + remoteIp + ":1099/RequestHandler");

        try {
            System.out.println("Attente de 10 secondes avant de vérifier si l'autre PC est prêt...");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.err.println("Erreur lors de l'attente initiale : " + e.getMessage());
            e.printStackTrace();
        }

        while (!other.isReady()) {
            try {
                System.out.println("En attente de la connexion de l'autre PC...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Erreur lors de l'attente de la connexion de l'autre PC : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public void updateDestinationDirectory(List<String[]> newFiles) throws IOException {
        Set<String> updatedFilePaths = new HashSet<>();
        for (String[] newFile : newFiles) {
            String filePath = newFile[0];
            String fileContent = newFile[1];
            long remoteTimestamp = Long.parseLong(newFile[2]);

            File localFile = new File(destinationDirectory, filePath);

            if (!localFile.exists() || localFile.lastModified() < remoteTimestamp) {
                localFile.getParentFile().mkdirs();
                try (FileWriter fileWriter = new FileWriter(localFile)) {
                    fileWriter.write(fileContent);
                }
                localFile.setLastModified(remoteTimestamp);
                System.out.println("Fichier créé/mis à jour : " + localFile.getAbsolutePath() + " | Créé avec succès : " + localFile.exists());

                // Ajoutez cette ligne pour introduire une pause après la création ou la mise à jour d'un fichier.
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }updatedFilePaths.add(filePath);
        }deleteObsoleteFiles(destinationDirectory, updatedFilePaths);
    }
    @Override
    public List<String[]> listDestinationDirectory() throws IOException {
        List<String[]> files = new ArrayList<>();
        listDestinationDirectoryRecursive(destinationDirectory, files);
        return files;
    }
    private void listDestinationDirectoryRecursive(File directory, List<String[]> files) throws IOException {
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles != null) {
            for (File file : directoryFiles) {
                if (file.isFile()) {
                    String relativePath = destinationDirectory.toURI().relativize(file.toURI()).getPath();
                    byte[] content = Files.readAllBytes(file.toPath());
                    String contentString = new String(content, StandardCharsets.UTF_8);
                    long timestamp = file.lastModified();
                    System.out.println("Fichier destination : " + relativePath);
                    files.add(new String[]{relativePath, contentString, String.valueOf(timestamp)});
                } else if (file.isDirectory()) {
                    listDestinationDirectoryRecursive(file, files);
                }
            }
        }
    }
    public void synchronizeWith(String remoteIp, boolean updateSource) throws Exception {
        DirectorySynchronizer other = (DirectorySynchronizer) Naming.lookup("rmi://" + remoteIp + ":1099/DirectorySynchronizer");

        List<String[]> remoteFiles = other.listDestinationDirectory();
        System.out.println("Nombre de fichiers distants : " + remoteFiles.size());

        System.out.println("Traitement des fichiers distants...");
        updateDestinationDirectory(remoteFiles);
        removeFiles(getDeletedFiles(listSourceDirectory(), remoteFiles));
    }
    public void handleRemoteFiles(List<String[]> remoteFiles, boolean updateSource) throws IOException {
        Set<String> updatedFilePaths = new HashSet<>();
        for (String[] remoteFile : remoteFiles) {
            String filePath = remoteFile[0];
            String fileContent = remoteFile[1];
            long remoteTimestamp = Long.parseLong(remoteFile[2]);

            File localFile = new File(destinationDirectory, filePath);

            if (!localFile.exists() || localFile.lastModified() < remoteTimestamp) {
                localFile.getParentFile().mkdirs();
                try (FileWriter fileWriter = new FileWriter(localFile)) {
                    fileWriter.write(fileContent);
                }
                localFile.setLastModified(remoteTimestamp);
                System.out.println("Fichier créé/mis à jour : " + localFile.getAbsolutePath() + " | Créé avec succès : " + localFile.exists());

                // Ajoutez cette ligne pour introduire une pause après la création ou la mise à jour d'un fichier.
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            updatedFilePaths.add(filePath);
        }
        removeFiles(getDeletedFiles(listSourceDirectory(), remoteFiles));
    }@Override
    public boolean acceptSyncRequest() throws RemoteException {
        System.out.println("Voulez-vous accepter la demande de synchronisation ? (y/n)");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine();
        boolean accepted = "y".equalsIgnoreCase(answer);
        setSyncAllowed(accepted);
        return accepted;
    }
    private void listSourceDirectoryRecursive(File directory, List<String[]> files) throws IOException {
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles != null) {
            for (File file : directoryFiles) {
                if (file.isFile()) {
                    String relativePath = sourceDirectory.toURI().relativize(file.toURI()).getPath();
                    byte[] content = Files.readAllBytes(file.toPath());
                    String contentString = new String(content, StandardCharsets.UTF_8);
                    long timestamp = file.lastModified();
                    System.out.println("Fichier source : " + relativePath);
                    files.add(new String[]{relativePath, contentString, String.valueOf(timestamp)});
                } else if (file.isDirectory()) {
                    listSourceDirectoryRecursive(file, files);
                }
            }
        }
    }

    @Override
    public void setRemoteTurn(int turn) throws RemoteException {
        this.turn = turn;
    }

    @Override
    public void run() {
        try {
            waitForRemote("127.0.0.1");

            while (true) {

                if (turn == instanceId) {
                    System.out.println("\nA votre tour !\n");
                    Thread.sleep(34000);
                    System.out.println("\nA C'est bientot fini ! !\n");
                    Thread.sleep(3000);
                    List<String[]> localFiles = listSourceDirectory();
                    DirectorySynchronizer otherSynchronizer = (DirectorySynchronizer) Naming.lookup("rmi://127.0.0.1:1100/DirectorySynchronizer");
                    List<String[]> remoteFiles = otherSynchronizer.listDestinationDirectory();

                    if (!localFiles.equals(remoteFiles)) {
                        System.out.println("Changements détectés. Synchronisation en cours...");
                        handleRemoteFiles(remoteFiles, updateSource);
                        otherSynchronizer.updateSourceDirectory(localFiles);
                        removeFiles(getDeletedFiles(localFiles, remoteFiles));
                        otherSynchronizer.handleDeletedFiles(getDeletedFiles(localFiles, remoteFiles));
                    }

                    Thread.sleep(1000);

                    RequestHandler otherHandler = (RequestHandler) Naming.lookup("rmi://127.0.0.1:1100/RequestHandler");
                    otherHandler.setRemoteTurn((turn == 1) ? 2 : 1);
                    turn = (turn == 1) ? 2 : 1;
                } else {
                    System.out.println("En attente de votre tour...");
                    Thread.sleep(syncInterval);
                }
            }
        } catch (InterruptedException | NotBoundException | IOException e) {
            System.err.println("Le thread de synchronisation a été interrompu : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void test () {
        System.out.println("Samet");
    }
    private Set<String> getDeletedFiles(List<String[]> localFiles, List<String[]> remoteFiles) {
        Set<String> localFilePaths = localFiles.stream().map(file -> file[0]).collect(Collectors.toSet());
        Set<String> remoteFilePaths = remoteFiles.stream().map(file -> file[0]).collect(Collectors.toSet());
        remoteFilePaths.removeAll(localFilePaths);
        return remoteFilePaths;
    }
    @Override
    public void removeFiles(Set<String> obsoleteFilePaths) throws IOException {
        for (String relativePath : obsoleteFilePaths) {
            File obsoleteFile = new File(destinationDirectory, relativePath); // Modifiez cette ligne
            if (obsoleteFile.exists()) {
                Files.delete(obsoleteFile.toPath());
                System.out.println("Fichier obsolète supprimé : " + obsoleteFile.getAbsolutePath());
            }
        }
    }
    @Override
    public void handleDeletedFiles(Set<String> deletedFilePaths) throws IOException {
        for (String relativePath : deletedFilePaths) {
            File deletedFile = new File(destinationDirectory, relativePath); // Modifiez cette ligne
            if (deletedFile.exists()) {
                Files.delete(deletedFile.toPath());
                System.out.println("Fichier supprimé : " + deletedFile.getAbsolutePath());
            }
        }
    }
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
                            System.out.println("Dossier obsolète supprimé : " + file.getAbsolutePath());
                        }
                    }
                    else {
                        Files.delete(file.toPath());
                    }
                }
            }
        }
    }
    @Override
    public void setRemoteIsMyTurn(boolean isMyTurn) throws RemoteException {
        this.isMyTurn = isMyTurn;
    }
    public void updateSourceDirectory(List<String[]> newFiles) throws IOException {
        Set<String> updatedFilePaths = new HashSet<>();
        for (String[] newFile : newFiles) {
            String filePath = newFile[0];
            String fileContent = newFile[1];
            long remoteTimestamp = Long.parseLong(newFile[2]);

            File localFile = new File(destinationDirectory, filePath);

            if (!localFile.exists() || localFile.lastModified() < remoteTimestamp) {
                localFile.getParentFile().mkdirs();
                try (FileWriter fileWriter = new FileWriter(localFile)) {
                    fileWriter.write(fileContent);
                }
                localFile.setLastModified(remoteTimestamp);
                System.out.println("Fichier créé/mis à jour : " + localFile.getAbsolutePath() + " | Créé avec succès : " + localFile.exists());

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }updatedFilePaths.add(filePath);
        }deleteObsoleteFiles(sourceDirectory, updatedFilePaths);
    }
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);

            BidirectionalSync localDirectory = new BidirectionalSync("C:\\Users\\ayhan\\OneDrive\\Documents\\1er année ENSISA\\Semestre 2\\AOO Java\\CloneDirectory\\testD", "C:\\Users\\ayhan\\OneDrive\\Documents\\1er année ENSISA\\Semestre 2\\AOO Java\\Learn\\testDB", 40000, true, 1); // 60000ms = 1 minute
            Naming.rebind("rmi://localhost:1099/DirectorySynchronizer", localDirectory);
            Naming.rebind("rmi://localhost:1099/RequestHandler", localDirectory);
            System.out.println("Serveur RMI lancé");

            localDirectory.waitForRemote("127.0.0.1");
            localDirectory.setSyncAllowed(true);

            Thread syncThread = new Thread(localDirectory);
            syncThread.start();

            try {
                RequestHandler other = (RequestHandler) Naming.lookup("rmi://127.0.0.1:1099/RequestHandler");
                boolean syncAllowed = other.acceptSyncRequest(); // Déclarez 'syncAllowed' ici
                if (!syncAllowed) {
                    System.out.println("La synchronisation a été refusée par l'autre PC.");
                    System.exit(0);
                }
            } catch (MalformedURLException | RemoteException | NotBoundException e) {
                System.err.println("Erreur lors de la demande de permission de synchronisation : " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du lancement du serveur RMI : " + e.getMessage());
            e.printStackTrace();
        }
    }
}