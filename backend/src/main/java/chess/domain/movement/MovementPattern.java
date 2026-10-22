package chess.domain.movement;

import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.Position;

import java.util.List;

/**
 * Patrón Strategy (Unidad 00/01): encapsula "cómo se mueve" una pieza.
 * <p>
 * OCP: para una pieza nueva (ej. de una variante) se crea/compone un patrón sin tocar
 * el generador de jugadas. LSP: toda implementación respeta el mismo contrato.
 * <p>
 * Contrato: genera jugadas <b>pseudo-legales</b> (respetan el desplazamiento de la pieza,
 * no salen del tablero ni capturan piezas propias) pero <b>no</b> verifica si dejan al
 * propio rey en jaque; eso lo filtra {@code MoveGenerator}. Nunca modifica el tablero.
 */
public interface MovementPattern {

    /** Agrega a {@code out} las jugadas pseudo-legales desde {@code from}. Se reutiliza la lista para no crear objetos de más. */
    void generate(Board board, Position from, Color color, List<Move> out);

    /** ¿Una pieza de este patrón en {@code from} ataca la casilla {@code target}? */
    boolean attacks(Board board, Position from, Position target, Color color);
}
