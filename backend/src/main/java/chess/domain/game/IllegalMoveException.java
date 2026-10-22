package chess.domain.game;

/** Jugada rechazada por las reglas (o por el estado de la partida). */
public class IllegalMoveException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IllegalMoveException(String message) {
        super(message);
    }
}
