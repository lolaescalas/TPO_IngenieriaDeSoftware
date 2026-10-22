package chess.domain.rules;

import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Piece;

import java.util.Optional;

/**
 * Detecta los finales automáticos del reglamento después de cada jugada:
 * jaque mate, ahogado, material insuficiente y regla de 50 jugadas.
 * (La triple repetición necesita el historial, la lleva {@code Game}; acuerdo y
 * rendición son decisiones de los jugadores.)
 */
public final class GameEndDetector {

    /** 50 jugadas de cada bando = 100 medias jugadas. */
    public static final int FIFTY_MOVE_HALFMOVES = 100;

    private final MoveGenerator moveGenerator;

    public GameEndDetector(MoveGenerator moveGenerator) {
        this.moveGenerator = moveGenerator;
    }

    /** Evalúa la posición cuando le toca mover a {@code sideToMove}. */
    public Optional<GameResult> evaluate(Board board, Color sideToMove) {
        if (!moveGenerator.hasAnyLegalMove(board, sideToMove)) {
            if (moveGenerator.checkDetector().isInCheck(board, sideToMove)) {
                return Optional.of(GameResult.win(sideToMove.opposite(), GameResult.Reason.CHECKMATE));
            }
            return Optional.of(GameResult.draw(GameResult.Reason.STALEMATE));
        }
        if (isInsufficientMaterial(board)) {
            return Optional.of(GameResult.draw(GameResult.Reason.INSUFFICIENT_MATERIAL));
        }
        if (board.getHalfmoveClock() >= FIFTY_MOVE_HALFMOVES) {
            return Optional.of(GameResult.draw(GameResult.Reason.FIFTY_MOVE_RULE));
        }
        return Optional.empty();
    }

    /**
     * Reglamento: sin peones, y solo Rey contra Rey, o Rey contra Rey y Caballo/Alfil.
     */
    public boolean isInsufficientMaterial(Board board) {
        int minors = 0;
        for (Piece p : board.allPieces()) {
            switch (p.getType()) {
                case KING -> { }
                case KNIGHT, BISHOP -> minors++;
                default -> {
                    return false; // peón, torre o dama: hay material para mate
                }
            }
        }
        return minors <= 1;
    }
}
