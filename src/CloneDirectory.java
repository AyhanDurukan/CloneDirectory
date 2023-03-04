import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CloneDirectory implements Runnable {
    private String sourcePath;
    private File sourceDirectory;
    private String destinationPath;
    private File destinationDirectory;

    public CloneDirectory (String sourcePath,String destinationPath) {
        this.sourcePath = sourcePath;
        this.sourceDirectory = new File(sourcePath);
        System.out.println("\nRepertoire source : " + sourceDirectory + "\n");
        this.destinationPath = destinationPath;
        this.destinationDirectory = new File(destinationPath);
        System.out.println("\nRepertoire destination : " + destinationDirectory + "\n");
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
                        String fileName = String.join("\\", temp);
                        files.add(fileName);
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

    public void clone(String destinationPath) throws IOException {

        List<String> files = listSourceDirectory(this.getSourceDirectory());
        for (String file : files) {
            System.out.println(file);
            StringTokenizer modifiedPath = new StringTokenizer(file, "\\");
            int count = modifiedPath.countTokens();
            System.out.println(count);

            String currentPath = destinationPath;
            while (modifiedPath.hasMoreTokens()) {
                String token = modifiedPath.nextToken();
                count--;

                if (count == 0) {
                    String fileName = token;
                    String filePath = currentPath + "\\" + fileName;
                    File newFile = new File(filePath);

                    if (newFile.exists()) {
                        long sourceLastModified = new File(sourcePath + "\\" + file).lastModified();
                        long destinationLastModified = newFile.lastModified();

                        if (sourceLastModified > destinationLastModified) {
                            if (newFile.delete()) {
                                System.out.println("Fichier " + fileName + " supprimé");
                            } else {
                                System.out.println("Impossible de supprimer le fichier " + fileName);
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }

                    if (newFile.createNewFile()) {
                        System.out.println("Fichier " + fileName + " créé");
                    } else {
                        System.out.println("Impossible de créer le fichier " + fileName);
                        continue;
                    }

                    String sourceFilePath = sourcePath + "\\" + file;
                    FileInputStream fileInputStream = new FileInputStream(sourceFilePath);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

                    FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                    }

                    bufferedReader.close();
                    bufferedWriter.close();
                    fileInputStream.close();
                    fileOutputStream.close();

                } else {
                    currentPath += "\\" + token;
                    File currentDirectory = new File(currentPath);
                    if (!currentDirectory.exists()) {
                        currentDirectory.mkdir();
                        System.out.println("Dossier " + token + " créé");
                    }
                }
            }
        } removeDeletedFiles();
    }

    public void removeDeletedFiles() throws IOException {
        List<String> sourceFiles = listSourceDirectory(getSourceDirectory());
        List<String> destinationFiles = new ArrayList<>();

        // list all files in the destination directory
        File[] destinationFilesArray = destinationDirectory.listFiles();
        if (destinationFilesArray != null) {
            for (File file : destinationFilesArray) {
                if (file.isFile()) {
                    destinationFiles.add(file.getName());
                }
            }
        }

        for (String destinationFile : destinationFiles) {
            if (!sourceFiles.contains(destinationFile)) {
                File file = new File(destinationDirectory.getPath() + "\\" + destinationFile);
                if (file.delete()) {
                    System.out.println("Fichier " + destinationFile + " supprimé de la destination");
                } else {
                    System.out.println("Impossible de supprimer le fichier " + destinationFile + " de la destination");
                }
            }
        }
    }

    public void run() {
        while (true) {
            try {
                clone(destinationPath);
                Thread.sleep(4000);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }
}