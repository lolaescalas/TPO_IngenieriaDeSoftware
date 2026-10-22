package chess.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Estado del tablero: piezas + datos necesarios para las reglas especiales
 * (derechos de enroque, casilla de captura al paso y contador de la regla de 50 jugadas).
 * <p>
 * SRP (Unidad 01): solo guarda estado. No sabe qué jugadas son legales
 * (eso es {@code MoveGenerator}) ni cuándo termina la partida ({@code GameEndDetector}).
 */
public class Board {

    private final Piece[][] squares = new Piece[Position.SIZE][Position.SIZE];
    private CastlingRights castlingRights = CastlingRights.NONE;
    private Position enPassantTarget;
    private int halfmoveClock;

    public Piece getPiece(Position position) {
        return squares[position.row()][position.column()];
    }

    public void setPiece(Position position, Piece piece) {
        squares[position.row()][position.column()] = piece;
    }

    public void removePiece(Position position) {
        setPiece(position, null);
    }

    public boolean isEmpty(Position position) {
        return getPiece(position) == null;
    }

    public boolean hasEnemyPiece(Position position, Color color) {
        Piece piece = getPiece(position);
        return piece != null && piece.getColor() != color;
    }

    public CastlingRights getCastlingRights() {
        return castlingRights;
    }

    public void setCastlingRights(CastlingRights castlingRights) {
        this.castlingRights = castlingRights;
    }

    /** Casilla "saltada" por el último avance doble de peón (o vacío si no hubo). */
    public Optional<Position> getEnPassantTarget() {
        return Optional.ofNullable(enPassantTarget);
    }

    public void setEnPassantTarget(Position enPassantTarget) {
        this.enPassantTarget = enPassantTarget;
    }

    /** Medias jugadas desde la última captura o movimiento de peón (regla de 50 jugadas). */
    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    public void setHalfmoveClock(int halfmoveClock) {
        this.halfmoveClock = halfmoveClock;
    }

    public Optional<Position> findKing(Color color) {
        for (int r = 0; r < Position.SIZE; r++) {
            for (int c = 0; c < Position.SIZE; c++) {
                Piece p = squares[r][c];
                if (p != null && p.getColor() == color && p.getType() == PieceType.KING) {
                    return Optional.of(Position.of(r, c));
                }
            }
        }
        return Optional.empty();
    }

    /** Posiciones ocupadas por piezas de un color. */
    public List<Position> positionsOf(Color color) {
        List<Position> result = new ArrayList<>(16);
        for (int r = 0; r < Position.SIZE; r++) {
            for (int c = 0; c < Position.SIZE; c++) {
                Piece p = squares[r][c];
                if (p != null && p.getColor() == color) {
                    result.add(Position.of(r, c));
                }
            }
        }
        return result;
    }

    /** Todas las piezas que quedan (para reglas de material). */
    public List<Piece> allPieces() {
        List<Piece> result = new ArrayList<>(32);
        for (Piece[] row : squares) {
            for (Piece p : row) {
                if (p != null) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    /**
     * Copia independiente. La IA copia el tablero una sola vez y después explora con
     * make/unmake (Command) sobre esa copia, en vez de copiar en cada nodo (Unidad 04, RAM).
     */
    public Board copy() {
        Board copy = new Board();
        for (int r = 0; r < Position.SIZE; r++) {
            System.arraycopy(squares[r], 0, copy.squares[r], 0, Position.SIZE);
        }
        copy.castlingRights = castlingRights;
        copy.enPassantTarget = enPassantTarget;
        copy.halfmoveClock = halfmoveClock;
        return copy;
    }

    /**
     * Clave que identifica la posición para la regla de triple repetición:
     * piezas + turno + derechos de enroque + casilla al paso.
     */
    public String positionKey(Color sideToMove) {
        StringBuilder sb = new StringBuilder(80);
        for (Piece[] row : squares) {
            for (Piece p : row) {
                sb.append(p == null ? '.' : p.symbol());
            }
        }
        sb.append(sideToMove == Color.WHITE ? 'w' : 'b')
          .append(castlingRights)
          .append(enPassantTarget == null ? "-" : enPassantTarget.toString());
        return sb.toString();
    }
}
