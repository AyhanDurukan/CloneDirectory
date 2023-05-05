import java.util.HashSet;
import java.util.Set;

public class SyncMetadata {
    private Set<String> deletedFiles;

    public SyncMetadata() {
        deletedFiles = new HashSet<>();
    }

    // Ajoute un fichier supprimé aux métadonnées
    public void addDeletedFile(String filePath) {
        deletedFiles.add(filePath);
    }

    // Vérifie si un fichier a été supprimé
    public boolean isFileDeleted(String filePath) {
        return deletedFiles.contains(filePath);
    }
}
