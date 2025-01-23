public class GridAction {
    private static final int[] LEFT = new int[] {0, -1};
    private static final int[] RIGHT = new int[] {0, 1};
    private static final int[] UP = new int[] {-1, 0};
    private static final int[] DOWN = new int[] {1, 0};

    public static void moveNumbers(Grid grid, String direction) {
        switch (direction) {
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