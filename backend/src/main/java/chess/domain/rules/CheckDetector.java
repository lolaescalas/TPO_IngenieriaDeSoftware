package chess.domain.rules;

import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Piece;
import chess.domain.model.Position;

/** SRP: única responsabilidad, saber si una casilla está atacada / si un rey está en jaque. */
public final class CheckDetector {

    /** ¿Alguna pieza de {@code attacker} ataca {@code square}? */
    public boolean isSquareAttacked(Board board, Position square, Color attacker) {
        for (Position from : board.positionsOf(attacker)) {
            Piece piece = board.getPiece(from);
            if (piece.getMovement().attacks(board, from, square, attacker)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInCheck(Board board, Color color) {
        return board.findKing(color)
                .map(king -> isSquareAttacked(board, king, color.opposite()))
                .orElse(false);
    }
}
