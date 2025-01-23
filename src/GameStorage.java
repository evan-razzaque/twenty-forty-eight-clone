import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class GameStorage {
    private static final String saveFolderPath = "SaveData";
    private static File saveFile;

    public static String[] load(Grid grid) {
        // Creates SaveData directory if it doesn't exist
        new File(saveFolderPath).mkdirs();
        saveFile = new File(saveFolderPath + "/grid" + grid.getGridSize() + ".txt");

        String[] gridData;

        try {
            if (!saveFile.exists()) {
                saveFile.createNewFile();
                Files.writeString(Path.of(saveFile.getPath()), grid + "\n0\n0\n0\n0\n0");
            }

            gridData = Files.readAllLines(saveFile.toPath()).toArray(new String[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return gridData;
    }

    public static void save(Grid grid) {
        saveFile = new File(saveFolderPath + "/grid" + grid.getGridSize() + ".txt");

        try {
            Files.writeString(saveFile.toPath(),
                grid + "\n" +
                grid.getHighScore() + "\n" +
                grid.getScore() + "\n" +
                grid.getNumberCount() + "\n" +
                ((grid.getHasWon())? 1:0) + "\n" +
                ((grid.isGameContinued())? 1:0)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}