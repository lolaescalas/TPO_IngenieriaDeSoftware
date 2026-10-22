package chess.application;

import chess.domain.game.IllegalMoveException;
import chess.domain.model.Move;
import chess.domain.model.PieceType;
import chess.domain.model.Position;

import java.util.List;
import java.util.Locale;

/**
 * Traduce texto del usuario a una jugada legal. Formatos aceptados:
 * <ul>
 *   <li>{@code e2 e4} o {@code e2e4}</li>
 *   <li>{@code e7 e8 Q} / {@code e7e8=Q} (coronación; sin letra se corona Dama)</li>
 *   <li>{@code O-O} / {@code 0-0} (enroque corto), {@code O-O-O} (largo); también {@code e1 g1}</li>
 * </ul>
 * La letra de coronación acepta inglés (Q R B N) o español (D T A C).
 */
public final class MoveParser {

    public Move parse(String input, List<Move> legalMoves) {
        String text = input.trim().toUpperCase(Locale.ROOT).replace('0', 'O');
        if (text.equals("O-O") || text.equals("O-O-O")) {
            boolean kingSide = text.equals("O-O");
            return legalMoves.stream()
                    .filter(m -> m.isCastling() && (m.to().column() == 6) == kingSide)
                    .findFirst()
                    .orElseThrow(() -> new IllegalMoveException("No se puede enrocar ahora"));
        }

        String compact = text.replaceAll("[\\s=\\-]", "");
        if (compact.length() < 4 || compact.length() > 5) {
            throw new IllegalMoveException("Formato inválido: '" + input + "'. Ejemplo: e2 e4");
        }
        Position from;
        Position to;
        try {
            from = Position.parse(compact.substring(0, 2));
            to = Position.parse(compact.substring(2, 4));
        } catch (IllegalArgumentException e) {
            throw new IllegalMoveException("Casilla inválida en '" + input + "'");
        }
        PieceType promotion = compact.length() == 5 ? promotionPiece(compact.charAt(4)) : PieceType.QUEEN;

        for (Move m : legalMoves) {
            if (m.from().equals(from) && m.to().equals(to)
                    && (m.promotion() == null || m.promotion() == promotion)) {
                return m;
            }
        }
        throw new IllegalMoveException("Jugada ilegal: " + from + " -> " + to);
    }

    private static PieceType promotionPiece(char c) {
        return switch (c) {
            case 'Q', 'D' -> PieceType.QUEEN;
            case 'R', 'T' -> PieceType.ROOK;
            case 'B', 'A' -> PieceType.BISHOP;
            case 'N', 'C' -> PieceType.KNIGHT;
            default -> throw new IllegalMoveException("Pieza de coronación inválida: " + c);
        };
    }
}
