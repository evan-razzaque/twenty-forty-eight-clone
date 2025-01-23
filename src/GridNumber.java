public class GridNumber {
    private long value, oldValue;
    private int row, col, oldRow, oldCol;

    public GridNumber(int row, int col, long value) {
        this.value = value;
        this.row = row;
        this.col = col;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setPos(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getOldRow() {
        return oldRow;
    }

    public int getOldCol() {
        return oldCol;
    }

    public long getOldValue() {
        return oldValue;
    }

    public void setOldPos() {
        oldRow = row;
        oldCol = col;
    }

    public void setOldValue() {
        oldValue = value;
    }

    @Override
    public String toString() {
        return "GridNumber{" +
                "col=" + col +
                ", row=" + row +
                ", oldCol=" + oldCol +
                ", oldRow=" + oldRow +
                ", value=" + value +
                '}';
    }
}