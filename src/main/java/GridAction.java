/**
 * A class to initiate grid actions involving the movement of the grid numbers.
 *
 * @author Evan Razzaque
 */
public class GridAction {
    /**
     * Left direction
     */
    private static final int[] LEFT = new int[] {0, -1};

    /**
     * Right direction
     */
    private static final int[] RIGHT = new int[] {0, 1};

    /**
     * Up direction
     */
    private static final int[] UP = new int[] {-1, 0};

    /**
     * Down direction
     */
    private static final int[] DOWN = new int[] {1, 0};

    /**
     * A method to move the numbers on the grid instance.
     *
     * @param grid The {@link Grid} instance
     * @param direction The direction to move the numbers in
     */
    public static void moveNumbers(Grid grid, String direction) {
        switch (direction.toLowerCase()) {
            case "left":
                for (int row = 0; row < grid.getGridSize(); row++) {
                    for (int col = 1; col < grid.getGridSize(); col++)
                        grid.moveNumber(row, col, LEFT);
                }
                
                break;
            case "right":
                for (int row = 0; row < grid.getGridSize(); row++) {
                    for (int col = grid.getGridSize() - 2; col > -1; col--)
                        grid.moveNumber(row, col, RIGHT);
                }

                break;
            case "up":
                for (int col = 0; col < grid.getGridSize(); col++) {
                    for (int row = 1; row < grid.getGridSize(); row++)
                        grid.moveNumber(row, col, UP);
                }

                break;
            case "down":
                for (int col = 0; col < grid.getGridSize(); col++) {
                    for (int row = grid.getGridSize() - 2; row > -1; row--)
                        grid.moveNumber(row, col, DOWN);
                }

                break;

            default:
                throw new IllegalArgumentException("Invalid Direction");
        }
    }
}