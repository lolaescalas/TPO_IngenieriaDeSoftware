package chess.domain.event;

import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.rules.GameResult;

/**
 * Patrón Observer (Unidad 00/01) y puerto de salida de la arquitectura hexagonal:
 * el núcleo avisa "pasó algo" sin saber quién escucha (consola, logger, una futura UI web).
 * <p>
 * ISP: los métodos son {@code default}, cada listener implementa solo lo que le interesa.
 * <p>
 * Memoria (Unidad 04): un listener registrado queda referenciado por la partida; si un
 * objeto deja de usarse hay que llamar a {@code removeListener} o se produce un
 * <i>memory leak</i> (el GC no puede liberarlo).
 */
public interface GameEventListener {

    default void onMoveMade(MoveEvent event) {
    }

    default void onCheck(Color colorInCheck) {
    }

    default void onMoveUndone(Move move) {
    }

    default void onGameOver(GameResult result) {
    }
}
