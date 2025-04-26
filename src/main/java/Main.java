import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * A remake of 2048 with JavaFX.
 *
 * @author Evan Razzaque
 */
public class Main extends Application {
    /**
     * A method to draw the grid on the screen.
     */
    private void drawGrid() {
        int gridSize = grid.getGridSize();

        if (!root.getChildren().isEmpty()) {
            while (root.getChildren().getFirst() instanceof Rectangle) {
                root.getChildren().removeFirst();
            }
        }

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Rectangle rectangle = new Rectangle(
                    canvas.getLayoutX() + grid.getCellSize() * col,
                    canvas.getLayoutY() + grid.getCellSize() * row,
                    grid.getCellSize(),
                    grid.getCellSize()
                );

                rectangle.setFill(Color.WHITE);
                rectangle.setStroke(Color.BLACK);
                rectangle.setStrokeWidth(2);
                root.getChildren().addFirst(rectangle);
            }
        }
    }

    /**
     * A method to start a new game with a given grid size.
     *
     * @param event The event instance of the clicked grid size button
     */
    private void changeGridSize(ActionEvent event) {
        previewDisplayGc.clearRect(0, 0, previewDisplay.getWidth(), previewDisplay.getHeight());

        root.requestFocus();
        int value = Integer.parseInt(((Button) event.getSource()).getText());

        if (value == grid.getGridSize()) {
            return;
        }

        GameStorage.save(grid);
        grid.startGame(value);
        updateScoreDisplay();
        drawGrid();
    }

    /**
     * A method to display the grid with the size corresponding to the grid size button being hovered.
     *
     * @param event The event instance of the grid size button being hovered
     */
    private void displayGridPreview(MouseEvent event) {
        int gridSize = Integer.parseInt(((Button) event.getSource()).getText());

        if (gridSize == grid.getGridSize() || !GameStorage.saveExists(gridSize)) {
            return;
        }

        gridPreview = new Grid(previewDisplayGc, gridSize, 0);
        gridPreview.startGame();

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                previewDisplayGc.strokeRect(
                        gridPreview.getCellSize() * col, gridPreview.getCellSize() * row,
                    gridPreview.getCellSize(), gridPreview.getCellSize()
                );
            }
        }
    }

    /**
     * A method to add all the grid size buttons to the screen.
     */
    private void addGridSizes() {
        for (int i = 2; i <= 25; i++) {
            Button btn_gridSize = new Button(String.valueOf(i));
            btn_gridSize.setPrefWidth(75);

            btn_gridSize.setOnMouseEntered(this::displayGridPreview);
            btn_gridSize.setOnAction(this::changeGridSize);

            btn_gridSize.setOnMouseExited(event ->
                previewDisplayGc.clearRect(0, 0, previewDisplay.getWidth(), previewDisplay.getHeight())
            );

            gridSizeSelector.add(btn_gridSize, (i-2) % 3, (i-2) / 3);
        }
    }

    /**
     * A method to update the score display.
     */
    private void updateScoreDisplay() {
        lb_score.setText("High Score: " + grid.getHighScore() + "\nScore: " + grid.getScore());
    }

    /**
     * A method to perform game actions based on player input.
     *
     * @param ke The keyboard event instance.
     */
    private void gameAction(KeyEvent ke) {
        String direction = "";

        switch (ke.getCode()) {
            case W, UP -> direction = "up";
            case A, LEFT -> direction = "left";
            case S, DOWN -> direction = "down";
            case D, RIGHT -> direction = "right";
            case R -> grid.restartGame(grid.getGridSize());
            case ESCAPE -> root.requestFocus();
            case Z -> {
                if (!ke.isControlDown()) break;

                grid.undo();
                root.requestFocus();
            }
        }

        if (!direction.isEmpty()) grid.move(direction);
        updateScoreDisplay();
    }

    Grid grid, gridPreview;
    Pane root;
    GridPane gridSizeSelector;
    Canvas canvas, previewDisplay;
    GraphicsContext gc, previewDisplayGc;
    Label lb_score, lb_changeGridSize;
    Button btn_undo, btn_restart;

    /**
     * The method to set up the window and game.
     *
     * @param stage The stage instance of the application
     */
    @Override
    public void start(Stage stage) {
        canvas = new Canvas(750, 750);
        canvas.relocate(100, 140);
        gc = canvas.getGraphicsContext2D();

        previewDisplay = new Canvas(235, 235);
        previewDisplay.relocate(900, 500);
        previewDisplayGc = previewDisplay.getGraphicsContext2D();

        lb_score = new Label("High Score: 0\nScore: 0");
        lb_changeGridSize = new Label("Grid Size");
        btn_undo = new Button("Undo");
        btn_restart = new Button("New Game");
        
        grid = new Grid(gc, 4, 1);
        root = new Pane();
        gridSizeSelector = new GridPane(5, 5);
        drawGrid();

        root.getChildren().add(gridSizeSelector);
        root.getChildren().add(previewDisplay);
        root.getChildren().addAll(canvas, lb_score, lb_changeGridSize, btn_undo, btn_restart);
        Scene scene = new Scene(root, 1200, 900);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("2048");

        lb_score.relocate(100,20);
        lb_score.setFont(Font.font(24));

        gridSizeSelector.relocate(900, 140);
        addGridSizes();

        lb_changeGridSize.relocate(900,100);
        lb_changeGridSize.setFont(Font.font(24));

        btn_undo.relocate(100,100);
        btn_restart.relocate(775, 100);
        btn_restart.setPrefWidth(75);

        root.setOnKeyPressed(this::gameAction);

        btn_undo.setOnAction(event -> {
            grid.undo();
            updateScoreDisplay();
            root.requestFocus();
        });

        btn_restart.setOnAction(event -> {
            root.requestFocus();

            try {
                grid.restartGame(grid.getGridSize());
                updateScoreDisplay();
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Invalid grid size input").showAndWait();
            }
        });

        stage.setOnCloseRequest(event -> GameStorage.save(grid));

        if (grid.getUndoLimit() == 0) {
            btn_undo.setDisable(true);
        }

        grid.startGame();
        root.requestFocus();
        updateScoreDisplay();
        stage.show();
    }

    /**
     * The method to start the application.
     *
     * @param args Unused
     */
    public static void main(String[] args) {
        launch(args);
    }
}