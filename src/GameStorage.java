import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A class providing methods to access game saves.
 *
 * @author Evan Razzaque
 */
public abstract class GameStorage {
    private static final String SAVE_FOLDER_PATH = "SaveData";

    /** The file to save the game data to **/
    private static File saveFile;

    /**
     * A method to check if a save exists for a particular grid.
     *
     * @param gridSize The size of the grid
     * @return whether the save exists or not
     */
    public static boolean saveExists(int gridSize) {
        return Files.exists(Path.of(SAVE_FOLDER_PATH + "/grid" + gridSize + ".txt"));
    }

    public static String[] load(int gridSize) {
        // Creates SaveData directory if it doesn't exist
        new File(SAVE_FOLDER_PATH).mkdirs();
        saveFile = new File(SAVE_FOLDER_PATH + "/grid" + gridSize + ".txt");

        String[] gridData;

        try {
            if (!saveFile.exists()) {
                saveFile.createNewFile();
                Files.writeString(Path.of(saveFile.getPath()),
                    "_".repeat((int) Math.pow(gridSize, 2)) + "\n0".repeat(5));
            }

            gridData = Files.readAllLines(saveFile.toPath()).toArray(new String[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return gridData;
    }

    public static void save(Grid grid) {
        saveFile = new File(SAVE_FOLDER_PATH + "/grid" + grid.getGridSize() + ".txt");

        try {
            Files.writeString(saveFile.toPath(),
                grid + "\n" +
                grid.getHighScore() + "\n" +
                grid.getScore() + "\n" +
                grid.getNumberCount() + "\n" +
                (grid.getHasWon()? 1:0) + "\n" +
                (grid.isGameContinued()? 1:0)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}