package chess.domain.factory;

import chess.domain.model.Board;
import chess.domain.model.CastlingRights;
import chess.domain.model.Color;
import chess.domain.model.Position;

/**
 * Factory de tableros: posición inicial del reglamento o cualquier posición en notación
 * FEN (Forsyth-Edwards Notation), muy útil para los tests ("armar" un jaque mate directo).
 * <p>
 * FEN de ejemplo (posición inicial):
 * {@code rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1}
 * (piezas por fila desde la 8, turno, enroques, casilla al paso, medias jugadas, nº de jugada).
 */
public final class BoardFactory {

    public static final String STANDARD_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private BoardFactory() {
    }

    /** Resultado de leer un FEN: el tablero y a quién le toca mover. */
    public record Setup(Board board, Color sideToMove) {
    }

    public static Setup standard() {
        return fromFen(STANDARD_FEN);
    }

    public static Setup fromFen(String fen) {
        String[] parts = fen.trim().split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("FEN inválido: " + fen);
        }
        Board board = new Board();
        String[] rows = parts[0].split("/");
        if (rows.length != Position.SIZE) {
            throw new IllegalArgumentException("FEN inválido (filas): " + fen);
        }
        for (int r = 0; r < Position.SIZE; r++) {
            int c = 0;
            for (char ch : rows[r].toCharArray()) {
                if (Character.isDigit(ch)) {
                    c += ch - '0';
                } else {
                    board.setPiece(Position.of(r, c), PieceFactory.fromSymbol(ch));
                    c++;
                }
            }
            if (c != Position.SIZE) {
                throw new IllegalArgumentException("FEN inválido (columnas en fila " + (8 - r) + "): " + fen);
            }
        }
        Color side = parts[1].equals("w") ? Color.WHITE : Color.BLACK;

        String castling = parts.length > 2 ? parts[2] : "-";
        board.setCastlingRights(new CastlingRights(
                castling.contains("K"), castling.contains("Q"),
                castling.contains("k"), castling.contains("q")));

        if (parts.length > 3 && !parts[3].equals("-")) {
            board.setEnPassantTarget(Position.parse(parts[3]));
        }
        if (parts.length > 4) {
            board.setHalfmoveClock(Integer.parseInt(parts[4]));
        }
        return new Setup(board, side);
    }
}
