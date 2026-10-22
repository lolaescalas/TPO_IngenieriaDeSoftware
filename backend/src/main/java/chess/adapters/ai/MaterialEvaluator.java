package chess.adapters.ai;

import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Piece;
import chess.domain.model.PieceType;
import chess.domain.model.Position;

/**
 * Evaluación estática de una posición en "centipeones" (100 = un peón) desde el punto de
 * vista de {@code color}: material + un pequeño bonus por controlar el centro.
 */
public final class MaterialEvaluator {

    public static int value(PieceType type) {
        return switch (type) {
            case PAWN -> 100;
            case KNIGHT -> 320;
            case BISHOP -> 330;
            case ROOK -> 500;
            case QUEEN -> 900;
            case KING -> 0; // nunca se captura: el mate se evalúa aparte
        };
    }

    public int evaluate(Board board, Color color) {
        int score = 0;
        for (int r = 0; r < Position.SIZE; r++) {
            for (int c = 0; c < Position.SIZE; c++) {
                Piece p = board.getPiece(Position.of(r, c));
                if (p == null) {
                    continue;
                }
                int pieceScore = value(p.getType()) + positionalBonus(p, r, c);
                score += p.getColor() == color ? pieceScore : -pieceScore;
            }
        }
        return score;
    }

    /** Caballos, alfiles y peones valen un poco más en el centro (máx. ~+12). */
    private static int positionalBonus(Piece p, int row, int col) {
        if (p.getType() != PieceType.KNIGHT && p.getType() != PieceType.BISHOP && p.getType() != PieceType.PAWN) {
            return 0;
        }
        int distance = Math.max(Math.abs(2 * row - 7), Math.abs(2 * col - 7)); // 1 (centro) .. 7 (borde)
        return (7 - distance) * 2;
    }
}
