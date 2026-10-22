package chess.adapters.console;

import chess.adapters.ai.AiPlayer;
import chess.adapters.ai.AiStrategyFactory;
import chess.adapters.ai.Difficulty;
import chess.application.ChessFacade;
import chess.application.GameController;
import chess.application.port.Player;
import chess.domain.game.Game;
import chess.domain.rules.MoveGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Random;

/** Menú de consola: elige modo y dificultad, arma los objetos y arranca la partida. */
public final class ConsoleApp {

    private final BufferedReader in;
    private final PrintStream out;

    public ConsoleApp(BufferedReader in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public void run() {
        out.println("=== Ajedrez - TPO Ingeniería de Software (UADE) ===");
        out.println("1) Humano vs Humano");
        out.println("2) Humano (blancas) vs IA");
        out.println("3) IA vs Humano (negras)");
        int mode = askInt("Modo", 1, 3);

        AiStrategyFactory aiFactory = new AiStrategyFactory(new MoveGenerator(), new Random());
        Player white;
        Player black;
        if (mode == 1) {
            white = new ConsolePlayer(askText("Nombre de las blancas", "Blancas"), in, out);
            black = new ConsolePlayer(askText("Nombre de las negras", "Negras"), in, out);
        } else {
            out.println("Dificultad: 1) Fácil  2) Media  3) Difícil");
            Difficulty level = Difficulty.values()[askInt("Dificultad", 1, 3) - 1];
            Player ai = new AiPlayer(aiFactory.forLevel(level));
            Player human = new ConsolePlayer(askText("Tu nombre", "Jugador"), in, out);
            white = mode == 2 ? human : ai;
            black = mode == 2 ? ai : human;
        }

        Game game = Game.standard();
        ChessFacade facade = new ChessFacade(game);
        ConsoleBoardRenderer renderer = new ConsoleBoardRenderer(out);
        facade.addListener(renderer);

        out.print(ConsolePlayer.HELP);
        renderer.show(facade.boardSnapshot());
        new GameController(facade, white, black).run();
        facade.removeListener(renderer);
        out.println("¡Gracias por jugar!");
    }

    private int askInt(String label, int min, int max) {
        while (true) {
            String text = askText(label + " [" + min + "-" + max + "]", String.valueOf(min));
            try {
                int value = Integer.parseInt(text);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // vuelve a preguntar
            }
            out.println("  Opción inválida");
        }
    }

    private String askText(String label, String defaultValue) {
        out.print(label + " (" + defaultValue + "): ");
        out.flush();
        try {
            String line = in.readLine();
            return line == null || line.isBlank() ? defaultValue : line.trim();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
