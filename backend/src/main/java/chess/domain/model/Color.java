package chess.domain.model;

/**
 * Color de un ejército.
 * <p>
 * Convención del tablero: la fila 0 es la fila 8 del ajedrez (lado de las negras),
 * por eso las blancas avanzan hacia filas menores (dirección -1).
 */
public enum Color {
    WHITE, BLACK;

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    /** Hacia dónde avanza un peón de este color (delta de fila). */
    public int pawnDirection() {
        return this == WHITE ? -1 : 1;
    }

    /** Fila inicial de los peones (fila 2 del jugador). */
    public int pawnStartRow() {
        return this == WHITE ? 6 : 1;
    }

    /** Fila de coronación (última fila del jugador). */
    public int promotionRow() {
        return this == WHITE ? 0 : 7;
    }

    /** Primera fila del jugador (donde arrancan rey y torres). */
    public int backRow() {
        return this == WHITE ? 7 : 0;
    }
}
