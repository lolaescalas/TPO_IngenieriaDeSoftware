package chess.domain.game;

import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.rules.GameResult;

import java.util.Objects;
import java.util.Optional;

/** Estado final: jaque mate, tablas (cualquier motivo) o rendición. No acepta más jugadas. */
public final class GameOverState implements GameState {

    private final GameResult result;

    public GameOverState(GameResult result) {
        this.result = Objects.requireNonNull(result);
    }

    @Override
    public void play(Game game, Move move) {
        throw new IllegalMoveException("La partida terminó: " + result);
    }

    @Override
    public Optional<Color> sideToMove() {
        return Optional.empty();
    }

    @Override
    public Optional<GameResult> result() {
        return Optional.of(result);
    }

    @Override
    public String toString() {
        return result.toString();
    }
}
