package chess.domain.game;

import chess.domain.model.Color;

/** Estado: turno de las blancas. Singleton porque no guarda datos propios. */
public final class WhiteTurnState extends TurnState {

    static final WhiteTurnState INSTANCE = new WhiteTurnState();

    private WhiteTurnState() {
    }

    @Override
    protected Color color() {
        return Color.WHITE;
    }

    @Override
    protected TurnState opponentTurn() {
        return BlackTurnState.INSTANCE;
    }

    @Override
    public String toString() {
        return "Turno de las blancas";
    }
}
