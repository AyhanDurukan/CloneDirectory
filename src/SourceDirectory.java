import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SourceDirectory {
    private String path;
    private File directory;

    public SourceDirectory (String path) {
        this.path = path;
        this.directory = new File(path);
    }

    public void listDirectory () {
        List<File> files = new ArrayList<>();
        if (directory.isDirectory()) {
            File[] directoryFiles = directory.listFiles();
            if (directoryFiles != null) {
                for (File file : directoryFiles) {
                    if (file.isFile()) {
                        files.add(file);
                        System.out.println(file.getName());
                    }
                }
            }
        }

    }

}