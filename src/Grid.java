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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Grid {
    private int gridSize;
    private double tileSize;
    private long[][] numberGrid, combinedNumbersLocations;

    private ArrayList<GridNumber> gridNumbers;
    private ArrayList<long[][]> previousGridStates;
    private ArrayList<Integer> previousNumberCounts;
    private ArrayList<Long> previousScores;

    private int moveCount, numberCount;
    private long score, highScore;
    private boolean hasWon, gameContinued;

    private final int UNDO_LIMIT;
    private final GraphicsContext GC;
    private static final HashMap<Long, Paint> COLORS = GameAssets.getColors();
    private static final char[] PREFIXES = new char[] {'K', 'M', 'B', 'T', 'q', 'Q', 's', 'S'};
    private final Timeline partialRenderTimeline, renderTimeline;

    public Grid(GraphicsContext gc, int gridSize, int undoLimit) {
        this.GC = gc;
        this.GC.setTextAlign(TextAlignment.CENTER);
        this.GC.setTextBaseline(VPos.CENTER);

        if (gc.getCanvas().getWidth() != gc.getCanvas().getHeight())
            throw new IllegalStateException("Canvas width must be equal to canvas height");

        this.gridSize = gridSize;
        tileSize = gc.getCanvas().getWidth() / gridSize;
        this.UNDO_LIMIT = undoLimit;

        partialRenderTimeline = new Timeline();
        renderTimeline = GameAssets.getRenderTimeline(this, 80);
    }

    public void load() {
        String[] gridData = GameStorage.load(gridSize);

        char[] numbers = gridData[0].toCharArray();

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (numbers[row * gridSize + col] == '_') continue;
                numberGrid[row][col] = (long) (Math.pow(2, numbers[row * gridSize + col] - 32));
            }
        }

        highScore = Long.parseLong(gridData[1]);
        score = Long.parseLong(gridData[2]);
        numberCount = Integer.parseInt(gridData[3]);
        hasWon = gridData[4].equals("1");
        gameContinued = gridData[5].equals("1");
    }

    public void startGame(int gridSize) {
        if (gridSize < 2)
            throw new IllegalArgumentException("Grid size cannot be less than 2");

        this.gridSize = gridSize;
        tileSize = GC.getCanvas().getWidth() / gridSize;
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

    public void startGame() {
        startGame(gridSize);
    }

    public int getGridSize() {
        return gridSize;
    }

    public double getTileSize() {
        return tileSize;
    }

    public int getUndoLimit() {
        return UNDO_LIMIT;
    }

    public int getNumberCount() {
        return numberCount;
    }

    public long getScore() {
        return score;
    }

    public long getHighScore() {
        return highScore;
    }

    public boolean getHasWon() {
        return hasWon;
    }

    public boolean isGameContinued() {
        return gameContinued;
    }

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

    public void addNumber(int col, int row, long value) {
        numberGrid[row][col] = value;
        gridNumbers.add(new GridNumber(row, col, value));
        numberCount++;
    }

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

    public void moveNumber(int r1, int c1, int[] d) {
        long value = numberGrid[r1][c1];
        if (value == 0) return;

        int r2 = r1 + d[0];
        int c2 = c1 + d[1];

        // Calculates the amount of moves an individual tile can move in a given direction
        int moves = (int) (Math.abs(r1 - (gridSize - 1) * Math.signum(d[0] + 1)) * Math.abs(d[0]) + Math.abs(c1 - (gridSize - 1) * Math.signum(d[1] + 1)) * Math.abs(d[1]));

        int combineCount = 0;

        for (int i = 0; i < moves; i++) {
            if (numberGrid[r2][c2] != value && numberGrid[r2][c2] != 0 || combinedNumbersLocations[r2][c2] == 1 || combineCount > 0)
                break;

            // Saves previous grid state
            if (moveCount == 0) {
                if (getPlayableMoves() > 0) storeGridState();

                for (GridNumber n : gridNumbers) {
                    n.setOldPos();
                    n.setOldValue();
                }
            }

            // Combining tiles
            if (numberGrid[r2][c2] == value) {
                value += value;
                combineCount++;
                combinedNumbersLocations[r2][c2] = 1;
                numberCount--;
                score += value;

                if (score > highScore) highScore = score;
            }

            // Moving the tile
            numberGrid[r2][c2] = value;
            numberGrid[r1][c1] = 0;
            moveCount++;

            // Set values for tile objects
            for (GridNumber n : gridNumbers) {
                if (n.getRow() == r1 && n.getCol() == c1) {
                    n.setPos(r2, c2);
                    n.setValue(value);
                }
            }

            if (value == 2048 && !hasWon)
                hasWon = true;

            // Updating tile location pointers
            r1 += d[0];
            c1 += d[1];
            r2 += d[0];
            c2 += d[1];
        }
    }

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

    public void move(String direction) {
        moveCount = 0;
        combinedNumbersLocations = new long[gridSize][gridSize];
        GridAction.moveNumbers(this, direction);

        if (moveCount > 0) {
            render();
        }
    }

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

    public static double round4(long value) {
        int digits = (int) Math.log10(value);
        long divisor = (long) Math.pow(10, (digits / 3) * 3);
        int roundingFactor = (int) Math.pow(10, (3 - (digits % 3)));

        return (double) Math.round(value * roundingFactor / divisor) / roundingFactor;
    }

    public void drawNumber(int col, int row, double offsetX, double offsetY, long value) {
        double fontSize;
        double tileSize = this.tileSize * 0.9;
        double tileOffset = (this.tileSize - tileSize) / 2;
        String tileText = String.valueOf(value);

        if (value < 100) {
            fontSize = this.tileSize * 0.366;
        } else if (value < 1000) {
            fontSize = this.tileSize * 0.333;
        } else {
            fontSize = this.tileSize * 0.233;
        }

        if (value > 10_000) {
            tileText = round4(value) + " " + PREFIXES[(int) Math.log10(value) / 3 - 1];
        }

        GC.setFill(COLORS.getOrDefault(value, Color.GOLD));
        if (value > 131072) {
            GC.setFill(Color.BLACK);
        }

        GC.fillRect(
            this.tileSize*col + tileOffset + offsetX,
            this.tileSize*row + tileOffset + offsetY,
            tileSize,
            tileSize
        );

        GC.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSize));
        GC.setFill(Color.WHITE);
        if (value < 8) GC.setFill(Color.valueOf("#444444"));
        GC.fillText(tileText,
            this.tileSize/2 + this.tileSize*col + offsetX,
            this.tileSize/2 + this.tileSize*row + offsetY
        );
    }

    public void drawNumber(int col, int row, long value) {
        drawNumber(col, row, 0, 0, value);
    }

    public void partialRenderGrid(int step, int totalSteps) {
        double offsetX, offsetY;
        GC.clearRect(0, 0, GC.getCanvas().getWidth(), GC.getCanvas().getHeight());

        for (GridNumber n : gridNumbers) {
            offsetX = ((n.getCol() - n.getOldCol()) * this.tileSize) * ((double) step / totalSteps);
            offsetY = ((n.getRow() - n.getOldRow()) * this.tileSize) * ((double) step / totalSteps);

            drawNumber(n.getOldCol(), n.getOldRow(), offsetX, offsetY, n.getOldValue());
        }
    }

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

    public void restartGame(int gridSize) {
        numberGrid = new long[this.gridSize][this.gridSize];
        score = 0;
        numberCount = 0;
        hasWon = false;
        gameContinued = false;

        GameStorage.save(this);
        startGame(gridSize);
    }

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

    @Override
    public String toString() {
        StringBuilder numbers = new StringBuilder();

        char c;
        for (long[] row : numberGrid) {
            for (long col : row) {
                if (col > 0) {
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