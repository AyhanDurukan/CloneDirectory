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
import java.rmi.Remote;


public class BidirectionalSync extends UnicastRemoteObject implements DirectorySynchronizer, RequestHandler, Runnable {
    private String sourcePath;
    private String destinationPath;
    private File sourceDirectory;
    private File destinationDirectory;
    private long syncInterval;
    private boolean syncAllowed;
    private boolean syncRequested;


    public BidirectionalSync(String sourcePath, String destinationPath, long syncInterval) throws RemoteException {
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
        this.sourceDirectory = new File(sourcePath);
        this.destinationDirectory = new File(destinationPath);
        this.syncInterval = syncInterval;
        this.syncRequested = false;
        this.syncAllowed = false; // Ajoutez cette ligne pour initialiser syncAllowed
        System.out.println("\nRépertoire source : " + sourceDirectory + "\n");
        System.out.println("\nRépertoire destination : " + destinationDirectory + "\n");
    }

    @Override
    public void updateSourceDirectory(List<String[]> files) throws RemoteException, IOException {
        // Créez un ensemble contenant les chemins relatifs des fichiers à jour
        Set<String> updatedFilePaths = new HashSet<>();
        System.out.println("Mise à jour des fichiers...");
        for (String[] file : files) {
            String filePath = file[0];
            String fileContent = file[1];
            long remoteTimestamp = Long.parseLong(file[2]);

            File localFile = new File(sourceDirectory, filePath); // Changez "destinationDirectory" en "sourceDirectory"
            System.out.println("Mise à jour du fichier : " + localFile.getAbsolutePath());

            localFile.getParentFile().mkdirs();
            try (FileWriter fileWriter = new FileWriter(localFile)) {
                fileWriter.write(fileContent);
            }
            localFile.setLastModified(remoteTimestamp);
            updatedFilePaths.add(filePath);
            System.out.println("Fichier mis à jour : " + localFile.getAbsolutePath() + " | Créé avec succès : " + localFile.exists()); // Ajoutez cette ligne

        }
        System.out.println("Nombre de fichiers mis à jour : " + updatedFilePaths.size());
        deleteObsoleteFiles(sourceDirectory, updatedFilePaths, sourceDirectory);
    }



    @Override
    public boolean requestSyncPermission() throws RemoteException {
        return syncAllowed;
    }

    public void setSyncAllowed(boolean syncAllowed) {
        this.syncAllowed = syncAllowed;
        this.syncRequested = true;
    }
    private void deleteObsoleteFiles(File directory, Set<String> updatedFilePaths, File referenceDirectory) throws IOException {
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles != null) {
            for (File file : directoryFiles) {
                String relativePath = referenceDirectory.toURI().relativize(file.toURI()).getPath();
                if (!updatedFilePaths.contains(relativePath)) {
                    if (file.isFile()) {
                        File otherDirectoryFile = new File(referenceDirectory.equals(destinationDirectory) ? sourceDirectory : destinationDirectory, relativePath);
                        if (otherDirectoryFile.exists()) {
                            long localTimestamp = file.lastModified();
                            long remoteTimestamp = otherDirectoryFile.lastModified();
                            if (localTimestamp < remoteTimestamp) {
                                Files.delete(file.toPath());
                            }
                        } else {
                            Files.delete(file.toPath());
                        }
                    } else if (file.isDirectory()) {
                        deleteObsoleteFiles(file, updatedFilePaths, referenceDirectory);
                        if (file.listFiles().length == 0) {
                            Files.delete(file.toPath());
                        }
                    }
                }
            }
        }
    }


    @Override
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

        // Récupérer les fichiers distants
        List<String[]> remoteFiles = other.listDestinationDirectory(); // Modifiez cette ligne
        System.out.println("Nombre de fichiers distants : " + remoteFiles.size());

        // Traiter les fichiers distants
        System.out.println("Traitement des fichiers distants...");
        handleRemoteFiles(remoteFiles, updateSource);
    }


    public void handleRemoteFiles(List<String[]> remoteFiles, boolean updateSource) throws IOException {
        for (String[] remoteFile : remoteFiles) {
            String filePath = remoteFile[0];
            String fileContent = remoteFile[1];
            long remoteTimestamp = Long.parseLong(remoteFile[2]);

            File localFile = new File(updateSource ? sourceDirectory : destinationDirectory, filePath);
            System.out.println("Traitement du fichier distant : " + localFile.getAbsolutePath());

            if (!localFile.exists() || localFile.lastModified() < remoteTimestamp) {
                localFile.getParentFile().mkdirs();
                try (FileWriter fileWriter = new FileWriter(localFile)) {
                    fileWriter.write(fileContent);
                }
                localFile.setLastModified(remoteTimestamp);
                System.out.println("Fichier créé/mis à jour : " + localFile.getAbsolutePath() + " | Créé avec succès : " + localFile.exists());
            }
        }
    }

    @Override
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

    public void run() {
        try {
            waitForRemote("127.0.0.1");

            while (true) {
                if (syncAllowed) {
                    try {
                        this.synchronizeWith("127.0.0.1", true);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la synchronisation : " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                Thread.sleep(syncInterval);
            }
        } catch (InterruptedException | RemoteException | MalformedURLException | NotBoundException e) {
            System.err.println("Le thread de synchronisation a été interrompu : " + e.getMessage());
            e.printStackTrace();
        }
    }





    public static void main(String[] args) {
        try {
            // Démarrer le registre RMI sur le port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            BidirectionalSync localDirectory = new BidirectionalSync("C:\\Users\\ayhan\\OneDrive\\Documents\\1er année ENSISA\\Semestre 2\\AOO Java\\CloneDirectory\\testD", "C:\\Users\\ayhan\\OneDrive\\Documents\\1er année ENSISA\\Semestre 2\\AOO Java\\Learn\\testDB", 20000); // 60000ms = 1 minute
            Naming.rebind("rmi://localhost:1099/DirectorySynchronizer", localDirectory);
            Naming.rebind("rmi://localhost:1099/RequestHandler", localDirectory);
            System.out.println("Serveur RMI lancé");

            // Attendez que l'autre PC soit prêt
            // Attendez que l'autre PC soit prêt
            localDirectory.waitForRemote("127.0.0.1");
            localDirectory.setSyncAllowed(true);

// Démarrer le thread de synchronisation
            Thread syncThread = new Thread(localDirectory);
            syncThread.start();

// Demander la permission de synchronisation
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