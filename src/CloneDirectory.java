import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CloneDirectory {
    private String sourcePath;
    private File sourceDirectory;
    private String destinationPath;
    private File destinationDirectory;

    public CloneDirectory (String sourcePath, String destinationPath) {
        this.sourcePath = sourcePath;
        this.sourceDirectory = new File(sourcePath);
        this.destinationPath = destinationPath;
        this.destinationDirectory = new File(destinationPath);
        System.out.println("Repertoire source : " + sourceDirectory);
        System.out.println("Repertoire destination : " + destinationDirectory);
    }

    public List<String> listSourceDirectory(File sourceDirectory) throws IOException {
        List<String> files = new ArrayList<>();
        listSourceDirectoryRecursive(sourceDirectory, files);
        return files;
    }

    private void listSourceDirectoryRecursive(File directory, List<String> files) throws IOException {
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
                        files.add("testD\\" + String.join("\\", temp));
                    }
                } else if (file.isDirectory()) {
                    listSourceDirectoryRecursive(file, files);
                }
            }
        }
    }

    public void printSourceFile() throws IOException {
        List<String> files = listSourceDirectory(getSourceDirectory());
        System.out.println("Voici le contenu du repertoire " + sourceDirectory.getName() + ":");
        for (String file : files) {
            System.out.println(file);
        }
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }
}