import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class DestinationDirectory {
    private String destinationPath;
    private File destinationDirectory;
    private List<String> sFiles;

    public DestinationDirectory (String destinationPath) {
        this.destinationPath = destinationPath;
        this.destinationDirectory = new File(destinationPath);
        System.out.println("\nRepertoire destination : " + destinationDirectory + "\n");
    }

    public void cloneSource() throws IOException {
        for (String file : sFiles) {
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

                    if (newFile.createNewFile()) {
                        System.out.println("Fichier " + fileName + " créé");
                    } else {
                        System.out.println("Impossible de créer le fichier " + fileName);
                        continue;
                    }

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
        for (String file : sFiles) {
            System.out.println(file);
        }
    }

    public void receive (int port) throws IOException {
        ServerSocket ss = new ServerSocket (port);
        System.out.println("En attente de connexion d'un client...");
        Socket server = ss.accept();
        System.out.println("Connexion établie" + "\n");

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));

        String line;
        List<String> files = new ArrayList<>();

        int numFiles = Integer.parseInt(in.readLine());

        for (int i = 0; i < numFiles; i++) {
            line = in.readLine();
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            while (tokenizer.hasMoreTokens()) {
                String file = tokenizer.nextToken();
                //System.out.println(file);
                files.add(file);
            }
        }
        System.out.println("La liste des fichiers recus :\n" + files + "\n");
        sFiles = files;
        out.write("Les fichiers ont été reçus avec succès.");
        out.newLine();
        out.flush();
        server.close();
    }

    public static void main(String[] args) throws IOException {
        DestinationDirectory DD = new DestinationDirectory("C:\\Users\\ayhan\\OneDrive\\Documents\\1er année ENSISA\\Semestre 2\\AOO Java\\CloneDirectory\\testDD");
        DD.receive(8000);
        DD.cloneSource();
    }
}