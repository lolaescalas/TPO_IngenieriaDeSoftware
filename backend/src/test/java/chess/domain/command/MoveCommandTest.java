package chess.domain.command;

import chess.domain.factory.BoardFactory;
import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.MoveType;
import chess.domain.model.PieceType;
import chess.domain.model.Position;
import chess.domain.rules.MoveGenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regla de la clase para Command: "estado antes de execute == estado después de undo". */
class MoveCommandTest {

    private static String snapshot(Board board, Color side) {
        return board.positionKey(side) + "|" + board.getHalfmoveClock();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            BoardFactory.STANDARD_FEN,
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
            "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
            "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1"
    })
    void undoRestoresExactlyThePreviousState(String fen) {
        BoardFactory.Setup setup = BoardFactory.fromFen(fen);
        Board board = setup.board();
        String before = snapshot(board, setup.sideToMove());
        for (Move move : new MoveGenerator().legalMoves(board, setup.sideToMove())) {
            MoveCommand command = new MoveCommand(board, move);
            command.execute();
            command.undo();
            assertEquals(before, snapshot(board, setup.sideToMove()), "Falló undo de " + move);
        }
    }

    @Test
    void castlingMovesKingAndRook() {
        Board board = BoardFactory.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1").board();
        new MoveCommand(board, new Move(Position.parse("e1"), Position.parse("g1"), MoveType.CASTLE_KING_SIDE)).execute();
        assertEquals(PieceType.KING, board.getPiece(Position.parse("g1")).getType());
        assertEquals(PieceType.ROOK, board.getPiece(Position.parse("f1")).getType());
        assertNull(board.getPiece(Position.parse("h1")));
        assertTrue(!board.getCastlingRights().canCastle(Color.WHITE, true)
                && !board.getCastlingRights().canCastle(Color.WHITE, false));
    }

    @Test
    void enPassantRemovesTheCapturedPawn() {
        Board board = BoardFactory.fromFen("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1").board();
        MoveCommand command = new MoveCommand(board, new Move(Position.parse("e5"), Position.parse("d6"), MoveType.EN_PASSANT));
        command.execute();
        assertNull(board.getPiece(Position.parse("d5")));
        assertEquals(PieceType.PAWN, command.getCapturedPiece().getType());
    }

    @Test
    void promotionReplacesPawnAndUndoRestoresIt() {
        Board board = BoardFactory.fromFen("4k3/1P6/8/8/8/8/8/4K3 w - - 0 1").board();
        MoveCommand command = new MoveCommand(board,
                new Move(Position.parse("b7"), Position.parse("b8"), MoveType.PROMOTION, PieceType.KNIGHT));
        command.execute();
        assertEquals(PieceType.KNIGHT, board.getPiece(Position.parse("b8")).getType());
        command.undo();
        assertEquals(PieceType.PAWN, board.getPiece(Position.parse("b7")).getType());
    }

    @Test
    void historyUndoRedo() {
        Board board = BoardFactory.standard().board();
        CommandHistory<MoveCommand> history = new CommandHistory<>();
        history.executeAndRecord(new MoveCommand(board, new Move(Position.parse("e2"), Position.parse("e4"), MoveType.PAWN_DOUBLE_STEP)));
        history.undo();
        assertNull(board.getPiece(Position.parse("e4")));
        history.redo();
        assertEquals(PieceType.PAWN, board.getPiece(Position.parse("e4")).getType());
        assertEquals(Position.parse("e3"), board.getEnPassantTarget().orElseThrow());
    }
}
