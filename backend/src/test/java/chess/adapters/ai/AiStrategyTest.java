package chess.adapters.ai;

import chess.domain.factory.BoardFactory;
import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.rules.MoveGenerator;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiStrategyTest {

    private final MoveGenerator generator = new MoveGenerator();
    private final AiStrategyFactory factory = new AiStrategyFactory(generator, new Random(42));

    @Test
    void everyLevelReturnsALegalMove() {
        BoardFactory.Setup setup = BoardFactory.standard();
        for (Difficulty level : Difficulty.values()) {
            Move move = factory.forLevel(level).chooseMove(setup.board().copy(), Color.WHITE);
            assertTrue(generator.legalMoves(setup.board(), Color.WHITE).contains(move), level + " jugó ilegal");
        }
    }

    @Test
    void greedyCapturesHangingQueen() {
        // Dama negra en d5 indefensa, atacable por el peón e4
        Board board = BoardFactory.fromFen("4k3/8/8/3q4/4P3/8/8/4K3 w - - 0 1").board();
        Move move = factory.forLevel(Difficulty.MEDIUM).chooseMove(board, Color.WHITE);
        assertEquals("e4d5", move.toString());
    }

    @Test
    void minimaxFindsMateInOne() {
        // Mate del pasillo: Ta1-a8#
        Board board = BoardFactory.fromFen("6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1").board();
        Move move = factory.forLevel(Difficulty.HARD).chooseMove(board, Color.WHITE);
        assertEquals("a1a8", move.toString());
    }

    @Test
    void minimaxAvoidsLosingItsQueen() {
        // La dama blanca en d4 está atacada por el peón e5: debe moverla (o capturar)
        Board board = BoardFactory.fromFen("4k3/8/8/4p3/3Q4/8/8/4K3 w - - 0 1").board();
        Move move = new MinimaxStrategy(generator, new MaterialEvaluator(), 3, 2_000).chooseMove(board, Color.WHITE);
        assertEquals("d4", move.from().toString());
    }

    /** "SLA" interno (Unidad 03): la IA difícil responde dentro de su presupuesto de tiempo. */
    @Test
    void hardAiRespectsItsTimeBudget() {
        AiStrategy hard = factory.forLevel(Difficulty.HARD);
        Board board = BoardFactory.fromFen(
                "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1").board();
        assertTimeoutPreemptively(Duration.ofMillis(hard.timeBudgetMillis() + 500),
                () -> hard.chooseMove(board, Color.WHITE));
    }
}
