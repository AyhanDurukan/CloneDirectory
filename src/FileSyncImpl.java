public class FileSyncImpl implements FileSync {

    private File sourceDirectory;
    private File targetDirectory;
    private List<String> currentFileList;
    private List<String> newFileList;

    public FileSyncImpl(File sourceDirectory, File targetDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.targetDirectory = targetDirectory;
        currentFileList = getCurrentFileList(sourceDirectory);
        newFileList = new ArrayList<>();
    }

    private List<String> getCurrentFileList(File directory) {
        File[] files = directory.listFiles();
        List<String> fileList = new ArrayList<>();

        for (File file : files) {
            fileList.add(file.getName());
        }

        return fileList;
    }

    public void synchronize() {
        newFileList = getCurrentFileList(targetDirectory);

        List<String> deletedFiles = new ArrayList<>(currentFileList);
        deletedFiles.removeAll(newFileList);

        for (String fileName : deletedFiles) {
            File fileToDelete = new File(targetDirectory, fileName);
            deleteRecursively(fileToDelete);
        }

        // Copiez les fichiers du répertoire source vers le répertoire cible, en ignorant les fichiers supprimés.
        // ...

        currentFileList = newFileList;
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File child : files) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}
