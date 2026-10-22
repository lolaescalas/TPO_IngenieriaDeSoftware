package chess.domain.rules;

import chess.domain.factory.BoardFactory;
import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.MoveType;
import chess.domain.model.PieceType;
import chess.domain.model.Position;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reglas del reglamento: jaque, enroque, captura al paso y coronación. */
class SpecialMovesTest {

    private final MoveGenerator generator = new MoveGenerator();

    private List<Move> legal(String fen) {
        BoardFactory.Setup s = BoardFactory.fromFen(fen);
        return generator.legalMoves(s.board(), s.sideToMove());
    }

    private static boolean has(List<Move> moves, MoveType type) {
        return moves.stream().anyMatch(m -> m.type() == type);
    }

    @Test
    void cannotLeaveOwnKingInCheck() {
        // Alfil blanco en e2 clavado por la torre negra en e8: no puede moverse
        List<Move> moves = legal("4r1k1/8/8/8/8/8/4B3/4K3 w - - 0 1");
        assertTrue(moves.stream().noneMatch(m -> m.from().equals(Position.parse("e2"))));
    }

    @Test
    void kingInCheckMustResolveIt() {
        // Rey en e1 en jaque por torre en e8: solo jugadas de rey fuera de la columna e
        List<Move> moves = legal("4r1k1/8/8/8/8/8/8/R3K3 w - - 0 1");
        assertTrue(moves.stream().allMatch(m -> m.from().equals(Position.parse("e1"))));
        assertTrue(moves.stream().noneMatch(m -> m.to().column() == 4));
    }

    @Test
    void castlingBothSidesWhenPathIsFree() {
        List<Move> moves = legal("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        assertTrue(has(moves, MoveType.CASTLE_KING_SIDE));
        assertTrue(has(moves, MoveType.CASTLE_QUEEN_SIDE));
    }

    @Test
    void noCastlingWithoutRights() {
        List<Move> moves = legal("r3k2r/8/8/8/8/8/8/R3K2R w - - 0 1");
        assertFalse(moves.stream().anyMatch(Move::isCastling));
    }

    @Test
    void noCastlingWhenPieceInBetween() {
        List<Move> moves = legal("r3k2r/8/8/8/8/8/8/RN2K1NR w KQkq - 0 1");
        assertFalse(moves.stream().anyMatch(Move::isCastling));
    }

    @Test
    void noCastlingOutOfCheck() {
        List<Move> moves = legal("4r1k1/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        assertFalse(moves.stream().anyMatch(Move::isCastling));
    }

    @Test
    void noCastlingThroughAttackedSquare() {
        // Torre negra en f8 ataca f1 (casilla por la que pasa el rey en el enroque corto)
        List<Move> moves = legal("5rk1/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        assertFalse(has(moves, MoveType.CASTLE_KING_SIDE));
        assertTrue(has(moves, MoveType.CASTLE_QUEEN_SIDE));
    }

    @Test
    void queenSideCastlingAllowedEvenIfRookSquareB1IsAttacked() {
        // Reglamento: la casilla de origen de la torre (y b1) sí pueden estar atacadas
        List<Move> moves = legal("1r4k1/8/8/8/8/8/8/R3K3 w Q - 0 1");
        assertTrue(has(moves, MoveType.CASTLE_QUEEN_SIDE));
    }

    @Test
    void enPassantAvailableOnlyRightAfterDoubleStep() {
        // Negras acaban de jugar d7-d5; peón blanco en e5
        List<Move> moves = legal("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1");
        assertTrue(moves.stream().anyMatch(m -> m.type() == MoveType.EN_PASSANT && m.to().equals(Position.parse("d6"))));
        // Misma posición sin la marca de "al paso" (pasó un turno): ya no se puede
        assertFalse(has(legal("4k3/8/8/3pP3/8/8/8/4K3 w - - 0 1"), MoveType.EN_PASSANT));
    }

    @Test
    void promotionOffersFourPieces() {
        List<Move> moves = legal("4k3/1P6/8/8/8/8/8/4K3 w - - 0 1");
        List<PieceType> promos = moves.stream().filter(m -> m.type() == MoveType.PROMOTION).map(Move::promotion).toList();
        assertEquals(4, promos.size());
        assertTrue(promos.containsAll(List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)));
    }

    @Test
    void detectsCheck() {
        Board board = BoardFactory.fromFen("4k3/8/8/8/8/8/8/4K2r w - - 0 1").board();
        assertTrue(generator.checkDetector().isInCheck(board, Color.WHITE));
        assertFalse(generator.checkDetector().isInCheck(board, Color.BLACK));
    }
}
