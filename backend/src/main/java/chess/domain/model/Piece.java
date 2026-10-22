package chess.domain.model;

import chess.domain.movement.MovementPattern;

import java.util.Objects;

/**
 * Pieza de ajedrez.
 * <p>
 * Composición sobre herencia (Unidad 01): en lugar de 6 subclases (Rook, Bishop, ...),
 * una pieza es un color + un tipo + un {@link MovementPattern} (Strategy) que sabe cómo
 * se mueve. La Dama, por ejemplo, se arma componiendo el patrón de la Torre y el del Alfil.
 * Las piezas se crean con {@code PieceFactory} (Factory).
 * <p>
 * Es inmutable: si una pieza "se movió" o no lo registra el tablero (derechos de enroque),
 * así una misma instancia se puede compartir.
 */
public final class Piece {

    private final Color color;
    private final PieceType type;
    private final MovementPattern movement;

    public Piece(Color color, PieceType type, MovementPattern movement) {
        this.color = Objects.requireNonNull(color);
        this.type = Objects.requireNonNull(type);
        this.movement = Objects.requireNonNull(movement);
    }

    public Color getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }

    public MovementPattern getMovement() {
        return movement;
    }

    /** Letra FEN: mayúscula = blancas, minúscula = negras. */
    public char symbol() {
        return color == Color.WHITE ? type.symbol() : Character.toLowerCase(type.symbol());
    }

    @Override
    public String toString() {
        return String.valueOf(symbol());
    }
}
