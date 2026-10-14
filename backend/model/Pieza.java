package chess.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Piece {

    protected static final int[][] ORTHOGONAL = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    protected static final int[][] DIAGONAL   = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

    private final Color color;
    private final PieceType type;

    protected Piece(Color color, PieceType type) {
        this.color = Objects.requireNonNull(color);
        this.type = Objects.requireNonNull(type);
    }

    public Color getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }

    /** Movimientos que esta pieza podría hacer desde 'origin' según su regla de desplazamiento. */
    public abstract List<Move> possibleMoves(Board board, Position origin);

    /** Para Rook, Bishop y Queen: avanza en línea recta hasta chocar con algo. */
    protected List<Move> slidingMoves(Board board, Position origin, int[][] directions) {
        List<Move> moves = new ArrayList<>();
        for (int[] d : directions) {
            Position current = origin;
            while (current.canOffset(d[0], d[1])) {
                current = current.offset(d[0], d[1]);
                if (board.isEmpty(current)) {
                    moves.add(new Move(origin, current));
                } else {
                    if (board.hasEnemyPiece(current, color)) {
                        moves.add(new Move(origin, current)); // captura
                    }
                    break; // bloqueado por pieza propia o enemiga
                }
            }
        }
        return moves;
    }

    /** Para Knight y King: saltos de offsets fijos. */
    protected List<Move> jumpMoves(Board board, Position origin, int[][] offsets) {
        List<Move> moves = new ArrayList<>();
        for (int[] o : offsets) {
            if (origin.canOffset(o[0], o[1])) {
                Position destination = origin.offset(o[0], o[1]);
                if (board.isEmpty(destination) || board.hasEnemyPiece(destination, color)) {
                    moves.add(new Move(origin, destination));
                }
            }
        }
        return moves;
    }
}