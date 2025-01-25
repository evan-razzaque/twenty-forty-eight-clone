import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A class to provide methods to access game saves.
 *
 * @author Evan Razzaque
 */
public abstract class GameStorage {
    /** A format string used to the path of a grid save, where '%d' is the gridSize. **/
    private static final String SAVE_FILE_TEMPLATE = "SaveData/grid%d.json";
    private static final String SAVE_FOLDER_PATH = SAVE_FILE_TEMPLATE.substring(0, SAVE_FILE_TEMPLATE.indexOf('/') + 1);

    /** The file to save the game data to **/
    private static File saveFile;

    /**
     * A method to check if a save file exists for a particular grid.
     *
     * @param gridSize The size of the grid to check for
     * @return whether the save exists or not
     */
    public static boolean saveExists(int gridSize) {
        return Files.exists(Path.of(SAVE_FILE_TEMPLATE.formatted(gridSize)));
    }

    /**
     * A method to load the grid's state.
     *
     * @param gridSize The size of the grid to load
     * @return grid state JSON object
     */
    public static JSONObject load(int gridSize) {
        // Creates SaveData directory if it doesn't exist
        new File(SAVE_FOLDER_PATH).mkdirs();
        saveFile = new File(SAVE_FILE_TEMPLATE.formatted(gridSize));

        JSONObject gridData;

        try {
            if (!saveFile.exists()) {
                saveFile.createNewFile();

                Files.writeString(Path.of(saveFile.getPath()), """
                    {"grid": "%s", "highScore": 0, "score": 0, "numberCount": 0, "hasWon": false, "gameContinued": false}
                """.formatted("_".repeat((int) Math.pow(gridSize, 2))));
            }

            gridData = new JSONObject(Files.readString(saveFile.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return gridData;
    }

    /**
     * A method to save a grid's state to a file.
     *
     * @param grid The grid object to save
     */
    public static void save(Grid grid) {
        saveFile = new File(SAVE_FILE_TEMPLATE.formatted(grid.getGridSize()));

        JSONObject gridData = new JSONObject()
            .put("grid", grid)
            .put("highScore", grid.getHighScore())
            .put("score", grid.getScore())
            .put("numberCount", grid.getNumberCount())
            .put("hasWon", grid.getHasWon())
            .put("gameContinued", grid.isGameContinued());

        try {
            Files.writeString(saveFile.toPath(), gridData.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}