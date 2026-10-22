package chess.domain.movement;

import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Caballo y Rey: saltos de desplazamientos fijos (sin importar lo que haya en el medio).
 * <p>
 * Optimización CPU (Unidad 04, precomputación / lookup table): los destinos posibles desde
 * cada una de las 64 casillas se calculan una sola vez en el constructor; generar jugadas
 * pasa a ser recorrer una lista ya armada.
 */
public final class JumpMovement implements MovementPattern {

    public static final int[][] KNIGHT_OFFSETS = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
    public static final int[][] KING_OFFSETS = {
            {-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

    private final List<List<Position>> targetsBySquare = new ArrayList<>(64);
    private final boolean[][] reach = new boolean[64][64];

    public JumpMovement(int[][] offsets) {
        for (int r = 0; r < Position.SIZE; r++) {
            for (int c = 0; c < Position.SIZE; c++) {
                Position origin = Position.of(r, c);
                List<Position> targets = new ArrayList<>();
                for (int[] o : offsets) {
                    if (origin.canOffset(o[0], o[1])) {
                        Position t = origin.offset(o[0], o[1]);
                        targets.add(t);
                        reach[index(origin)][index(t)] = true;
                    }
                }
                targetsBySquare.add(List.copyOf(targets));
            }
        }
    }

    @Override
    public void generate(Board board, Position from, Color color, List<Move> out) {
        for (Position destination : targetsBySquare.get(index(from))) {
            if (board.isEmpty(destination) || board.hasEnemyPiece(destination, color)) {
                out.add(new Move(from, destination));
            }
        }
    }

    @Override
    public boolean attacks(Board board, Position from, Position target, Color color) {
        return reach[index(from)][index(target)];
    }

    private static int index(Position p) {
        return p.row() * Position.SIZE + p.column();
    }
}
