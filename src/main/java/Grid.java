import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class representing a game of 2048.
 *
 * @author Evan Razzaque
 */
public class Grid {
    /**
     * The size of the grid
     */
    private int gridSize;

    /**
     * The size (in pixels) of each cell
     */
    private double cellSize;

    /**
     * A 2D array containing the numbers for the grid
     */
    private long[][] numberGrid;

    /**
     * An array to store whether a cell has been combined or not while performing a move
     */
    private boolean[][] combinedStateGrid;

    /**
     * Used to store grid number objects for animating tile movement
     */
    private ArrayList<GridNumber> gridNumbers;

    /**
     * Used to stores previous grid states to allow the player to undo moves
     */
    private ArrayList<long[][]> previousGridStates;

    /**
     * Stores the number count for each previous grid state
     */
    private ArrayList<Integer> previousNumberCounts;

    /**
     * Stores the score for each previous grid state
     */
    private ArrayList<Long> previousScores;

    /**
     * The number tiles each number moves while the player performs an action
     */
    private int moveCount;

    /**
     * The number of number tiles on the grid
     */
    private int numberCount;

    /**
     * The current score for the game
     */
    private long score;

    /**
     * The high score for the game and grid size
     */
    private long highScore;

    /**
     * Whether the player has reached the 2048 tile or not
     */
    private boolean hasWon;

    /**
     * Whether the player decided to continue the game after reach 2048
     */
    private boolean gameContinued;

    /**
     * The maximum amount of moves the player can undo
     */
    private final int UNDO_LIMIT;

    /**
     * The {@link GraphicsContext} instance to use to render the grid
     */
    private final GraphicsContext GC;

    /**
     * A hashmap mapping tile numbers to background colors for a tile
     */
    private static final HashMap<Long, Paint> COLORS = GameAssets.getColors();

    /**
     * Prefixes for displaying large numbers on a tile
     */
    private static final char[] PREFIXES = new char[] {'K', 'M', 'B', 'T', 'q', 'Q', 's', 'S'};

    /**
     * The render timeline used to animate the tiles moving
     */
    private final Timeline partialRenderTimeline;

    /**
     * The render timeline to update the grid display and game logic
     */
    private final Timeline renderTimeline;

    /**
     * A constructor for a grid.
     *
     * @param gc The {@link GraphicsContext} instance to use to render the grid
     * @param gridSize The size of the grid
     * @param undoLimit The maximum amount moves that can be undone
     */
    public Grid(GraphicsContext gc, int gridSize, int undoLimit) {
        this.GC = gc;
        this.GC.setTextAlign(TextAlignment.CENTER);
        this.GC.setTextBaseline(VPos.CENTER);

        if (gc.getCanvas().getWidth() != gc.getCanvas().getHeight())
            throw new IllegalStateException("Canvas width must be equal to canvas height");

        this.gridSize = gridSize;
        cellSize = gc.getCanvas().getWidth() / gridSize;
        this.UNDO_LIMIT = undoLimit;

        partialRenderTimeline = new Timeline();
        renderTimeline = GameAssets.getRenderTimeline(this, 80);
    }

    /**
     * A method to load the grid's state from its save file. <br>
     * A new file will be created if none exists.
     * @see GameStorage#load(int)
     */
    public void load() {
        JSONObject gridData = GameStorage.load(gridSize);

        char[] numbers = gridData.getString("grid").toCharArray();

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (numbers[row * gridSize + col] == '_') continue;
                numberGrid[row][col] = (long) (Math.pow(2, numbers[row * gridSize + col] - 32));
            }
        }

        highScore = gridData.getLong("highScore");
        score = gridData.getLong("score");
        numberCount = gridData.getInt("numberCount");
        hasWon = gridData.getBoolean("hasWon");
        gameContinued = gridData.getBoolean("gameContinued");
    }

    /**
     * A method to start the game with a given grid size.
     *
     * @param gridSize The size of the grid
     */
    public void startGame(int gridSize) {
        if (gridSize < 2)
            throw new IllegalArgumentException("Grid size cannot be less than 2");

        this.gridSize = gridSize;
        cellSize = GC.getCanvas().getWidth() / gridSize;
        numberGrid = new long[gridSize][gridSize];
        gridNumbers = new ArrayList<>();
        previousGridStates = new ArrayList<>();
        previousNumberCounts = new ArrayList<>();
        previousScores = new ArrayList<>();

        load();

        if (numberCount == 0) {
            addNumber();
            addNumber();

            GameStorage.save(this);
        }

        setGridNumbers();
        renderGrid();
    }

    /**
     * A method to start the game with the same grid size.
     */
    public void startGame() {
        startGame(gridSize);
    }

    /**
     * Gets the grid size.
     *
     * @return the size of the grid
     */
    public int getGridSize() {
        return gridSize;
    }

    /**
     * Gets the cells' size.
     *
     * @return the size of each cell
     */
    public double getCellSize() {
        return cellSize;
    }

    /**
     * Gets the undo limit.
     *
     * @return the undo limit
     */
    public int getUndoLimit() {
        return UNDO_LIMIT;
    }

    /**
     * Gets the number of number tiles on the grid.
     *
     * @return the amount of numbers
     */
    public int getNumberCount() {
        return numberCount;
    }

    /**
     * Gets the current score of the game.
     *
     * @return the current score
     */
    public long getScore() {
        return score;
    }

    /**
     * Gets the highscore for the current grid size.
     *
     * @return the highscore for the grid size
     */
    public long getHighScore() {
        return highScore;
    }

    /**
     * Determines if the player has won.
     *
     * @return whether the player has won or not
     */
    public boolean getHasWon() {
        return hasWon;
    }

    /**
     * Determines if the game has been continued.
     *
     * @return whether the game is continued or not
     */
    public boolean isGameContinued() {
        return gameContinued;
    }

    /**
     * A method to set the values in the grid numbers arraylist.
     */
    public void setGridNumbers() {
        gridNumbers.clear();

        long value;
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                value = numberGrid[row][col];
                if (value > 0) gridNumbers.add(new GridNumber(row, col, value));
            }
        }
    }

    /**
     * A method to add a number tile on the grid.
     *
     * @param col Grid column
     * @param row Grid row
     * @param value The value of tile
     */
    public void addNumber(int col, int row, long value) {
        numberGrid[row][col] = value;
        gridNumbers.add(new GridNumber(row, col, value));
        numberCount++;
    }

    /**
     * A method to add a number tile with a random location and
     * with a value of 2 or 4.
     */
    public void addNumber() {
        int randRow, randCol;
        int randInt = (int) (1 + Math.random() * 100);

        int value = 2;
        if (randInt <= 10) value = 4;

        while (true) {
            randRow = (int) (Math.random() * gridSize);
            randCol = (int) (Math.random() * gridSize);

            if (numberGrid[randRow][randCol] == 0) {
                addNumber(randCol, randRow, value);
                break;
            }
        }
    }

    /**
     * A method to move an individual number across the grid.
     *
     * @param r1 The number's row on the grid
     * @param c1 The number's column on the grid
     * @param d The direction (x, y) to number
     */
    public void moveNumber(int r1, int c1, int[] d) {
        long value = numberGrid[r1][c1];
        if (value == 0) return;

        // Target cell
        int r2 = r1 + d[0];
        int c2 = c1 + d[1];

        // Calculates the amount of moves an individual number can move in a given direction
        int moves = (int) (Math.abs(r1 - (gridSize - 1) * Math.signum(d[0] + 1)) * Math.abs(d[0]) + Math.abs(c1 - (gridSize - 1) * Math.signum(d[1] + 1)) * Math.abs(d[1]));

        int combineCount = 0;

        for (int i = 0; i < moves; i++) {
            // If a number has reached another number with a different value, the number has already combined with
            // another number, or the target cell has already had a combination occur
            if (numberGrid[r2][c2] != value && numberGrid[r2][c2] != 0 || combinedStateGrid[r2][c2] || combineCount > 0)
                break;

            // Saving previous grid state
            if (moveCount == 0) {
                if (getPlayableMoves() > 0) storeGridState();

                for (GridNumber n : gridNumbers) {
                    n.setOldPos();
                    n.setOldValue();
                }
            }

            // Combining numbers
            if (numberGrid[r2][c2] == value) {
                value += value;
                combineCount++;
                combinedStateGrid[r2][c2] = true;
                numberCount--;
                score += value;

                if (score > highScore) highScore = score;
            }

            // Moving the numbers
            numberGrid[r2][c2] = value;
            numberGrid[r1][c1] = 0;
            moveCount++;

            // Update number objects
            for (GridNumber n : gridNumbers) {
                if (n.getRow() == r1 && n.getCol() == c1) {
                    n.setPos(r2, c2);
                    n.setValue(value);
                }
            }

            if (value == 2048 && !hasWon) hasWon = true;

            // Updating cell pointers
            r1 += d[0];
            c1 += d[1];
            r2 += d[0];
            c2 += d[1];
        }
    }

    /**
     * A method to store the previous grid states.
     */
    private void storeGridState() {
        long[][] numberGridTemp = new long[gridSize][gridSize];

        for (int row = 0; row < gridSize; row++)
            System.arraycopy(numberGrid[row], 0, numberGridTemp[row], 0, gridSize);

        previousGridStates.add(numberGridTemp);
        previousNumberCounts.add(numberCount);
        previousScores.add(score);

        if (previousGridStates.size() > UNDO_LIMIT) {
            previousGridStates.removeFirst();
            previousNumberCounts.removeFirst();
            previousScores.removeFirst();
        }
    }

    /**
     * A method to move the numbers in a given direction.
     *
     * @param direction The direction to move the tile in
     */
    public void move(String direction) {
        moveCount = 0;
        combinedStateGrid = new boolean[gridSize][gridSize];
        GridAction.moveNumbers(this, direction);

        if (moveCount > 0) {
            render();
        }
    }

    /**
     * A method to undo the latest move and restore the previous grid state.
     */
    public void undo() {
        if (previousGridStates.isEmpty()) return;

        numberGrid = previousGridStates.getLast();
        numberCount = previousNumberCounts.getLast();
        score = previousScores.getLast();

        previousGridStates.removeLast();
        previousNumberCounts.removeLast();
        previousScores.removeLast();

        setGridNumbers();
        GameStorage.save(this);
        renderGrid();
    }

    /**
     * A method to return the value displayed with 4 digits with its decimal point shifted to the thousands' separator. <br>
     * For example, 131,072 would become 131.0, which can be displayed as 131.0 K.
     *
     * @param value The value to truncate.
     * @return the truncated value with its decimal point shifted
     */
    public static double round4(long value) {
        int digits = (int) Math.log10(value);
        long divisor = (long) Math.pow(10, (digits / 3) * 3);
        int roundingFactor = (int) Math.pow(10, (3 - (digits % 3)));

        return (double) Math.round(value * roundingFactor / divisor) / roundingFactor;
    }

    /**
     * Draws a number on the grid with an offset.
     *
     * @param col Grid column
     * @param row Grid row
     * @param offsetX The x-offset of the number
     * @param offsetY The y-offset of the number
     * @param value The value of the number
     */
    public void drawNumber(int col, int row, double offsetX, double offsetY, long value) {
        double fontSize;
        double cellSize = this.cellSize * 0.9;
        double cellOffset = (this.cellSize - cellSize) / 2;
        String cellText = String.valueOf(value);

        if (value < 100) {
            fontSize = this.cellSize * 0.366;
        } else if (value < 1000) {
            fontSize = this.cellSize * 0.333;
        } else {
            fontSize = this.cellSize * 0.233;
        }

        if (value > 10_000) {
            cellText = round4(value) + " " + PREFIXES[(int) Math.log10(value) / 3 - 1];
        }

        GC.setFill(COLORS.getOrDefault(value, Color.GOLD));
        if (value > 131072) {
            GC.setFill(Color.BLACK);
        }

        GC.fillRect(
            this.cellSize * col + cellOffset + offsetX,
            this.cellSize * row + cellOffset + offsetY,
            cellSize,
            cellSize
        );

        GC.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSize));
        GC.setFill(Color.WHITE);
        if (value < 8) GC.setFill(Color.valueOf("#444444"));
        GC.fillText(cellText,
            this.cellSize / 2 + this.cellSize * col + offsetX,
            this.cellSize / 2 + this.cellSize * row + offsetY
        );
    }

    /**
     * A method to draw a number on the grid.
     *
     * @param col Grid column
     * @param row Grid row
     * @param value The value of the number
     */
    public void drawNumber(int col, int row, long value) {
        drawNumber(col, row, 0, 0, value);
    }

    /**
     * Renders a single "frame" of during the animation of the numbers moving to their new location.
     *
     * @param step The current frame number
     * @param totalSteps Total amount of frames
     */
    public void partialRenderGrid(int step, int totalSteps) {
        double offsetX, offsetY;
        GC.clearRect(0, 0, GC.getCanvas().getWidth(), GC.getCanvas().getHeight());

        for (GridNumber n : gridNumbers) {
            offsetX = ((n.getCol() - n.getOldCol()) * this.cellSize) * ((double) step / totalSteps);
            offsetY = ((n.getRow() - n.getOldRow()) * this.cellSize) * ((double) step / totalSteps);

            drawNumber(n.getOldCol(), n.getOldRow(), offsetX, offsetY, n.getOldValue());
        }
    }

    /**
     * A method to render the grid.
     */
    public void renderGrid() {
        long n;
        GC.clearRect(0, 0, GC.getCanvas().getWidth(), GC.getCanvas().getHeight());

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                n = numberGrid[row][col];

                if (n != 0) drawNumber(col, row, n);
            }
        }
    }

    /**
     * A method to render the numbers on the grid moving, then renders grid again once the animation has finished.
     */
    public void render() {
        AtomicInteger i = new AtomicInteger(1);
        double renderTime = renderTimeline.getKeyFrames().getFirst().getTime().toMillis();
        partialRenderTimeline.setCycleCount(20);

        if (!partialRenderTimeline.getKeyFrames().isEmpty())
            partialRenderTimeline.getKeyFrames().removeFirst();

        partialRenderTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(renderTime / partialRenderTimeline.getCycleCount()),
            event -> partialRenderGrid(i.getAndIncrement(), partialRenderTimeline.getCycleCount()))
        );

        partialRenderTimeline.playFromStart();
        renderTimeline.playFromStart();
    }

    /**
     * Gets the number of moves the player can make.
     * @return number of playable moves
     */
    public int getPlayableMoves() {
        int moves = 0;

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (col < gridSize - 1) {
                    if (numberGrid[row][col] == numberGrid[row][col + 1])
                        moves++;
                }

                if (row < gridSize - 1) {
                    if (numberGrid[row][col] == numberGrid[row + 1][col])
                        moves++;
                }
            }
        }

        return moves;
    }

    /**
     * A method to restart the game with a new grid size.
     *
     * @param gridSize The size of the grid
     */
    public void restartGame(int gridSize) {
        numberGrid = new long[this.gridSize][this.gridSize];
        score = 0;
        numberCount = 0;
        hasWon = false;
        gameContinued = false;

        GameStorage.save(this);
        startGame(gridSize);
    }

    /**
     * A method to display the game over dialog.
     */
    public void displayLoseDialog() {
        Alert alert = GameAssets.getLoseDialog();
        Platform.runLater(() -> {
            Optional<ButtonType> choice = alert.showAndWait();

            if (choice.isPresent()) {
                if (choice.get() == ButtonType.YES)
                    restartGame(gridSize);
            }
        });
    }

    /**
     * A method to display the win dialog.
     */
    public void displayWinDialog() {
        Alert alert = GameAssets.getWinDialog();
        Platform.runLater(() -> {
            Optional<ButtonType> choice = alert.showAndWait();

            if (choice.isPresent()) {
                if (choice.get() == ButtonType.NO) {
                    restartGame(gridSize);
                } else {
                    gameContinued = true;
                }
            }
        });
    }

    /**
     * A method used to format the numberGrid for the purposes of saving the numberGrid.
     *
     * @return A string containing each number on the grid all in one line.
     * @see Grid#load()
     */
    @Override
    public String toString() {
        StringBuilder numbers = new StringBuilder();

        char c;
        for (long[] row : numberGrid) {
            for (long col : row) {
                if (col > 0) {
                    // Since each value is a power of two, we store its exponent to save space
                    c = (char) ((int) Math.ceil(Math.log(col) / Math.log(2)) + 32);
                } else {
                    c = '_';
                }

                numbers.append(c);
            }
        }

        return numbers.toString();
    }
}