package chess.domain.movement;

import chess.domain.factory.BoardFactory;
import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.Position;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests unitarios por pieza (base de la pirámide): cada MovementPattern aislado. */
class PieceMovementTest {

    private static Set<String> targets(String fen, String square) {
        Board board = BoardFactory.fromFen(fen).board();
        Position from = Position.parse(square);
        List<Move> moves = new ArrayList<>();
        board.getPiece(from).getMovement().generate(board, from, board.getPiece(from).getColor(), moves);
        return moves.stream().map(m -> m.to().toString()).collect(Collectors.toSet());
    }

    @Test
    void rookSlidesUntilBlockedAndCapturesEnemy() {
        // Torre en d4, peón propio en d6, peón rival en f4
        Set<String> t = targets("4k3/8/3P4/8/3R1p2/8/8/4K3 w - - 0 1", "d4");
        assertEquals(Set.of("d5", "d3", "d2", "d1", "a4", "b4", "c4", "e4", "f4"), t);
    }

    @Test
    void bishopMovesDiagonally() {
        Set<String> t = targets("4k3/8/8/8/3B4/8/8/4K3 w - - 0 1", "d4");
        assertEquals(13, t.size());
        assertTrue(t.containsAll(Set.of("a1", "h8", "a7", "g1")));
    }

    @Test
    void queenIsRookPlusBishop() {
        Set<String> t = targets("4k3/8/8/8/3Q4/8/8/7K w - - 0 1", "d4");
        assertEquals(27, t.size()); // 14 de torre + 13 de alfil
    }

    @Test
    void knightJumpsOverPieces() {
        Set<String> t = targets(BoardFactory.STANDARD_FEN, "g1");
        assertEquals(Set.of("f3", "h3"), t);
    }

    @Test
    void kingMovesOneSquare() {
        Set<String> t = targets("4k3/8/8/8/8/8/8/K7 w - - 0 1", "a1");
        assertEquals(Set.of("a2", "b1", "b2"), t);
    }

    @Test
    void pawnAdvancesOneOrTwoFromStart() {
        assertEquals(Set.of("e3", "e4"), targets(BoardFactory.STANDARD_FEN, "e2"));
        assertEquals(Set.of("e6", "e5"), targets(BoardFactory.STANDARD_FEN.replace(" w ", " b "), "e7"));
    }

    @Test
    void pawnCapturesDiagonallyButNotForward() {
        // Peón e4, bloqueado por e5, con piezas rivales en d5 y f5
        Set<String> t = targets("4k3/8/8/3ppp2/4P3/8/8/4K3 w - - 0 1", "e4");
        assertEquals(Set.of("d5", "f5"), t);
    }

    @Test
    void pawnAttacksOnlyForwardDiagonals() {
        Board board = BoardFactory.fromFen("4k3/8/8/8/4P3/8/8/4K3 w - - 0 1").board();
        Position e4 = Position.parse("e4");
        PawnMovement pawn = new PawnMovement();
        assertTrue(pawn.attacks(board, e4, Position.parse("d5"), Color.WHITE));
        assertTrue(pawn.attacks(board, e4, Position.parse("f5"), Color.WHITE));
        assertFalse(pawn.attacks(board, e4, Position.parse("e5"), Color.WHITE));
        assertFalse(pawn.attacks(board, e4, Position.parse("d3"), Color.WHITE));
    }
}
