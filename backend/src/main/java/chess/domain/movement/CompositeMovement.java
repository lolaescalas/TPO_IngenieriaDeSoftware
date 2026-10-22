package chess.domain.movement;

import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.Position;

import java.util.List;

/**
 * Composición de patrones: la Dama = Torre + Alfil ("funciona como una Torre y un Alfil
 * combinados", según el reglamento). Ejemplo directo de composición sobre herencia.
 */
public final class CompositeMovement implements MovementPattern {

    private final List<MovementPattern> parts;

    public CompositeMovement(MovementPattern... parts) {
        this.parts = List.of(parts);
    }

    @Override
    public void generate(Board board, Position from, Color color, List<Move> out) {
        for (MovementPattern part : parts) {
            part.generate(board, from, color, out);
        }
    }

    @Override
    public boolean attacks(Board board, Position from, Position target, Color color) {
        for (MovementPattern part : parts) {
            if (part.attacks(board, from, target, color)) {
                return true;
            }
        }
        return false;
    }
}
