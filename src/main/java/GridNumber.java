/**
 * Represents a number on a grid with its current and previous state.
 *
 * @author Evan Razzaque
 */
public class GridNumber {
    /**
     * The value of the number
     */
    private long value;

    /**
     * The previous value of the number
     */
    private long oldValue;

    /**
     * The number's row on the grid
     */
    private int row;

    /**
     * The number's column on the grid
     */
    private int col;

    /**
     * The number's previous row on the grid
     */
    private int oldRow;

    /**
     * The number's column on the grid
     */
    private int oldCol;

    /**
     * A constructor for a GridNumber.
     *
     * @param row The number's row on the grid
     * @param col The number's column on the grid
     * @param value The value of the number
     */
    public GridNumber(int row, int col, long value) {
        this.value = value;
        this.row = row;
        this.col = col;
    }

    /**
     * Sets the value of the number.
     *
     * @param value the value to set the number to
     */
    public void setValue(long value) {
        this.value = value;
    }

    /**
     * Gets the number's row on the grid.
     *
     * @return the number's row
     */
    public int getRow() {
        return row;
    }

    /**
     * Gets the number's column on the grid.
     *
     * @return the number's column
     */
    public int getCol() {
        return col;
    }

    /**
     * Sets the position of the number.
     *
     * @param row Grid row
     * @param col Grid column
     */
    public void setPos(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Gets the number's previous row on the grid.
     *
     * @return the number's previous row
     */
    public int getOldRow() {
        return oldRow;
    }

    /**
     * Gets the number's previous column on the grid.
     *
     * @return the number's previous row
     */
    public int getOldCol() {
        return oldCol;
    }

    /**
     * Gets the number's previous value.
     *
     * @return the previous value of the number.
     */
    public long getOldValue() {
        return oldValue;
    }

    /**
     * Sets the number's previous position to the number's current position.
     */
    public void setOldPos() {
        oldRow = row;
        oldCol = col;
    }

    /**
     * Sets the number's previous value to the number's current value.
     */
    public void setOldValue() {
        oldValue = value;
    }
}