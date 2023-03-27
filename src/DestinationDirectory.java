import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.util.List;
import java.util.StringTokenizer;

public class DestinationDirectory {
    private String destinationPath;
    private File destinationDirectory;
    private List<String[]> sFiles; // Ajoutez cette ligne pour déclarer la variable d'instance sFiles


    public DestinationDirectory(String destinationPath) {
        this.destinationPath = destinationPath;
        this.destinationDirectory = new File(destinationPath);
        System.out.println("\nRepertoire destination : " + destinationDirectory + "\n");
    }

    public void cloneSource() throws IOException {
        for (String[] file : sFiles) {
            String filePath = file[0];
            String fileContent = file[1];

            System.out.println(filePath);
            StringTokenizer modifiedPath = new StringTokenizer(filePath, File.separator);
            int count = modifiedPath.countTokens();
            System.out.println(count);

            String currentPath = destinationPath;
            while (modifiedPath.hasMoreTokens()) {
                String token = modifiedPath.nextToken();
                count--;

                if (count == 0) {
                    String fileName = token;
                    filePath = currentPath + File.separator + fileName;
                    File newFile = new File(filePath);

                    if (newFile.createNewFile()) {
                        System.out.println("Fichier " + fileName + " créé");
                    } else {
                        System.out.println("Impossible de créer le fichier " + fileName);
                        continue;
                    }

                    FileWriter fileWriter = new FileWriter(newFile);
                    fileWriter.write(fileContent);
                    fileWriter.close();

                } else {
                    currentPath += File.separator + token;
                    File currentDirectory = new File(currentPath);
                    if (!currentDirectory.exists()) {
                        currentDirectory.mkdir();
                        System.out.println("Dossier " + token + " créé");
                    }
                }
            }
        }
    }

    public void printReceivedFile() throws IOException {
        System.out.println("Voici les fichiers recus :");
        for (String[] file : sFiles) {
            System.out.println(file[0]);
        }
    }

    public void checkAndUpdate() throws Exception {
        DirectorySynchronizer synchronizer = (DirectorySynchronizer) Naming.lookup("rmi://localhost:1099/DirectorySynchronizer");
        List<String[]> newFiles = synchronizer.listSourceDirectory();
        sFiles = newFiles;
        cloneSource();
        deleteFilesNotInSource();
        updateModifiedFiles();
    }

    public void receive() throws Exception {
        DirectorySynchronizer synchronizer = (DirectorySynchronizer) Naming.lookup("rmi://localhost:1099/DirectorySynchronizer");
        List<String[]> files = synchronizer.listSourceDirectory();
        System.out.println("La liste des fichiers recus :\n" + files + "\n");
        sFiles = files;
    }

    public void deleteFilesNotInSource() {
        deleteFilesNotInSourceRecursive(destinationDirectory, sFiles);
    }

    private void deleteFilesNotInSourceRecursive(File directory, List<String[]> sourceFiles) {
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles != null) {
            for (File file : directoryFiles) {
                if (file.isFile()) {
                    boolean foundInSource = false;
                    for (String[] sourceFile : sourceFiles) {
                        String sourceFilePath = destinationPath + File.separator + sourceFile[0];
                        if (sourceFilePath.equals(file.getPath())) {
                            foundInSource = true;
                            break;
                        }
                    }

                    if (!foundInSource) {
                        file.delete();
                        System.out.println("Fichier supprimé: " + file.getPath());
                    }
                } else if (file.isDirectory()) {
                    deleteFilesNotInSourceRecursive(file, sourceFiles);
                }
            }
        }
    }

    public void updateModifiedFiles() throws IOException {
        updateModifiedFilesRecursive(destinationDirectory, sFiles);
    }

    private void updateModifiedFilesRecursive(File directory, List<String[]> sourceFiles) throws IOException {
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles != null) {
            for (File file : directoryFiles) {
                if (file.isFile()) {
                    for (String[] sourceFile : sourceFiles) {
                        String sourceFilePath = destinationPath + File.separator + sourceFile[0];
                        if (sourceFilePath.equals(file.getPath())) {
                            long sourceLastModified = Long.parseLong(sourceFile[2]);
                            if (file.lastModified() < sourceLastModified) {
                                FileWriter fileWriter = new FileWriter(file);
                                fileWriter.write(sourceFile[1]);
                                fileWriter.close();
                                file.setLastModified(sourceLastModified);
                                System.out.println("Fichier mis à jour : " + file.getPath());
                            }
                            break;
                        }
                    }
                } else if (file.isDirectory()) {
                    updateModifiedFilesRecursive(file, sourceFiles);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        DestinationDirectory destinationDirectory = new DestinationDirectory("C:\\Users\\ayhan\\OneDrive\\Documents\\1er année ENSISA\\Semestre 2\\AOO Java\\CloneDirectory\\testDD");
        destinationDirectory.receive();
        destinationDirectory.cloneSource();

        Thread updateThread = new Thread(() -> {
            while (true) {
                try {
                    // Attendre 10 secondes avant de vérifier à nouveau
                    Thread.sleep(10 * 1000);
                    destinationDirectory.checkAndUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        updateThread.start();
    }
}