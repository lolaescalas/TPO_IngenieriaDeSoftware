package backend.model;

public class Board {

    private final Piece[][] squares = new Piece[Position.SIZE][Position.SIZE];

    public Piece getPiece(Position position) {
        return squares[position.row()][position.column()];
    }

    public void setPiece(Position position, Piece piece) {
        squares[position.row()][position.column()] = piece;
    }

    public boolean isEmpty(Position position) {
        return getPiece(position) == null;
    }

    public boolean hasEnemyPiece(Position position, Color color) {
        Piece piece = getPiece(position);
        return piece != null && piece.getColor() != color;
    }
}