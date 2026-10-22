package chess.adapters.ai;

import chess.domain.command.MoveCommand;
import chess.domain.model.Board;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.model.MoveType;
import chess.domain.model.Piece;
import chess.domain.rules.MoveGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dificultad difícil: Minimax (en su forma "negamax") con poda alfa-beta.
 * <p>
 * Optimización CPU (Unidad 04):
 * <ul>
 *   <li><b>Poda alfa-beta</b>: descarta ramas que no pueden mejorar el resultado. Minimax puro
 *       explora O(b^d) nodos (b ≈ 35 jugadas por posición, d = profundidad); con buena poda
 *       se acerca a O(b^(d/2)).</li>
 *   <li><b>Ordenar jugadas</b>: probar primero capturas valiosas hace que la poda corte antes.</li>
 *   <li><b>Profundización iterativa + presupuesto de tiempo</b>: busca a profundidad 1, 2, 3...
 *       y si se acaba el tiempo devuelve la mejor jugada de la última profundidad completa
 *       (así cumple su "SLA" de {@link #timeBudgetMillis()}).</li>
 * </ul>
 * Optimización RAM: no copia el tablero en cada nodo; hace y deshace jugadas con
 * {@link MoveCommand} sobre una sola copia (make/unmake).
 */
public final class MinimaxStrategy implements AiStrategy {

    private static final int MATE = 1_000_000;

    private final MoveGenerator moveGenerator;
    private final MaterialEvaluator evaluator;
    private final int maxDepth;
    private final long timeBudgetMillis;

    private long deadline;
    private boolean timeUp;
    private long nodes;

    public MinimaxStrategy(MoveGenerator moveGenerator, MaterialEvaluator evaluator, int maxDepth, long timeBudgetMillis) {
        this.moveGenerator = moveGenerator;
        this.evaluator = evaluator;
        this.maxDepth = maxDepth;
        this.timeBudgetMillis = timeBudgetMillis;
    }

    @Override
    public Move chooseMove(Board board, Color color) {
        List<Move> moves = ordered(board, moveGenerator.legalMoves(board, color));
        if (moves.isEmpty()) {
            throw new IllegalStateException("No legal moves");
        }
        deadline = System.currentTimeMillis() + timeBudgetMillis;
        timeUp = false;
        nodes = 0;

        Move bestMove = moves.getFirst();
        for (int depth = 1; depth <= maxDepth; depth++) {
            Move bestAtDepth = null;
            int alpha = -MATE - 1;
            for (Move move : moves) {
                MoveCommand command = new MoveCommand(board, move);
                command.execute();
                int score = -negamax(board, color.opposite(), depth - 1, -MATE - 1, -alpha, 1);
                command.undo();
                if (timeUp) {
                    break;
                }
                if (score > alpha) {
                    alpha = score;
                    bestAtDepth = move;
                }
            }
            if (timeUp) {
                break; // la profundidad incompleta no es confiable
            }
            bestMove = bestAtDepth;
            // la mejor jugada primero en la próxima iteración => más poda
            moves.remove(bestMove);
            moves.addFirst(bestMove);
            if (alpha >= MATE - maxDepth) {
                break; // encontró mate: no hace falta buscar más
            }
        }
        return bestMove;
    }

    private int negamax(Board board, Color side, int depth, int alpha, int beta, int ply) {
        nodes++;
        if ((nodes & 1023) == 0 && System.currentTimeMillis() > deadline) {
            timeUp = true;
        }
        if (timeUp) {
            return 0;
        }
        List<Move> moves = moveGenerator.legalMoves(board, side);
        if (moves.isEmpty()) {
            // sin jugadas: mate (perdió quien mueve; antes = peor) o ahogado (tablas)
            return moveGenerator.checkDetector().isInCheck(board, side) ? -MATE + ply : 0;
        }
        if (depth == 0) {
            return evaluator.evaluate(board, side);
        }
        for (Move move : ordered(board, moves)) {
            MoveCommand command = new MoveCommand(board, move);
            command.execute();
            int score = -negamax(board, side.opposite(), depth - 1, -beta, -alpha, ply + 1);
            command.undo();
            if (score >= beta) {
                return beta; // poda beta: el rival nunca permitiría esta línea
            }
            if (score > alpha) {
                alpha = score;
            }
        }
        return alpha;
    }

    /** MVV-LVA simplificado: primero capturas de piezas valiosas y coronaciones. */
    private List<Move> ordered(Board board, List<Move> moves) {
        List<Move> sorted = new ArrayList<>(moves);
        sorted.sort(Comparator.comparingInt((Move m) -> -orderingScore(board, m)));
        return sorted;
    }

    private static int orderingScore(Board board, Move move) {
        int score = 0;
        Piece target = board.getPiece(move.to());
        if (target != null) {
            score += 10 * MaterialEvaluator.value(target.getType())
                    - MaterialEvaluator.value(board.getPiece(move.from()).getType()) / 10;
        }
        if (move.type() == MoveType.PROMOTION) {
            score += MaterialEvaluator.value(move.promotion());
        }
        return score;
    }

    /** Nodos visitados en la última búsqueda (para medir el efecto de la poda). */
    public long lastSearchNodes() {
        return nodes;
    }

    @Override
    public long timeBudgetMillis() {
        return timeBudgetMillis;
    }

    @Override
    public String name() {
        return "Difícil (minimax alfa-beta, prof. " + maxDepth + ")";
    }
}
