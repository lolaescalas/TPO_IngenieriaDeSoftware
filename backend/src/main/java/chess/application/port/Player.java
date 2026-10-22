package chess.application.port;

import chess.application.ChessFacade;

/**
 * Puerto de entrada (hexagonal): "alguien que decide qué hacer en su turno".
 * Lo implementan un humano por consola y la IA; el controlador no sabe cuál es cuál
 * (DIP + Strategy). ISP: es independiente del puerto de salida {@code GameEventListener}.
 */
public interface Player {

    String name();

    /** Decide la acción del turno (jugar, deshacer, ofrecer tablas, rendirse...). */
    PlayerAction nextAction(ChessFacade game);

    /** El rival ofreció tablas: ¿las acepta? */
    boolean acceptsDraw(ChessFacade game);

    /** La IA no pide "deshacer"; sirve para deshacer de a dos jugadas contra la máquina. */
    default boolean isHuman() {
        return true;
    }

    /** Mensaje informativo para el jugador (ej. jugada rechazada). */
    default void notify(String message) {
    }
}
