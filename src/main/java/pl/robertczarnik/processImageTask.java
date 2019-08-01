package pl.robertczarnik;
import java.nio.file.Path;

public class processImageTask {
    private final String command;
    private final Path imagePath;

    public processImageTask(Path imagePath, String command) {
        this.command=command;
        this.imagePath=imagePath;
    }

    public String getCommand() {
        return command;
    }

    public Path getImagePath() {
        return imagePath;
    }
}
