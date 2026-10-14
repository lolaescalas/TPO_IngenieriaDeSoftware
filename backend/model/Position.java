package backend.model;

public record Position(int row, int column) {

    public static final int SIZE = 8;

    public Position {
        if (!isValid(row, column)) {
            throw new IllegalArgumentException(
                    "Position outside the board: (" + row + ", " + column + ")");
        }
    }

    public static boolean isValid(int row, int column) {
        return row >= 0 && row < SIZE && column >= 0 && column < SIZE;
    }

    public boolean canOffset(int rowDelta, int columnDelta) {
        return isValid(row + rowDelta, column + columnDelta);
    }

    public Position offset(int rowDelta, int columnDelta) {
        return new Position(row + rowDelta, column + columnDelta);
    }

    /** Notación algebraica, útil para debug: fila 6, columna 4 -> "e2". */
    @Override
    public String toString() {
        return "" + (char) ('a' + column) + (SIZE - row);
    }
}