package chess.application;

import chess.application.port.Player;
import chess.application.port.PlayerAction;
import chess.domain.game.Game;
import chess.domain.model.Color;
import chess.domain.rules.GameResult;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Test del bucle de juego con jugadores "fake" guionados (inyección de dependencias). */
class GameControllerTest {

    /** Fake: devuelve acciones predefinidas, en orden. */
    static final class ScriptedPlayer implements Player {
        private final Deque<PlayerAction> script = new ArrayDeque<>();
        private final boolean acceptsDraw;
        private final boolean human;
        final List<String> messages = new ArrayList<>();

        ScriptedPlayer(boolean acceptsDraw, boolean human, PlayerAction... actions) {
            this.acceptsDraw = acceptsDraw;
            this.human = human;
            script.addAll(List.of(actions));
        }

        @Override
        public String name() {
            return "fake";
        }

        @Override
        public PlayerAction nextAction(ChessFacade game) {
            return script.isEmpty() ? new PlayerAction.Quit() : script.poll();
        }

        @Override
        public boolean acceptsDraw(ChessFacade game) {
            return acceptsDraw;
        }

        @Override
        public boolean isHuman() {
            return human;
        }

        @Override
        public void notify(String message) {
            messages.add(message);
        }
    }

    private static PlayerAction move(String m) {
        return new PlayerAction.PlayMove(m);
    }

    @Test
    void playsUntilCheckmate() {
        ChessFacade facade = new ChessFacade(Game.standard());
        Player white = new ScriptedPlayer(false, true, move("f2 f3"), move("g2 g4"));
        Player black = new ScriptedPlayer(false, true, move("e7 e5"), move("d8 h4"));

        new GameController(facade, white, black).run();

        assertEquals(GameResult.Reason.CHECKMATE, facade.result().orElseThrow().reason());
    }

    @Test
    void illegalMoveIsReportedAndPlayerRetries() {
        ChessFacade facade = new ChessFacade(Game.standard());
        ScriptedPlayer white = new ScriptedPlayer(false, true, move("e2 e5"), move("e2 e4"));
        Player black = new ScriptedPlayer(false, true);

        new GameController(facade, white, black).run();

        assertEquals(1, white.messages.size());
        assertEquals(1, facade.moveHistory().size());
    }

    @Test
    void drawOfferAcceptedOrRejected() {
        ChessFacade accepted = new ChessFacade(Game.standard());
        new GameController(accepted, new ScriptedPlayer(false, true, new PlayerAction.OfferDraw()),
                new ScriptedPlayer(true, true)).run();
        assertEquals(GameResult.Reason.AGREEMENT, accepted.result().orElseThrow().reason());

        ChessFacade rejected = new ChessFacade(Game.standard());
        ScriptedPlayer offerer = new ScriptedPlayer(false, true, new PlayerAction.OfferDraw());
        new GameController(rejected, offerer, new ScriptedPlayer(false, true)).run();
        assertFalse(rejected.isOver());
        assertTrue(offerer.messages.getFirst().contains("rechazó"));
    }

    @Test
    void undoAgainstComputerRemovesTwoPlies() {
        ChessFacade facade = new ChessFacade(Game.standard());
        Player human = new ScriptedPlayer(false, true, move("e2 e4"), new PlayerAction.Undo());
        Player computer = new ScriptedPlayer(false, false, move("e7 e5"));

        new GameController(facade, human, computer).run();

        assertTrue(facade.moveHistory().isEmpty());
        assertEquals(Color.WHITE, facade.sideToMove().orElseThrow());
    }

    @Test
    void resignEndsTheGame() {
        ChessFacade facade = new ChessFacade(Game.standard());
        new GameController(facade, new ScriptedPlayer(false, true, new PlayerAction.Resign()),
                new ScriptedPlayer(false, true)).run();
        assertEquals(Color.BLACK, facade.result().orElseThrow().winner());
    }
}
