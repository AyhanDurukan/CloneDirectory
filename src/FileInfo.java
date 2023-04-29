public class FileInfo {
    private String relativePath;
    private String content;
    private long timestamp;

    public FileInfo(String relativePath, String content, long timestamp) {
        this.relativePath = relativePath;
        this.content = content;
        this.timestamp = timestamp;
    }
}