import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SourceDirectory extends UnicastRemoteObject implements DirectorySynchronizer {
    private String sourcePath;
    private File sourceDirectory;

    public SourceDirectory(String sourcePath) throws RemoteException {
        this.sourcePath = sourcePath;
        this.sourceDirectory = new File(sourcePath);
        System.out.println("\nRepertoire source : " + sourceDirectory + "\n");
    }

    public List<String[]> listSourceDirectory() throws IOException {
        List<String[]> files = new ArrayList<>();
        listSourceDirectoryRecursive(sourceDirectory, files);
        return files;
    }

    @Override
    public void updateDestinationDirectory(List<String[]> files) throws RemoteException {

    }

    private void listSourceDirectoryRecursive(File directory, List<String[]> files) throws IOException {
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles != null) {
            for (File file : directoryFiles) {
                if (file.isFile()) {
                    List<String> temp = new ArrayList<>();
                    StringTokenizer modifiedPath = new StringTokenizer(file.getPath(), "\\");
                    boolean foundTestD = false;
                    while (modifiedPath.hasMoreTokens()) {
                        String val = modifiedPath.nextToken();
                        if (val.equals(sourceDirectory.getName())) {
                            foundTestD = true;
                            temp.clear();
                        } else if (foundTestD) {
                            temp.add(val);
                        }
                    }
                    if (!temp.isEmpty()) {
                        String fileName = String.join("\\", temp);
                        byte[] content = Files.readAllBytes(Paths.get(file.getPath()));
                        String contentString = new String(content, StandardCharsets.UTF_8);
                        files.add(new String[]{fileName, contentString});
                    }
                } else if (file.isDirectory()) {
                    listSourceDirectoryRecursive(file, files);
                }
            }
        }
    }

    public void printSourceFile() throws IOException {
        List<String[]> files = listSourceDirectory();
        System.out.println("Voici le contenu du repertoire " + sourceDirectory.getName() + ":");
        for (String[] file : files) {
            System.out.println(file[0]);
            System.out.println(file[1]);
        }
    }

    public static void main(String[] args) {
        try {
            SourceDirectory sourceDirectory = new SourceDirectory("C:\\Users\\ayhan\\OneDrive\\Documents\\1er année ENSISA\\Semestre 2\\AOO Java\\CloneDirectory\\testD");
            Naming.rebind("rmi://localhost:1099/DirectorySynchronizer", sourceDirectory);
            System.out.println("Serveur RMI lancé");
        } catch (Exception e) {
            System.err.println("Erreur lors du lancement du serveur RMI : " + e.getMessage());
            e.printStackTrace();
        }
    }

}