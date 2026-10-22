package chess.domain.model;

import java.util.Objects;

/**
 * Jugada inmutable: origen, destino, tipo y (si corona) la pieza elegida.
 * Es un "value object": dos jugadas con los mismos datos son iguales.
 */
public record Move(Position from, Position to, MoveType type, PieceType promotion) {

    public Move {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (from.equals(to)) {
            throw new IllegalArgumentException("Origin and destination must be different");
        }
        if ((type == MoveType.PROMOTION) != (promotion != null)) {
            throw new IllegalArgumentException("Promotion piece required only for PROMOTION moves");
        }
        if (promotion != null && !promotion.isPromotionTarget()) {
            throw new IllegalArgumentException("Invalid promotion piece: " + promotion);
        }
    }

    /** Jugada común (sin efectos especiales). */
    public Move(Position from, Position to) {
        this(from, to, MoveType.NORMAL, null);
    }

    public Move(Position from, Position to, MoveType type) {
        this(from, to, type, null);
    }

    public boolean isCastling() {
        return type == MoveType.CASTLE_KING_SIDE || type == MoveType.CASTLE_QUEEN_SIDE;
    }

    /** Notación algebraica larga: "e2e4", "e7e8=Q". */
    @Override
    public String toString() {
        return from.toString() + to + (promotion != null ? "=" + promotion.symbol() : "");
    }
}
