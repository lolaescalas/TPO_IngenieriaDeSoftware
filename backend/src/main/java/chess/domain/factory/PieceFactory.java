package chess.domain.factory;

import chess.domain.model.Color;
import chess.domain.model.Piece;
import chess.domain.model.PieceType;
import chess.domain.movement.CompositeMovement;
import chess.domain.movement.JumpMovement;
import chess.domain.movement.MovementPattern;
import chess.domain.movement.PawnMovement;
import chess.domain.movement.SlidingMovement;

import java.util.EnumMap;
import java.util.Map;

/**
 * Patrón Factory (Unidad 00/01): único lugar donde se decide qué {@link MovementPattern}
 * corresponde a cada tipo de pieza. Lo usan el armado del tablero y la coronación.
 * <p>
 * Como las piezas son inmutables, se crean las 12 combinaciones (6 tipos x 2 colores)
 * una sola vez y se reutilizan (caching, Unidad 04).
 */
public final class PieceFactory {

    private static final Map<Color, Map<PieceType, Piece>> PIECES = new EnumMap<>(Color.class);

    static {
        MovementPattern rook = new SlidingMovement(SlidingMovement.ORTHOGONAL);
        MovementPattern bishop = new SlidingMovement(SlidingMovement.DIAGONAL);
        Map<PieceType, MovementPattern> patterns = new EnumMap<>(PieceType.class);
        patterns.put(PieceType.PAWN, new PawnMovement());
        patterns.put(PieceType.ROOK, rook);
        patterns.put(PieceType.BISHOP, bishop);
        patterns.put(PieceType.QUEEN, new CompositeMovement(rook, bishop));
        patterns.put(PieceType.KNIGHT, new JumpMovement(JumpMovement.KNIGHT_OFFSETS));
        patterns.put(PieceType.KING, new JumpMovement(JumpMovement.KING_OFFSETS));

        for (Color color : Color.values()) {
            Map<PieceType, Piece> byType = new EnumMap<>(PieceType.class);
            for (PieceType type : PieceType.values()) {
                byType.put(type, new Piece(color, type, patterns.get(type)));
            }
            PIECES.put(color, byType);
        }
    }

    private PieceFactory() {
    }

    public static Piece create(PieceType type, Color color) {
        return PIECES.get(color).get(type);
    }

    /** Desde una letra FEN: 'K' = rey blanco, 'q' = dama negra, etc. */
    public static Piece fromSymbol(char symbol) {
        Color color = Character.isUpperCase(symbol) ? Color.WHITE : Color.BLACK;
        return create(PieceType.fromSymbol(symbol), color);
    }
}
