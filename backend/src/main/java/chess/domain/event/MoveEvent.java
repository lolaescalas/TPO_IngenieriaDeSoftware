package chess.domain.event;

import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.Piece;

/**
 * Datos de una jugada ya realizada. {@code board} es una copia del tablero después de la
 * jugada: el listener puede leerla sin riesgo de alterar la partida.
 */
public record MoveEvent(Move move, Color mover, Piece movedPiece, Piece capturedPiece, Board board) {

    public boolean isCapture() {
        return capturedPiece != null;
    }
}
