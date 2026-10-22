package chess.domain.game;

import chess.application.ChessFacade;
import chess.domain.model.Color;
import chess.domain.rules.GameResult;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests de la partida: transiciones del State, Observer, deshacer/rehacer y todos los
 * finales del reglamento. Se juega a través de la Facade (como lo haría una UI).
 */
class GameTest {

    private static ChessFacade play(Game game, String... moves) {
        ChessFacade facade = new ChessFacade(game);
        for (String m : moves) {
            facade.play(m);
        }
        return facade;
    }

    // ------------------------------------------------------------------ State

    @Test
    void whiteStartsAndTurnsAlternate() {
        Game game = Game.standard();
        assertInstanceOf(WhiteTurnState.class, game.getState());
        play(game, "e2 e4");
        assertInstanceOf(BlackTurnState.class, game.getState());
        new ChessFacade(game).play("e7 e5");
        assertInstanceOf(WhiteTurnState.class, game.getState());
    }

    @Test
    void cannotMoveOpponentPieces() {
        Game game = Game.standard();
        IllegalMoveException e = assertThrows(IllegalMoveException.class,
                () -> game.play(new chess.domain.model.Move(
                        chess.domain.model.Position.parse("e7"), chess.domain.model.Position.parse("e5"))));
        assertTrue(e.getMessage().contains("blancas"));
    }

    @Test
    void rejectsIllegalMove() {
        ChessFacade facade = new ChessFacade(Game.standard());
        assertThrows(IllegalMoveException.class, () -> facade.play("e2 e5"));
    }

    // ------------------------------------------------------------------ Finales

    @Test
    void foolsMateIsCheckmate() {
        Game game = Game.standard();
        RecordingListener listener = new RecordingListener();
        game.addListener(listener);

        play(game, "f2 f3", "e7 e5", "g2 g4", "d8 h4");

        assertInstanceOf(GameOverState.class, game.getState());
        GameResult result = game.result().orElseThrow();
        assertEquals(GameResult.Reason.CHECKMATE, result.reason());
        assertEquals(Color.BLACK, result.winner());
        assertEquals(List.of("move f2f3", "move e7e5", "move g2g4", "move d8h4", "check WHITE", "over CHECKMATE"),
                listener.events);
        assertThrows(IllegalMoveException.class, () -> new ChessFacade(game).play("a2 a3"));
    }

    @Test
    void stalemateIsDraw() {
        // Negras: rey en a8. Blancas juegan Dama c7?? -> ahogado (rey sin jugadas y sin jaque)
        Game game = Game.fromFen("k7/8/1K6/8/8/8/8/2Q5 w - - 0 1");
        play(game, "c1 c7");
        assertEquals(GameResult.Reason.STALEMATE, game.result().orElseThrow().reason());
        assertTrue(game.result().orElseThrow().isDraw());
    }

    @Test
    void insufficientMaterialAfterCapture() {
        // Rey blanco captura el último caballo... queda Rey + Alfil vs Rey
        Game game = Game.fromFen("4k3/8/8/8/8/8/3n4/4K2B w - - 0 1");
        play(game, "e1 d2");
        assertEquals(GameResult.Reason.INSUFFICIENT_MATERIAL, game.result().orElseThrow().reason());
    }

    @Test
    void threefoldRepetition() {
        Game game = Game.standard();
        play(game,
                "g1 f3", "g8 f6", "f3 g1", "f6 g8",   // posición inicial: 2da vez
                "g1 f3", "g8 f6", "f3 g1", "f6 g8");  // 3ra vez
        assertEquals(GameResult.Reason.THREEFOLD_REPETITION, game.result().orElseThrow().reason());
    }

    @Test
    void fiftyMoveRule() {
        // Contador en 99 medias jugadas: una jugada más sin captura ni peón -> tablas
        Game game = Game.fromFen("4k3/8/8/8/8/8/8/R3K3 w - - 99 80");
        play(game, "a1 a2");
        assertEquals(GameResult.Reason.FIFTY_MOVE_RULE, game.result().orElseThrow().reason());
    }

    @Test
    void resignationAndAgreement() {
        Game a = Game.standard();
        a.resign(Color.WHITE);
        assertEquals(Color.BLACK, a.result().orElseThrow().winner());
        assertFalse(a.undo(), "la rendición no se deshace");

        Game b = Game.standard();
        b.agreeDraw();
        assertEquals(GameResult.Reason.AGREEMENT, b.result().orElseThrow().reason());
    }

    // ------------------------------------------------------------ Undo / Redo

    @Test
    void undoAndRedoRestoreTurnAndBoard() {
        Game game = Game.standard();
        String initial = game.boardSnapshot().positionKey(Color.WHITE);
        play(game, "e2 e4", "e7 e5");

        assertTrue(game.undo());
        assertTrue(game.undo());
        assertFalse(game.undo());
        assertEquals(initial, game.boardSnapshot().positionKey(Color.WHITE));
        assertInstanceOf(WhiteTurnState.class, game.getState());

        assertTrue(game.redo());
        assertInstanceOf(BlackTurnState.class, game.getState());
        assertEquals(1, game.moveHistory().size());
    }

    @Test
    void undoCheckmateReopensTheGame() {
        Game game = Game.standard();
        play(game, "f2 f3", "e7 e5", "g2 g4", "d8 h4");
        assertTrue(game.undo());
        assertInstanceOf(BlackTurnState.class, game.getState());
        assertFalse(game.isOver());
    }

    @Test
    void castlingAndPromotionThroughFacade() {
        Game game = Game.fromFen("r3k3/1P6/8/8/8/8/8/R3K2R w KQq - 0 1");
        ChessFacade facade = play(game, "O-O");
        assertEquals("K", String.valueOf(game.pieceAt(chess.domain.model.Position.parse("g1")).symbol()));
        facade.play("a8 a7");
        facade.play("b7 b8 C"); // coronar caballo (letra en español)
        assertEquals('N', game.pieceAt(chess.domain.model.Position.parse("b8")).symbol());
    }
}
