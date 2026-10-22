package chess.domain.game;

import chess.domain.command.CommandHistory;
import chess.domain.command.MoveCommand;
import chess.domain.event.GameEventListener;
import chess.domain.event.MoveEvent;
import chess.domain.factory.BoardFactory;
import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.Piece;
import chess.domain.model.Position;
import chess.domain.rules.CheckDetector;
import chess.domain.rules.GameEndDetector;
import chess.domain.rules.GameResult;
import chess.domain.rules.MoveGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Una partida: el corazón del núcleo (dominio puro, sin consola ni IA).
 * <p>
 * Junta los patrones del dominio:
 * <ul>
 *   <li><b>State</b>: {@link GameState} decide qué hacer con cada jugada según el turno.</li>
 *   <li><b>Command</b>: cada jugada es un {@link MoveCommand} guardado en un
 *       {@link CommandHistory} para deshacer/rehacer.</li>
 *   <li><b>Observer</b>: avisa a los {@link GameEventListener} registrados.</li>
 *   <li><b>DIP / inyección de dependencias</b>: recibe {@link MoveGenerator} y
 *       {@link GameEndDetector} por constructor (en los tests se pueden reemplazar).</li>
 * </ul>
 */
public final class Game {

    private final Board board;
    private final MoveGenerator moveGenerator;
    private final GameEndDetector endDetector;
    private final CommandHistory<MoveCommand> history = new CommandHistory<>();
    private final List<GameEventListener> listeners = new CopyOnWriteArrayList<>();
    /** Cuántas veces apareció cada posición (triple repetición). */
    private final Map<String, Integer> repetitions = new HashMap<>();
    private GameState state;

    public Game(Board board, Color sideToMove, MoveGenerator moveGenerator, GameEndDetector endDetector) {
        this.board = board;
        this.moveGenerator = moveGenerator;
        this.endDetector = endDetector;
        repetitions.merge(board.positionKey(sideToMove), 1, Integer::sum);
        TurnState initial = TurnState.of(sideToMove);
        this.state = detectEnd(sideToMove).<GameState>map(GameOverState::new).orElse(initial);
    }

    public Game(Board board, Color sideToMove) {
        this(board, sideToMove, defaultGenerator());
    }

    private Game(Board board, Color sideToMove, MoveGenerator generator) {
        this(board, sideToMove, generator, new GameEndDetector(generator));
    }

    private static MoveGenerator defaultGenerator() {
        return new MoveGenerator(new CheckDetector());
    }

    /** Partida desde la posición inicial del reglamento. */
    public static Game standard() {
        return fromFen(BoardFactory.STANDARD_FEN);
    }

    /** Partida desde cualquier posición (FEN). Muy útil en tests. */
    public static Game fromFen(String fen) {
        BoardFactory.Setup setup = BoardFactory.fromFen(fen);
        return new Game(setup.board(), setup.sideToMove());
    }

    // ---------------------------------------------------------------- API pública

    /** Juega una jugada; el estado actual decide si la acepta. */
    public void play(Move move) {
        state.play(this, move);
    }

    public List<Move> legalMoves() {
        return state.sideToMove()
                .map(color -> moveGenerator.legalMoves(board, color))
                .orElse(List.of());
    }

    public List<Move> legalMovesFrom(Position from) {
        Piece piece = board.getPiece(from);
        if (piece == null || state.sideToMove().filter(piece.getColor()::equals).isEmpty()) {
            return List.of();
        }
        return moveGenerator.legalMovesFrom(board, from);
    }

    /** Deshace la última jugada (también si la partida había terminado por mate/tablas automáticas). */
    public boolean undo() {
        if (!history.canUndo() || endedByDecision()) {
            return false;
        }
        Color mover = board.getPiece(lastMove().orElseThrow().to()).getColor();
        decrementRepetition(board.positionKey(mover.opposite()));
        MoveCommand command = history.undo().orElseThrow();
        state = TurnState.of(mover);
        listeners.forEach(l -> l.onMoveUndone(command.getMove()));
        return true;
    }

    public boolean redo() {
        if (!history.canRedo() || state.isOver()) {
            return false;
        }
        Color mover = state.sideToMove().orElseThrow();
        MoveCommand command = history.redo().orElseThrow();
        afterExecute(command, mover);
        changeState(TurnState.of(mover).next(this));
        return true;
    }

    public void resign(Color loser) {
        ensureInProgress();
        changeState(new GameOverState(GameResult.win(loser.opposite(), GameResult.Reason.RESIGNATION)));
    }

    /** Tablas de mutuo acuerdo (la negociación la hace la capa de aplicación). */
    public void agreeDraw() {
        ensureInProgress();
        changeState(new GameOverState(GameResult.draw(GameResult.Reason.AGREEMENT)));
    }

    public GameState getState() {
        return state;
    }

    public Optional<Color> sideToMove() {
        return state.sideToMove();
    }

    public boolean isOver() {
        return state.isOver();
    }

    public Optional<GameResult> result() {
        return state.result();
    }

    public boolean isInCheck(Color color) {
        return moveGenerator.checkDetector().isInCheck(board, color);
    }

    public Piece pieceAt(Position position) {
        return board.getPiece(position);
    }

    /** Copia del tablero: quien la reciba (UI, IA) no puede romper la partida. */
    public Board boardSnapshot() {
        return board.copy();
    }

    public List<Move> moveHistory() {
        return history.executed().stream().map(MoveCommand::getMove).toList();
    }

    public Optional<Move> lastMove() {
        List<MoveCommand> done = history.executed();
        return done.isEmpty() ? Optional.empty() : Optional.of(done.getLast().getMove());
    }

    public boolean canUndo() {
        return history.canUndo();
    }

    public boolean canRedo() {
        return history.canRedo();
    }

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    /** Importante para evitar memory leaks (ver {@link GameEventListener}). */
    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    // ------------------------------------------------- usado por los estados (State)

    void executeMove(Move move) {
        Color mover = board.getPiece(move.from()).getColor();
        MoveCommand command = new MoveCommand(board, move);
        history.executeAndRecord(command);
        afterExecute(command, mover);
    }

    void changeState(GameState newState) {
        this.state = newState;
        newState.result().ifPresent(r -> listeners.forEach(l -> l.onGameOver(r)));
    }

    /** Fin de partida automático cuando le toca a {@code sideToMove}. */
    Optional<GameResult> detectEnd(Color sideToMove) {
        Optional<GameResult> result = endDetector.evaluate(board, sideToMove);
        if (result.isEmpty() && repetitions.getOrDefault(board.positionKey(sideToMove), 0) >= 3) {
            return Optional.of(GameResult.draw(GameResult.Reason.THREEFOLD_REPETITION));
        }
        return result;
    }

    // ------------------------------------------------------------------ privados

    private void afterExecute(MoveCommand command, Color mover) {
        repetitions.merge(board.positionKey(mover.opposite()), 1, Integer::sum);
        MoveEvent event = new MoveEvent(command.getMove(), mover, command.getMovedPiece(),
                command.getCapturedPiece(), board.copy());
        listeners.forEach(l -> l.onMoveMade(event));
        if (isInCheck(mover.opposite())) {
            listeners.forEach(l -> l.onCheck(mover.opposite()));
        }
    }

    /** Rendición o acuerdo no se "deshacen": son decisiones de los jugadores, no jugadas. */
    private boolean endedByDecision() {
        return state.result()
                .map(r -> r.reason() == GameResult.Reason.RESIGNATION || r.reason() == GameResult.Reason.AGREEMENT)
                .orElse(false);
    }

    private void decrementRepetition(String key) {
        repetitions.computeIfPresent(key, (k, v) -> v > 1 ? v - 1 : null);
    }

    private void ensureInProgress() {
        if (state.isOver()) {
            throw new IllegalMoveException("La partida ya terminó: " + state);
        }
    }
}
