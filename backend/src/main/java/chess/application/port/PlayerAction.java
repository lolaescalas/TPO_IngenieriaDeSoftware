package chess.application.port;

/** Acciones posibles en un turno (tipo algebraico: sealed + records). */
public sealed interface PlayerAction {

    record PlayMove(String notation) implements PlayerAction {
    }

    record Undo() implements PlayerAction {
    }

    record Redo() implements PlayerAction {
    }

    record OfferDraw() implements PlayerAction {
    }

    record Resign() implements PlayerAction {
    }

    record Quit() implements PlayerAction {
    }
}
