import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SourceDirectory D = new SourceDirectory("C:/Users/ayhan/OneDrive/Documents/1er année ENSISA/Semestre 2/AOO Java/CloneDirectory/test","C:/Users/ayhan/OneDrive/Documents/1er année ENSISA/Semestre 2/AOO Java/CloneDirectory");
        D.listDirectory(D.getDirectory());
    }
}