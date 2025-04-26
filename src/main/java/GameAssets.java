import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.util.HashMap;

/**
 * A class containing game assets.
 *
 * @author Evan Razzaque
 */
public class GameAssets {
    /**
     * The dialog that shows up when the player loses
     */
    private static Alert loseDialog;

    /**
     * The dialog that shows up when the player loses
     */
    private static Alert winDialog;

    /**
     * A hashmap mapping tile numbers to background colors for a tile.
     *
     * @return the hashmap of colors
     */
    public static HashMap<Long, Paint> getColors() {
        HashMap<Long, Paint> colors = new HashMap<>();
        colors.put(2L, Color.valueOf("#eee4da"));
        colors.put(4L, Color.valueOf("#eee1c9"));
        colors.put(8L, Color.valueOf("#f3b27a"));
        colors.put(16L, Color.valueOf("#f69664"));
        colors.put(32L, Color.valueOf("#f77F5f"));
        colors.put(64L, Color.valueOf("#f75f3b"));
        colors.put(4096L, Color.valueOf("#ef666d"));
        colors.put(8192L, Color.valueOf("#ed4d59"));
        colors.put(16384L, Color.valueOf("#e14338"));
        colors.put(32768L, Color.valueOf("#71b4d6"));
        colors.put(65536L, Color.valueOf("#5ca0df"));
        colors.put(131072L, Color.valueOf("#007bbe"));

        return colors;
    }

    /**
     * Returns a timeline used to update the grid display and game logic.
     *
     * @param grid An instance of a {@link Grid}.
     * @param ms The duration of how long the tiles should take to move to their new position
     * @return the render timeline
     */
    public static Timeline getRenderTimeline(Grid grid, double ms) {
        return new Timeline(new KeyFrame(Duration.millis(ms), event -> {
            grid.addNumber();
            grid.setGridNumbers();
            grid.renderGrid();

            if (grid.getNumberCount() == grid.getGridSize() * grid.getGridSize()) {
                if (grid.getPlayableMoves() == 0) {
                    grid.displayLoseDialog();
                }
            } else if (grid.getHasWon() && !grid.isGameContinued()) {
                grid.displayWinDialog();
            }
        }));
    }

    /**
     * A method to get the dialog when the player loses.
     *
     * @return lose dialog
     */
    public static Alert getLoseDialog() {
        if (loseDialog != null) {
            return loseDialog;
        }

        loseDialog = new Alert(Alert.AlertType.INFORMATION, "New game?");
        loseDialog.setHeaderText("Game Over!");
        loseDialog.getDialogPane().getButtonTypes().removeFirst();
        loseDialog.getDialogPane().getButtonTypes().add(ButtonType.NO);
        loseDialog.getDialogPane().getButtonTypes().add(ButtonType.YES);
        ((Button) loseDialog.getDialogPane().lookupButton(ButtonType.YES)).setDefaultButton(false);
        ((Button) loseDialog.getDialogPane().lookupButton(ButtonType.NO)).setDefaultButton(true);

        return loseDialog;
    }

    /**
     * A method to get the dialog when the player win.
     *
     * @return win dialog
     */
    public static Alert getWinDialog() {
        if (winDialog != null) {
            return winDialog;
        }

        winDialog = new Alert(Alert.AlertType.INFORMATION, "Would you like to continue?");
        winDialog.setHeaderText("You Win!");
        winDialog.getDialogPane().getButtonTypes().removeFirst();
        winDialog.getDialogPane().getButtonTypes().add(ButtonType.NO);
        winDialog.getDialogPane().getButtonTypes().add(ButtonType.YES);

        return winDialog;
    }
}