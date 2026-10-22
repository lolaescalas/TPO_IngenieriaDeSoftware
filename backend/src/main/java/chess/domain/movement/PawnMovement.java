package chess.domain.movement;

import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.MoveType;
import chess.domain.model.PieceType;
import chess.domain.model.Position;

import java.util.List;

/**
 * Peón: la pieza con más casos especiales.
 * <ul>
 *   <li>Avanza 1 (o 2 si está en su fila inicial) sin capturar.</li>
 *   <li>Captura en diagonal hacia adelante.</li>
 *   <li>Captura al paso sobre la casilla {@link Board#getEnPassantTarget()}.</li>
 *   <li>Al llegar a la última fila corona (genera una jugada por cada pieza posible).</li>
 * </ul>
 */
public final class PawnMovement implements MovementPattern {

    private static final PieceType[] PROMOTIONS = {
            PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT};

    @Override
    public void generate(Board board, Position from, Color color, List<Move> out) {
        int dir = color.pawnDirection();

        // Avance simple y doble
        if (from.canOffset(dir, 0)) {
            Position one = from.offset(dir, 0);
            if (board.isEmpty(one)) {
                addAdvance(from, one, color, out);
                if (from.row() == color.pawnStartRow()) {
                    Position two = from.offset(2 * dir, 0);
                    if (board.isEmpty(two)) {
                        out.add(new Move(from, two, MoveType.PAWN_DOUBLE_STEP));
                    }
                }
            }
        }

        // Capturas en diagonal (incluye captura al paso)
        for (int dc : new int[]{-1, 1}) {
            if (!from.canOffset(dir, dc)) {
                continue;
            }
            Position diag = from.offset(dir, dc);
            if (board.hasEnemyPiece(diag, color)) {
                addAdvance(from, diag, color, out);
            } else if (board.getEnPassantTarget().filter(diag::equals).isPresent()) {
                out.add(new Move(from, diag, MoveType.EN_PASSANT));
            }
        }
    }

    private static void addAdvance(Position from, Position to, Color color, List<Move> out) {
        if (to.row() == color.promotionRow()) {
            for (PieceType type : PROMOTIONS) {
                out.add(new Move(from, to, MoveType.PROMOTION, type));
            }
        } else {
            out.add(new Move(from, to));
        }
    }

    /** El peón no ataca como se mueve: solo las dos diagonales de adelante. */
    @Override
    public boolean attacks(Board board, Position from, Position target, Color color) {
        return target.row() - from.row() == color.pawnDirection()
                && Math.abs(target.column() - from.column()) == 1;
    }
}
