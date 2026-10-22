package chess.application;

import chess.domain.game.Game;
import chess.domain.game.IllegalMoveException;
import chess.domain.model.Move;
import chess.domain.model.MoveType;
import chess.domain.model.PieceType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoveParserTest {

    private final MoveParser parser = new MoveParser();

    @ParameterizedTest
    @ValueSource(strings = {"e2 e4", "e2e4", "E2-E4", "  e2   e4 "})
    void acceptsSeveralFormats(String input) {
        List<Move> legal = Game.standard().legalMoves();
        assertEquals("e2e4", parser.parse(input, legal).toString());
    }

    @Test
    void parsesCastling() {
        List<Move> legal = Game.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1").legalMoves();
        assertEquals(MoveType.CASTLE_KING_SIDE, parser.parse("O-O", legal).type());
        assertEquals(MoveType.CASTLE_QUEEN_SIDE, parser.parse("0-0-0", legal).type());
        assertEquals(MoveType.CASTLE_KING_SIDE, parser.parse("e1 g1", legal).type());
    }

    @Test
    void promotionDefaultsToQueen() {
        List<Move> legal = Game.fromFen("4k3/1P6/8/8/8/8/8/4K3 w - - 0 1").legalMoves();
        assertEquals(PieceType.QUEEN, parser.parse("b7 b8", legal).promotion());
        assertEquals(PieceType.ROOK, parser.parse("b7b8=R", legal).promotion());
        assertEquals(PieceType.BISHOP, parser.parse("b7 b8 A", legal).promotion());
    }

    @Test
    void rejectsGarbageAndIllegalMoves() {
        List<Move> legal = Game.standard().legalMoves();
        assertThrows(IllegalMoveException.class, () -> parser.parse("hola", legal));
        assertThrows(IllegalMoveException.class, () -> parser.parse("z9 e4", legal));
        assertThrows(IllegalMoveException.class, () -> parser.parse("e2 e5", legal));
        assertThrows(IllegalMoveException.class, () -> parser.parse("O-O", legal));
    }
}
