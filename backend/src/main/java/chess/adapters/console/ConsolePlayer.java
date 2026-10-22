package chess.adapters.console;

import chess.application.ChessFacade;
import chess.application.port.Player;
import chess.application.port.PlayerAction;
import chess.domain.model.Color;
import chess.domain.model.Move;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador de entrada: un humano que juega escribiendo en la consola.
 * Los comandos "de consulta" (ayuda, movs, tablero, historial) se resuelven acá mismo y
 * no llegan al núcleo.
 */
public final class ConsolePlayer implements Player {

    static final String HELP = """
            Comandos:
              e2 e4 | e2e4      mover (coronar: e7 e8 Q, o D/T/A/C)
              O-O | O-O-O       enroque corto / largo
              movs e2           ver destinos posibles de la pieza en e2
              tablero           volver a mostrar el tablero
              historial         jugadas realizadas
              deshacer | rehacer
              tablas            ofrecer tablas
              rendirse          abandonar la partida
              salir             cerrar el programa
            """;

    private final String name;
    private final BufferedReader in;
    private final PrintStream out;

    public ConsolePlayer(String name, BufferedReader in, PrintStream out) {
        this.name = name;
        this.in = in;
        this.out = out;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public PlayerAction nextAction(ChessFacade game) {
        while (true) {
            Color side = game.sideToMove().orElseThrow();
            out.print((side == Color.WHITE ? "Blancas" : "Negras") + " (" + name + ") > ");
            out.flush();
            String line = readLine();
            if (line == null) {
                return new PlayerAction.Quit(); // fin de la entrada
            }
            String cmd = line.trim().toLowerCase(Locale.ROOT);
            switch (cmd) {
                case "" -> { }
                case "ayuda", "help", "?" -> out.print(HELP);
                case "tablero" -> out.print(ConsoleBoardRenderer.render(game.boardSnapshot()));
                case "historial" -> printHistory(game.moveHistory());
                case "deshacer", "undo" -> {
                    return new PlayerAction.Undo();
                }
                case "rehacer", "redo" -> {
                    return new PlayerAction.Redo();
                }
                case "tablas" -> {
                    return new PlayerAction.OfferDraw();
                }
                case "rendirse" -> {
                    return new PlayerAction.Resign();
                }
                case "salir", "exit" -> {
                    return new PlayerAction.Quit();
                }
                default -> {
                    if (cmd.startsWith("movs ")) {
                        showTargets(game, cmd.substring(5).trim());
                    } else {
                        return new PlayerAction.PlayMove(line.trim());
                    }
                }
            }
        }
    }

    @Override
    public boolean acceptsDraw(ChessFacade game) {
        out.print(name + ", te ofrecen tablas. ¿Aceptás? (s/n) > ");
        out.flush();
        String answer = readLine();
        return answer != null && answer.trim().toLowerCase(Locale.ROOT).startsWith("s");
    }

    @Override
    public void notify(String message) {
        out.println("  ! " + message);
    }

    private void showTargets(ChessFacade game, String square) {
        try {
            List<String> targets = game.legalTargetsFrom(square);
            out.println(targets.isEmpty() ? "  Sin jugadas desde " + square : "  " + square + " -> " + String.join(", ", targets));
        } catch (IllegalArgumentException e) {
            notify("Casilla inválida: " + square);
        }
    }

    private void printHistory(List<Move> moves) {
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < moves.size(); i++) {
            if (i % 2 == 0) {
                sb.append(i / 2 + 1).append(". ");
            }
            sb.append(moves.get(i)).append(' ');
        }
        out.println(moves.isEmpty() ? "  (sin jugadas)" : sb.toString());
    }

    private String readLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
