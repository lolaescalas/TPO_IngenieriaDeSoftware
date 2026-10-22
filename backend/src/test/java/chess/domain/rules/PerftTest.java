package chess.domain.rules;

import chess.domain.command.MoveCommand;
import chess.domain.factory.BoardFactory;
import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * "Perft" (performance test): cuenta todas las secuencias de jugadas legales hasta una
 * profundidad y lo compara con valores publicados (chessprogramming.org/Perft_Results).
 * Si un solo detalle de las reglas está mal (enroque, al paso, coronación, clavadas),
 * el número no coincide. Es el test de integración más fuerte del generador.
 */
class PerftTest {

    private static final MoveGenerator GENERATOR = new MoveGenerator();

    static long perft(Board board, Color side, int depth) {
        if (depth == 0) {
            return 1;
        }
        long nodes = 0;
        for (Move move : GENERATOR.legalMoves(board, side)) {
            MoveCommand command = new MoveCommand(board, move);
            command.execute();
            nodes += perft(board, side.opposite(), depth - 1);
            command.undo();
        }
        return nodes;
    }

    @ParameterizedTest(name = "{0} profundidad {1} = {2}")
    @CsvSource(delimiter = ';', value = {
            // Posición inicial
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; 1; 20",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; 2; 400",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; 3; 8902",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; 4; 197281",
            // "Kiwipete": enroques, clavadas, capturas y coronaciones
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1; 1; 48",
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1; 2; 2039",
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1; 3; 97862",
            // Posición 3: captura al paso que deja al rey en jaque (descubierto)
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1; 4; 43238",
            // Posición 4: coronaciones y enroques del negro
            "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1; 3; 9467",
            // Posición 5
            "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8; 3; 62379"
    })
    void perftMatchesKnownResults(String fen, int depth, long expected) {
        BoardFactory.Setup setup = BoardFactory.fromFen(fen);
        assertEquals(expected, perft(setup.board(), setup.sideToMove(), depth));
    }
}
