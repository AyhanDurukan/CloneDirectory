import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        CloneDirectory D = new CloneDirectory("C:\\Users\\ayhan\\OneDrive\\Documents\\1er année ENSISA\\Semestre 2\\AOO Java\\CloneDirectory\\testD");
        System.out.println("\nVoici la liste : " + D.listSourceDirectory(D.getSourceDirectory()) + "\n");
        D.printSourceFile();

        D.clone("C:\\Users\\ayhan\\OneDrive\\Documents\\1er année ENSISA\\Semestre 2\\AOO Java\\CloneDirectory\\testDD");
    }
}