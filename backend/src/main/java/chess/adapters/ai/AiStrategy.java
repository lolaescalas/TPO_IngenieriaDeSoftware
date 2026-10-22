package chess.adapters.ai;

import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;

/**
 * Patrón Strategy (Unidad 01/05): cada dificultad de la IA es un algoritmo distinto
 * intercambiable en tiempo de ejecución (Random = fácil, Greedy = medio, Minimax = difícil).
 * <p>
 * Contrato (LSP, y "SLA como interfaz" de la Unidad 03): recibe una <b>copia</b> del
 * tablero (puede modificarla libremente), devuelve una jugada legal para {@code color}
 * y responde dentro de {@link #timeBudgetMillis()}.
 */
public interface AiStrategy {

    Move chooseMove(Board board, Color color);

    String name();

    /** Tiempo máximo de respuesta comprometido (nuestro "SLA" interno). */
    default long timeBudgetMillis() {
        return 2_000;
    }
}
