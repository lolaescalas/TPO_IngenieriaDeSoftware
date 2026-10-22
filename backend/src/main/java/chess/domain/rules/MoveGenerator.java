package chess.domain.rules;

import chess.domain.command.MoveCommand;
import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.MoveType;
import chess.domain.model.Piece;
import chess.domain.model.PieceType;
import chess.domain.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Genera las jugadas legales de un color.
 * <p>
 * SRP: no sabe dibujar, ni de turnos, ni de fin de partida. Depende de cada
 * {@code MovementPattern} (Strategy) para el desplazamiento de las piezas y agrega lo que
 * ninguna pieza puede saber sola: el enroque (requiere casillas atacadas) y la máxima del
 * reglamento "no se puede dejar al propio rey en jaque".
 */
public final class MoveGenerator {

    private final CheckDetector checkDetector;

    public MoveGenerator(CheckDetector checkDetector) {
        this.checkDetector = checkDetector;
    }

    public MoveGenerator() {
        this(new CheckDetector());
    }

    public CheckDetector checkDetector() {
        return checkDetector;
    }

    /** Jugadas que respetan el desplazamiento de cada pieza (pueden dejar al rey en jaque). */
    public List<Move> pseudoLegalMoves(Board board, Color color) {
        List<Move> moves = new ArrayList<>(48);
        for (Position from : board.positionsOf(color)) {
            Piece piece = board.getPiece(from);
            piece.getMovement().generate(board, from, color, moves);
        }
        addCastlingMoves(board, color, moves);
        return moves;
    }

    /**
     * Jugadas legales: se filtra cada pseudo-legal haciendo la jugada con un
     * {@link MoveCommand}, mirando si el rey propio quedó en jaque y deshaciéndola.
     */
    public List<Move> legalMoves(Board board, Color color) {
        List<Move> legal = new ArrayList<>(48);
        for (Move move : pseudoLegalMoves(board, color)) {
            if (isLegal(board, move, color)) {
                legal.add(move);
            }
        }
        return legal;
    }

    public List<Move> legalMovesFrom(Board board, Position from) {
        Piece piece = board.getPiece(from);
        if (piece == null) {
            return List.of();
        }
        List<Move> result = new ArrayList<>();
        for (Move move : legalMoves(board, piece.getColor())) {
            if (move.from().equals(from)) {
                result.add(move);
            }
        }
        return result;
    }

    public boolean hasAnyLegalMove(Board board, Color color) {
        for (Move move : pseudoLegalMoves(board, color)) {
            if (isLegal(board, move, color)) {
                return true; // cortocircuito: no hace falta generar todas (Unidad 04)
            }
        }
        return false;
    }

    private boolean isLegal(Board board, Move move, Color color) {
        MoveCommand command = new MoveCommand(board, move);
        command.execute();
        boolean leavesKingInCheck = checkDetector.isInCheck(board, color);
        command.undo();
        return !leavesKingInCheck;
    }

    /**
     * Enroque (reglamento): primer movimiento de rey y torre (derechos), sin piezas en el
     * medio, y el rey no puede estar en jaque ni pasar por / llegar a una casilla atacada.
     */
    private void addCastlingMoves(Board board, Color color, List<Move> out) {
        int row = color.backRow();
        Position kingFrom = Position.of(row, 4);
        Piece king = board.getPiece(kingFrom);
        if (king == null || king.getType() != PieceType.KING || king.getColor() != color) {
            return;
        }
        Color enemy = color.opposite();
        boolean inCheck = false;
        boolean checked = false;

        for (boolean kingSide : new boolean[]{true, false}) {
            if (!board.getCastlingRights().canCastle(color, kingSide)) {
                continue;
            }
            Position rookFrom = Position.of(row, kingSide ? 7 : 0);
            Piece rook = board.getPiece(rookFrom);
            if (rook == null || rook.getType() != PieceType.ROOK || rook.getColor() != color) {
                continue;
            }
            int[] between = kingSide ? new int[]{5, 6} : new int[]{1, 2, 3};
            boolean clear = true;
            for (int c : between) {
                if (!board.isEmpty(Position.of(row, c))) {
                    clear = false;
                    break;
                }
            }
            if (!clear) {
                continue;
            }
            if (!checked) {
                inCheck = checkDetector.isSquareAttacked(board, kingFrom, enemy);
                checked = true;
            }
            if (inCheck) {
                return;
            }
            Position passing = Position.of(row, kingSide ? 5 : 3);
            Position kingTo = Position.of(row, kingSide ? 6 : 2);
            if (checkDetector.isSquareAttacked(board, passing, enemy)
                    || checkDetector.isSquareAttacked(board, kingTo, enemy)) {
                continue;
            }
            out.add(new Move(kingFrom, kingTo,
                    kingSide ? MoveType.CASTLE_KING_SIDE : MoveType.CASTLE_QUEEN_SIDE));
        }
    }
}
