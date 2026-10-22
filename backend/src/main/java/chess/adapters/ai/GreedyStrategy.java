package chess.adapters.ai;

import chess.domain.command.MoveCommand;
import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.rules.MoveGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dificultad media: mira una sola jugada hacia adelante y elige la que deja mejor
 * evaluación (si puede comer, come). Desempata al azar. Complejidad O(n) en jugadas.
 */
public final class GreedyStrategy implements AiStrategy {

    private final MoveGenerator moveGenerator;
    private final MaterialEvaluator evaluator;
    private final Random random;

    public GreedyStrategy(MoveGenerator moveGenerator, MaterialEvaluator evaluator, Random random) {
        this.moveGenerator = moveGenerator;
        this.evaluator = evaluator;
        this.random = random;
    }

    @Override
    public Move chooseMove(Board board, Color color) {
        List<Move> moves = moveGenerator.legalMoves(board, color);
        if (moves.isEmpty()) {
            throw new IllegalStateException("No legal moves");
        }
        int best = Integer.MIN_VALUE;
        List<Move> bestMoves = new ArrayList<>();
        for (Move move : moves) {
            MoveCommand command = new MoveCommand(board, move);
            command.execute();
            int score = moveGenerator.legalMoves(board, color.opposite()).isEmpty()
                    && moveGenerator.checkDetector().isInCheck(board, color.opposite())
                    ? Integer.MAX_VALUE // da mate: no hay nada mejor
                    : evaluator.evaluate(board, color);
            command.undo();
            if (score > best) {
                best = score;
                bestMoves.clear();
            }
            if (score == best) {
                bestMoves.add(move);
            }
        }
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    @Override
    public String name() {
        return "Media (greedy)";
    }
}
