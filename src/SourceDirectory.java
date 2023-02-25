import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SourceDirectory {
    private String path;
    private File directory;
    private String remotePath;
    private File remotedirectory;
    public SourceDirectory (String path, String remotePath) {
        this.path = path;
        this.directory = new File(path);
        this.remotePath = remotePath;
        this.remotedirectory = new File(remotePath);
    }

    public void listDirectory (File directory) throws IOException {
        List<String> files = new ArrayList<>();
        if (directory.isDirectory()) {
            File[] directoryFiles = directory.listFiles();
            if (directoryFiles != null) {
                for (File file : directoryFiles) {
                    if (file.isFile()) {
                        files.add(file.getName());
                        if (file.getParent().equals(path)) {
                            System.out.println(file.getName());
                            files.add(file.getName());
                        }else {
                            File modifiedPaths = new File(file.getParent());
                            System.out.println(modifiedPaths.getName() + "/" + file.getName());
                            files.add(modifiedPaths.getName() + "/" + file.getName());
                        }
                    } else if (file.isDirectory())
                        listDirectory(file);
                }
            }
        }
        //copy(files);
    }

    private void copy (List<File> files) throws IOException {
        DestinationDirectory remoteDir = new DestinationDirectory(files,this.remotePath);
    }

    public File getDirectory() {
        return directory;
    }
}