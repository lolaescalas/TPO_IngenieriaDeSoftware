package chess.application;

import chess.application.port.Player;
import chess.application.port.PlayerAction;
import chess.domain.game.IllegalMoveException;
import chess.domain.model.Color;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Bucle de juego (capa de aplicación): pide la acción al jugador de turno y la aplica.
 * No sabe si los jugadores son humanos o IA ni cómo se muestra el tablero: todo le llega
 * inyectado (DIP).
 */
public class GameController {

    private final ChessFacade game;
    private final Map<Color, Player> players = new EnumMap<>(Color.class);

    public GameController(ChessFacade game, Player white, Player black) {
        this.game = Objects.requireNonNull(game);
        players.put(Color.WHITE, Objects.requireNonNull(white));
        players.put(Color.BLACK, Objects.requireNonNull(black));
    }

    /** Juega hasta que termine la partida o alguien salga. */
    public void run() {
        while (!game.isOver()) {
            Color side = game.sideToMove().orElseThrow();
            if (!step(side)) {
                return;
            }
        }
    }

    /** Un turno. Devuelve false si el jugador eligió salir. */
    boolean step(Color side) {
        Player current = players.get(side);
        Player opponent = players.get(side.opposite());
        PlayerAction action = current.nextAction(game);
        try {
            switch (action) {
                case PlayerAction.PlayMove m -> game.play(m.notation());
                case PlayerAction.Undo u -> undo(current, opponent);
                case PlayerAction.Redo r -> redo(current, opponent);
                case PlayerAction.OfferDraw d -> offerDraw(current, opponent);
                case PlayerAction.Resign r -> game.resign(side);
                case PlayerAction.Quit q -> {
                    return false;
                }
            }
        } catch (IllegalMoveException e) {
            current.notify(e.getMessage());
        }
        return true;
    }

    /** Contra la IA se deshacen dos medias jugadas para que vuelva a jugar el humano. */
    private void undo(Player current, Player opponent) {
        int plies = opponent.isHuman() ? 1 : 2;
        int undone = 0;
        while (undone < plies && game.undo()) {
            undone++;
        }
        if (undone == 0) {
            current.notify("No hay jugadas para deshacer");
        }
    }

    private void redo(Player current, Player opponent) {
        int plies = opponent.isHuman() ? 1 : 2;
        int redone = 0;
        while (redone < plies && game.redo()) {
            redone++;
        }
        if (redone == 0) {
            current.notify("No hay jugadas para rehacer");
        }
    }

    private void offerDraw(Player current, Player opponent) {
        if (opponent.acceptsDraw(game)) {
            game.agreeDraw();
        } else {
            current.notify(opponent.name() + " rechazó las tablas");
        }
    }
}
