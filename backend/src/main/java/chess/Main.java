package chess;

import chess.adapters.console.ConsoleApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Punto de entrada y <b>composition root</b>: el único lugar donde se eligen las
 * implementaciones concretas (consola, IA) y se inyectan. El núcleo nunca hace "new" de
 * un adaptador.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        new ConsoleApp(in, out).run();
    }
}
