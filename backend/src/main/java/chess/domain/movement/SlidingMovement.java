package chess.domain.movement;

import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.Position;

import java.util.List;

/** Torre, Alfil (y la Dama por composición): avanza en línea recta hasta chocar con algo. */
public final class SlidingMovement implements MovementPattern {

    public static final int[][] ORTHOGONAL = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    public static final int[][] DIAGONAL = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

    private final int[][] directions;

    public SlidingMovement(int[][] directions) {
        this.directions = directions;
    }

    @Override
    public void generate(Board board, Position from, Color color, List<Move> out) {
        for (int[] d : directions) {
            Position current = from;
            while (current.canOffset(d[0], d[1])) {
                current = current.offset(d[0], d[1]);
                if (board.isEmpty(current)) {
                    out.add(new Move(from, current));
                } else {
                    if (board.hasEnemyPiece(current, color)) {
                        out.add(new Move(from, current)); // captura
                    }
                    break; // bloqueado por pieza propia o enemiga
                }
            }
        }
    }

    @Override
    public boolean attacks(Board board, Position from, Position target, Color color) {
        int dr = Integer.signum(target.row() - from.row());
        int dc = Integer.signum(target.column() - from.column());
        if (!supports(dr, dc) || !isAligned(from, target)) {
            return false;
        }
        Position current = from.offset(dr, dc);
        while (!current.equals(target)) {
            if (!board.isEmpty(current)) {
                return false; // hay una pieza en el medio
            }
            current = current.offset(dr, dc);
        }
        return true;
    }

    private boolean supports(int dr, int dc) {
        for (int[] d : directions) {
            if (d[0] == dr && d[1] == dc) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAligned(Position a, Position b) {
        int rows = Math.abs(a.row() - b.row());
        int cols = Math.abs(a.column() - b.column());
        return rows == 0 || cols == 0 || rows == cols;
    }
}
