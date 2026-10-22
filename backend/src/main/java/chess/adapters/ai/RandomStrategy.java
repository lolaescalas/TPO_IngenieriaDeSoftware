package chess.adapters.ai;

import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.rules.MoveGenerator;

import java.util.List;
import java.util.Random;

/** Dificultad fácil: cualquier jugada legal al azar. */
public final class RandomStrategy implements AiStrategy {

    private final MoveGenerator moveGenerator;
    private final Random random;

    /** {@code random} inyectado: en los tests se usa una semilla fija para que sea determinista. */
    public RandomStrategy(MoveGenerator moveGenerator, Random random) {
        this.moveGenerator = moveGenerator;
        this.random = random;
    }

    @Override
    public Move chooseMove(Board board, Color color) {
        List<Move> moves = moveGenerator.legalMoves(board, color);
        if (moves.isEmpty()) {
            throw new IllegalStateException("No legal moves");
        }
        return moves.get(random.nextInt(moves.size()));
    }

    @Override
    public String name() {
        return "Fácil (aleatoria)";
    }
}
