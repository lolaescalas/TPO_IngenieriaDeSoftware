package chess.domain.model;

/** Tipos de pieza con su letra en notación algebraica (inglés: N = kNight). */
public enum PieceType {
    PAWN('P'), ROOK('R'), KNIGHT('N'), BISHOP('B'), QUEEN('Q'), KING('K');

    private final char symbol;

    PieceType(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return symbol;
    }

    /** Piezas válidas para coronar un peón (reglamento: Caballo, Alfil, Torre o Dama). */
    public boolean isPromotionTarget() {
        return this == KNIGHT || this == BISHOP || this == ROOK || this == QUEEN;
    }

    public static PieceType fromSymbol(char c) {
        char upper = Character.toUpperCase(c);
        for (PieceType type : values()) {
            if (type.symbol == upper) {
                return type;
            }
        }
        throw new IllegalArgumentException("Pieza desconocida: " + c);
    }
}
