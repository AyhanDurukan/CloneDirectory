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
            StringTokenizer modifiedPath = new StringTokenizer(filePath, "\\");
            int count = modifiedPath.countTokens();
            System.out.println(count);

            String currentPath = destinationPath;
            while (modifiedPath.hasMoreTokens()) {
                String token = modifiedPath.nextToken();
                count--;

                if (count == 0) {
                    String fileName = token;
                    filePath = currentPath + "\\" + fileName;
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
                    currentPath += "\\" + token;
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

    public void receive() throws Exception {
        DirectorySynchronizer synchronizer = (DirectorySynchronizer) Naming.lookup("rmi://localhost:1099/DirectorySynchronizer");
        List<String[]> files = synchronizer.listSourceDirectory();
        System.out.println("La liste des fichiers recus :\n" + files + "\n");
        // Mise à jour de la variable d'instance sFiles
        sFiles = files;
    }

    public static void main(String[] args) throws Exception {
        DestinationDirectory destinationDirectory = new DestinationDirectory("C:\\Users\\ayhan\\OneDrive\\Documents\\1er année ENSISA\\Semestre 2\\AOO Java\\CloneDirectory\\testDD");
        destinationDirectory.receive();
        destinationDirectory.cloneSource();
    }
}
