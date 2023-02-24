import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class DestinationDirectory {
    public DestinationDirectory(List<File> files, String remotePath) throws IOException {

        for (File file : files) {
            File destFile = new File(remotePath + File.separator + file.getName());
            if (!destFile.exists()) {
                destFile.createNewFile();
                System.out.println("Fichier de destination créé : " + destFile.getName());
            }

        }
    }
}
