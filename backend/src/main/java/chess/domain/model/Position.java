package chess.domain.model;

/**
 * Casilla del tablero (fila 0 = fila "8", columna 0 = columna "a").
 * <p>
 * Optimización CPU/RAM (Unidad 04): las 64 posiciones posibles se crean una sola vez y
 * {@link #of(int, int)} devuelve siempre la misma instancia (caching / precomputación),
 * evitando crear miles de objetos iguales durante la búsqueda de la IA.
 */
public record Position(int row, int column) {

    public static final int SIZE = 8;

    private static final Position[][] CACHE = new Position[SIZE][SIZE];

    static {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                CACHE[r][c] = new Position(r, c);
            }
        }
    }

    public Position {
        if (!isValid(row, column)) {
            throw new IllegalArgumentException(
                    "Position outside the board: (" + row + ", " + column + ")");
        }
    }

    /** Fábrica con caché: preferirla a {@code new Position(...)}. */
    public static Position of(int row, int column) {
        if (!isValid(row, column)) {
            throw new IllegalArgumentException(
                    "Position outside the board: (" + row + ", " + column + ")");
        }
        return CACHE[row][column];
    }

    /** Convierte notación algebraica ("e2") a posición. */
    public static Position parse(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            throw new IllegalArgumentException("Casilla inválida: " + algebraic);
        }
        int column = Character.toLowerCase(algebraic.charAt(0)) - 'a';
        int rank = algebraic.charAt(1) - '0';
        if (!isValid(SIZE - rank, column)) {
            throw new IllegalArgumentException("Casilla inválida: " + algebraic);
        }
        return of(SIZE - rank, column);
    }

    public static boolean isValid(int row, int column) {
        return row >= 0 && row < SIZE && column >= 0 && column < SIZE;
    }

    public boolean canOffset(int rowDelta, int columnDelta) {
        return isValid(row + rowDelta, column + columnDelta);
    }

    public Position offset(int rowDelta, int columnDelta) {
        return of(row + rowDelta, column + columnDelta);
    }

    /** Notación algebraica, útil para debug: fila 6, columna 4 -> "e2". */
    @Override
    public String toString() {
        return "" + (char) ('a' + column) + (SIZE - row);
    }
}
