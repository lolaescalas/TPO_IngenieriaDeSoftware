package chess.domain.game;

import chess.domain.model.Color;

/** Estado: turno de las negras. Singleton porque no guarda datos propios. */
public final class BlackTurnState extends TurnState {

    static final BlackTurnState INSTANCE = new BlackTurnState();

    private BlackTurnState() {
    }

    @Override
    protected Color color() {
        return Color.BLACK;
    }

    @Override
    protected TurnState opponentTurn() {
        return WhiteTurnState.INSTANCE;
    }

    @Override
    public String toString() {
        return "Turno de las negras";
    }
}
