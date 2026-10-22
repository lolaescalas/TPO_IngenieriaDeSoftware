package chess.domain.game;

import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.rules.GameResult;

import java.util.Optional;

/**
 * Patrón State (Unidad 01, clase 3): el comportamiento de la partida depende de su estado.
 * <ul>
 *   <li>{@link WhiteTurnState}: solo acepta jugadas de las blancas; después pasa a negras.</li>
 *   <li>{@link BlackTurnState}: ídem para las negras.</li>
 *   <li>{@link GameOverState}: la partida terminó (mate, tablas o rendición); rechaza jugadas.</li>
 * </ul>
 * Gracias a esto no hay {@code if (turno == BLANCAS)} repartidos por el código: cada
 * estado sabe qué aceptar y a qué estado pasar.
 */
public sealed interface GameState permits TurnState, GameOverState {

    /** Intenta jugar {@code move}; el estado decide si lo acepta y cuál es el siguiente estado. */
    void play(Game game, Move move);

    /** A quién le toca mover (vacío si la partida terminó). */
    Optional<Color> sideToMove();

    Optional<GameResult> result();

    default boolean isOver() {
        return result().isPresent();
    }
}
