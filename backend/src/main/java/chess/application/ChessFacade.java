package chess.application;

import chess.domain.event.GameEventListener;
import chess.domain.game.Game;
import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.Position;
import chess.domain.rules.GameResult;

import java.util.List;
import java.util.Optional;

/**
 * Patrón Facade (Unidad 00): una API chica y simple para cualquier interfaz (consola hoy,
 * web mañana), que esconde {@link Game}, los comandos, las reglas y el parseo de texto.
 * Es el puerto de entrada de la arquitectura hexagonal.
 */
public class ChessFacade {

    private final Game game;
    private final MoveParser parser;

    public ChessFacade(Game game, MoveParser parser) {
        this.game = game;
        this.parser = parser;
    }

    public ChessFacade(Game game) {
        this(game, new MoveParser());
    }

    /** Juega una jugada escrita como texto ("e2 e4", "O-O", "e7e8=Q"). Devuelve la jugada aplicada. */
    public Move play(String notation) {
        Move move = parser.parse(notation, game.legalMoves());
        game.play(move);
        return move;
    }

    public boolean undo() {
        return game.undo();
    }

    public boolean redo() {
        return game.redo();
    }

    public void resign(Color loser) {
        game.resign(loser);
    }

    public void agreeDraw() {
        game.agreeDraw();
    }

    /** Destinos posibles desde una casilla ("e2" -> ["e3", "e4"]). */
    public List<String> legalTargetsFrom(String square) {
        return game.legalMovesFrom(Position.parse(square)).stream()
                .map(m -> m.to().toString())
                .distinct()
                .toList();
    }

    public List<Move> legalMoves() {
        return game.legalMoves();
    }

    public Optional<Color> sideToMove() {
        return game.sideToMove();
    }

    public boolean isOver() {
        return game.isOver();
    }

    public Optional<GameResult> result() {
        return game.result();
    }

    public boolean isInCheck(Color color) {
        return game.isInCheck(color);
    }

    public Board boardSnapshot() {
        return game.boardSnapshot();
    }

    public List<Move> moveHistory() {
        return game.moveHistory();
    }

    public void addListener(GameEventListener listener) {
        game.addListener(listener);
    }

    public void removeListener(GameEventListener listener) {
        game.removeListener(listener);
    }
}
