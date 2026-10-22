package chess.domain.game;

import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.Piece;
import chess.domain.rules.GameResult;

import java.util.Optional;

/**
 * Comportamiento común a "le toca a un color". Las subclases solo dicen qué color es y
 * cuál es el estado del rival (Template Method sobre el State).
 */
public abstract sealed class TurnState implements GameState permits WhiteTurnState, BlackTurnState {

    protected abstract Color color();

    protected abstract TurnState opponentTurn();

    public static TurnState of(Color color) {
        return color == Color.WHITE ? WhiteTurnState.INSTANCE : BlackTurnState.INSTANCE;
    }

    @Override
    public void play(Game game, Move move) {
        Piece piece = game.pieceAt(move.from());
        if (piece == null) {
            throw new IllegalMoveException("No hay ninguna pieza en " + move.from());
        }
        if (piece.getColor() != color()) {
            throw new IllegalMoveException("Le toca mover a las " + colorName(color()));
        }
        if (!game.legalMoves().contains(move)) {
            throw new IllegalMoveException("Jugada ilegal: " + move);
        }
        game.executeMove(move);
        game.changeState(next(game));
    }

    /** Transición: si la jugada terminó la partida -> GameOver; si no -> turno del rival. */
    public GameState next(Game game) {
        Optional<GameResult> end = game.detectEnd(color().opposite());
        return end.<GameState>map(GameOverState::new).orElse(opponentTurn());
    }

    @Override
    public Optional<Color> sideToMove() {
        return Optional.of(color());
    }

    @Override
    public Optional<GameResult> result() {
        return Optional.empty();
    }

    private static String colorName(Color c) {
        return c == Color.WHITE ? "blancas" : "negras";
    }
}
