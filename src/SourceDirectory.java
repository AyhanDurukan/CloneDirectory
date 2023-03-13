import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SourceDirectory {
    private String sourcePath;
    private File sourceDirectory;

    public SourceDirectory (String sourcePath) {
        this.sourcePath = sourcePath;
        this.sourceDirectory = new File(sourcePath);
        System.out.println("\nRepertoire source : " + sourceDirectory + "\n");
    }

    public List<String> listSourceDirectory() throws IOException {
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
        List<String> files = listSourceDirectory();
        System.out.println("Voici le contenu du repertoire " + sourceDirectory.getName() + ":");
        for (String file : files) {
            System.out.println(file);
        }
    }

    public void send(int port) throws IOException {
        Socket client = new Socket("127.0.0.1", port);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        List<String> files = listSourceDirectory();

        int numFiles = files.size();
        out.write(Integer.toString(numFiles) + "\n");
        for (String file : files) {
            out.write(file + "\n");
            out.flush();
        }

        String reply = in.readLine();
        System.out.println(reply);
        client.close();
    }

    public static void main(String[] args) throws IOException {
        SourceDirectory D = new SourceDirectory("C:\\Users\\ayhan\\OneDrive\\Documents\\1er année ENSISA\\Semestre 2\\AOO Java\\CloneDirectory\\testD");
        D.send(8000);
    }

}