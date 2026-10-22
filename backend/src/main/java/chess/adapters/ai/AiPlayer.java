package chess.adapters.ai;

import chess.application.ChessFacade;
import chess.application.port.Player;
import chess.application.port.PlayerAction;
import chess.domain.model.Color;
import chess.domain.model.Move;

/**
 * Adaptador: un jugador controlado por la computadora. Delega la decisión en una
 * {@link AiStrategy} inyectada (Strategy + DIP).
 */
public final class AiPlayer implements Player {

    /** Si la IA está abajo por más de esto (centipeones), acepta tablas. */
    private static final int ACCEPT_DRAW_BELOW = -300;

    private final AiStrategy strategy;
    private final MaterialEvaluator evaluator = new MaterialEvaluator();

    public AiPlayer(AiStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public String name() {
        return "IA " + strategy.name();
    }

    @Override
    public PlayerAction nextAction(ChessFacade game) {
        Color color = game.sideToMove().orElseThrow();
        Move move = strategy.chooseMove(game.boardSnapshot(), color);
        return new PlayerAction.PlayMove(move.toString());
    }

    @Override
    public boolean acceptsDraw(ChessFacade game) {
        Color me = game.sideToMove().orElseThrow().opposite(); // la oferta llega en el turno del rival
        return evaluator.evaluate(game.boardSnapshot(), me) < ACCEPT_DRAW_BELOW;
    }

    @Override
    public boolean isHuman() {
        return false;
    }
}
