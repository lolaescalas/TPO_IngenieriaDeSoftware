package chess.adapters.ai;

import chess.domain.rules.MoveGenerator;

import java.util.Random;

/** Factory: de una dificultad a la Strategy concreta que la implementa. */
public final class AiStrategyFactory {

    private final MoveGenerator moveGenerator;
    private final Random random;

    public AiStrategyFactory(MoveGenerator moveGenerator, Random random) {
        this.moveGenerator = moveGenerator;
        this.random = random;
    }

    public AiStrategy forLevel(Difficulty difficulty) {
        MaterialEvaluator evaluator = new MaterialEvaluator();
        return switch (difficulty) {
            case EASY -> new RandomStrategy(moveGenerator, random);
            case MEDIUM -> new GreedyStrategy(moveGenerator, evaluator, random);
            case HARD -> new MinimaxStrategy(moveGenerator, evaluator, 4, 2_000);
        };
    }
}
