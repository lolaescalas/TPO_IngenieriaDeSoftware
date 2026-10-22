package chess.adapters.console;

import chess.domain.event.GameEventListener;
import chess.domain.event.MoveEvent;
import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.MoveType;
import chess.domain.model.Piece;
import chess.domain.model.Position;
import chess.domain.rules.GameResult;

import java.io.PrintStream;

/**
 * Adaptador de salida (Observer): dibuja el tablero en texto cada vez que el núcleo avisa
 * de una jugada. Mayúsculas = blancas, minúsculas = negras (letras en inglés:
 * K rey, Q dama, R torre, B alfil, N caballo, P peón).
 */
public final class ConsoleBoardRenderer implements GameEventListener {

    private final PrintStream out;

    public ConsoleBoardRenderer(PrintStream out) {
        this.out = out;
    }

    public static String render(Board board) {
        StringBuilder sb = new StringBuilder();
        sb.append("    a b c d e f g h\n");
        sb.append("  +-----------------+\n");
        for (int r = 0; r < Position.SIZE; r++) {
            int rank = Position.SIZE - r;
            sb.append(rank).append(" | ");
            for (int c = 0; c < Position.SIZE; c++) {
                Piece p = board.getPiece(Position.of(r, c));
                sb.append(p == null ? ((r + c) % 2 == 0 ? '.' : ':') : p.symbol()).append(' ');
            }
            sb.append("| ").append(rank).append('\n');
        }
        sb.append("  +-----------------+\n");
        sb.append("    a b c d e f g h\n");
        return sb.toString();
    }

    public void show(Board board) {
        out.print(render(board));
    }

    @Override
    public void onMoveMade(MoveEvent event) {
        out.println();
        out.println((event.mover() == Color.WHITE ? "Blancas" : "Negras") + " juegan " + describe(event));
        show(event.board());
    }

    @Override
    public void onCheck(Color colorInCheck) {
        out.println("¡Jaque al rey " + (colorInCheck == Color.WHITE ? "blanco" : "negro") + "!");
    }

    @Override
    public void onMoveUndone(Move move) {
        out.println("Se deshizo " + move);
    }

    @Override
    public void onGameOver(GameResult result) {
        out.println();
        out.println("=== Fin de la partida: " + result + " ===");
    }

    private static String describe(MoveEvent event) {
        Move m = event.move();
        String text = switch (m.type()) {
            case CASTLE_KING_SIDE -> "O-O (enroque corto)";
            case CASTLE_QUEEN_SIDE -> "O-O-O (enroque largo)";
            case EN_PASSANT -> m + " (captura al paso)";
            case PROMOTION -> m + " (corona)";
            default -> m.toString();
        };
        return event.isCapture() && m.type() != MoveType.EN_PASSANT
                ? text + " x" + event.capturedPiece().symbol()
                : text;
    }
}
