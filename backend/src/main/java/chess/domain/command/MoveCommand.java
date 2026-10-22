package chess.domain.command;

import chess.domain.factory.PieceFactory;
import chess.domain.model.Board;
import chess.domain.model.CastlingRights;
import chess.domain.model.Move;
import chess.domain.model.MoveType;
import chess.domain.model.Piece;
import chess.domain.model.PieceType;
import chess.domain.model.Position;

import java.util.Objects;

/**
 * Command que aplica una jugada sobre el tablero y sabe revertirla.
 * <p>
 * Guarda todo lo necesario para el {@link #undo()}: pieza movida, pieza capturada (y dónde
 * estaba, porque en la captura al paso no coincide con el destino), derechos de enroque,
 * casilla al paso y contador de 50 jugadas anteriores.
 * <p>
 * Se usa en tres lugares: la partida real (deshacer/rehacer), el filtro de jugadas legales
 * ("hago la jugada, miro si mi rey quedó en jaque, la deshago") y la búsqueda de la IA
 * (make/unmake sin copiar el tablero, Unidad 04 RAM).
 * <p>
 * No valida legalidad: asume que la jugada viene de {@code MoveGenerator}.
 */
public final class MoveCommand implements Command {

    private final Board board;
    private final Move move;

    private Piece movedPiece;
    private Piece capturedPiece;
    private Position capturedAt;
    private CastlingRights previousCastling;
    private Position previousEnPassant;
    private int previousHalfmoveClock;
    private boolean executed;

    public MoveCommand(Board board, Move move) {
        this.board = Objects.requireNonNull(board);
        this.move = Objects.requireNonNull(move);
    }

    public Move getMove() {
        return move;
    }

    /** Pieza capturada por la jugada (null si no capturó). Disponible después de execute. */
    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public Piece getMovedPiece() {
        return movedPiece;
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command already executed: " + move);
        }
        Position from = move.from();
        Position to = move.to();
        movedPiece = board.getPiece(from);
        if (movedPiece == null) {
            throw new IllegalStateException("No piece at " + from);
        }

        // 1) Guardar el estado previo (memento mínimo para el undo)
        previousCastling = board.getCastlingRights();
        previousEnPassant = board.getEnPassantTarget().orElse(null);
        previousHalfmoveClock = board.getHalfmoveClock();

        // 2) Captura (en la captura al paso la pieza capturada está "al lado", no en el destino)
        capturedAt = move.type() == MoveType.EN_PASSANT ? Position.of(from.row(), to.column()) : to;
        capturedPiece = board.getPiece(capturedAt);
        board.removePiece(capturedAt);

        // 3) Mover (o coronar)
        board.removePiece(from);
        Piece placed = move.type() == MoveType.PROMOTION
                ? PieceFactory.create(move.promotion(), movedPiece.getColor())
                : movedPiece;
        board.setPiece(to, placed);

        // 4) Enroque: además del rey se mueve la torre
        if (move.isCastling()) {
            int row = from.row();
            boolean kingSide = move.type() == MoveType.CASTLE_KING_SIDE;
            Position rookFrom = Position.of(row, kingSide ? 7 : 0);
            Position rookTo = Position.of(row, kingSide ? 5 : 3);
            board.setPiece(rookTo, board.getPiece(rookFrom));
            board.removePiece(rookFrom);
        }

        // 5) Actualizar derechos y contadores
        CastlingRights rights = previousCastling;
        if (movedPiece.getType() == PieceType.KING) {
            rights = rights.withoutKing(movedPiece.getColor());
        }
        rights = rights.withoutRookAt(from).withoutRookAt(to); // torre movida o capturada
        board.setCastlingRights(rights);

        board.setEnPassantTarget(move.type() == MoveType.PAWN_DOUBLE_STEP
                ? Position.of((from.row() + to.row()) / 2, from.column())
                : null);

        boolean resetsClock = movedPiece.getType() == PieceType.PAWN || capturedPiece != null;
        board.setHalfmoveClock(resetsClock ? 0 : previousHalfmoveClock + 1);

        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException("Command not executed: " + move);
        }
        Position from = move.from();
        Position to = move.to();

        if (move.isCastling()) {
            int row = from.row();
            boolean kingSide = move.type() == MoveType.CASTLE_KING_SIDE;
            Position rookFrom = Position.of(row, kingSide ? 7 : 0);
            Position rookTo = Position.of(row, kingSide ? 5 : 3);
            board.setPiece(rookFrom, board.getPiece(rookTo));
            board.removePiece(rookTo);
        }

        board.removePiece(to);
        board.setPiece(from, movedPiece); // si coronó, vuelve a ser peón
        if (capturedPiece != null) {
            board.setPiece(capturedAt, capturedPiece);
        }

        board.setCastlingRights(previousCastling);
        board.setEnPassantTarget(previousEnPassant);
        board.setHalfmoveClock(previousHalfmoveClock);
        executed = false;
    }
}
