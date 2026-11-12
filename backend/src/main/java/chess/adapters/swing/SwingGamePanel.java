package chess.adapters.swing;

import chess.adapters.ai.AiPlayer;
import chess.adapters.ai.AiStrategy;
import chess.adapters.ai.AiStrategyFactory;
import chess.application.ChessFacade;
import chess.domain.event.GameEventListener;
import chess.domain.event.MoveEvent;
import chess.domain.game.IllegalMoveException;
import chess.domain.game.Game;
import chess.domain.model.Board;
import chess.domain.model.Move;
import chess.domain.model.PieceType;
import chess.domain.rules.GameResult;
import chess.domain.rules.MoveGenerator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Tablero gráfico controlado únicamente con mouse. */
final class SwingGamePanel extends JPanel {

    private final GameSetup setup;
    private final Runnable returnToMenu;
    private final ChessFacade facade = new ChessFacade(Game.standard());
    private final AiStrategy aiStrategy;
    private final ChessBoardPanel boardView = new ChessBoardPanel(this::onSquareClicked);
    private final Set<String> targets = new HashSet<>();
    private final JLabel status = new JLabel("", SwingConstants.CENTER);
    private final JTextArea history = new JTextArea();
    private final JButton undo = new JButton("Deshacer");
    private final JButton redo = new JButton("Rehacer");
    private final JButton draw = new JButton("Ofrecer tablas");
    private final JButton resign = new JButton("Rendirse");
    private final GameEventListener listener = new UiListener();

    private String selectedSquare;
    private String note = "Partida iniciada";
    private boolean aiThinking;

    SwingGamePanel(GameSetup setup, Runnable returnToMenu) {
        super(new BorderLayout(16, 16));
        this.setup = setup;
        this.returnToMenu = returnToMenu;
        this.aiStrategy = setup.aiColor() == null
                ? null
                : new AiStrategyFactory(new MoveGenerator(), new Random()).forLevel(setup.difficulty());

        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        setBackground(UiTheme.BACKGROUND);
        facade.addListener(listener);
        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createControls(), BorderLayout.SOUTH);
        refresh();
        startAiTurnIfNeeded();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        UiTheme.surface(header);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.SURFACE_LIGHT), BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        JLabel players = new JLabel(playersLabel(), SwingConstants.CENTER);
        UiTheme.label(players, 18f, true);
        UiTheme.label(status, 14f, false);
        header.add(players);
        header.add(status);
        return header;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setOpaque(false);
        content.add(boardView, BorderLayout.CENTER);

        history.setEditable(false);
        history.setFocusable(false);
        history.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        UiTheme.textArea(history);
        JScrollPane scroll = new JScrollPane(history);
        scroll.setPreferredSize(new Dimension(260, 0));
        scroll.setBorder(BorderFactory.createTitledBorder("Historial"));
        scroll.getViewport().setBackground(UiTheme.SURFACE);
        scroll.setBackground(UiTheme.SURFACE);
        scroll.setForeground(UiTheme.TEXT);
        content.add(scroll, BorderLayout.EAST);
        return content;
    }

    private JPanel createControls() {
        JPanel controls = new JPanel(new GridLayout(1, 6, 8, 0));
        controls.setOpaque(false);
        undo.addActionListener(event -> undo());
        redo.addActionListener(event -> redo());
        draw.addActionListener(event -> offerDraw());
        resign.addActionListener(event -> resign());
        JButton newGame = new JButton("Nueva partida");
        newGame.addActionListener(event -> {
            facade.removeListener(listener);
            returnToMenu.run();
        });
        JButton exit = new JButton("Salir");
        exit.addActionListener(event -> System.exit(0));
        UiTheme.button(undo, UiTheme.ButtonStyle.NEUTRAL);
        UiTheme.button(redo, UiTheme.ButtonStyle.NEUTRAL);
        UiTheme.button(draw, UiTheme.ButtonStyle.NEUTRAL);
        UiTheme.button(resign, UiTheme.ButtonStyle.DANGER);
        UiTheme.button(newGame, UiTheme.ButtonStyle.PRIMARY);
        UiTheme.button(exit, UiTheme.ButtonStyle.NEUTRAL);
        controls.add(undo);
        controls.add(redo);
        controls.add(draw);
        controls.add(resign);
        controls.add(newGame);
        controls.add(exit);
        return controls;
    }

    private void onSquareClicked(String square) {
        if (facade.isOver() || aiThinking || isAiTurn()) {
            return;
        }
        if (selectedSquare == null) {
            select(square);
            return;
        }
        if (selectedSquare.equals(square)) {
            clearSelection();
            refresh();
            return;
        }
        if (targets.contains(square)) {
            playSelectedMove(square);
            return;
        }
        select(square);
    }

    private void select(String square) {
        List<String> legalTargets = facade.legalTargetsFrom(square);
        if (legalTargets.isEmpty()) {
            note = "Elegí una pieza del jugador que tiene el turno";
            refresh();
            return;
        }
        selectedSquare = square;
        targets.clear();
        targets.addAll(legalTargets);
        note = "Elegí el destino de " + square;
        refresh();
    }

    private void playSelectedMove(String destination) {
        String notation = selectedSquare + destination;
        List<Move> options = facade.legalMoves().stream()
                .filter(move -> move.from().toString().equals(selectedSquare)
                        && move.to().toString().equals(destination)
                        && move.promotion() != null)
                .toList();
        if (!options.isEmpty()) {
            PieceType promotion = choosePromotion();
            if (promotion == null) {
                return;
            }
            notation += "=" + promotion.symbol();
        }
        try {
            facade.play(notation);
            clearSelection();
            refresh();
            startAiTurnIfNeeded();
        } catch (IllegalMoveException exception) {
            note = exception.getMessage();
            clearSelection();
            refresh();
        }
    }

    private PieceType choosePromotion() {
        Object selected = JOptionPane.showInputDialog(this, "Elegí la pieza para coronar", "Coronación",
                JOptionPane.QUESTION_MESSAGE, null,
                new Object[]{"Dama", "Torre", "Alfil", "Caballo"}, "Dama");
        if (selected == null) {
            return null;
        }
        return switch (selected.toString()) {
            case "Torre" -> PieceType.ROOK;
            case "Alfil" -> PieceType.BISHOP;
            case "Caballo" -> PieceType.KNIGHT;
            default -> PieceType.QUEEN;
        };
    }

    private void undo() {
        if (!canHumanAct()) {
            return;
        }
        int plies = setup.aiColor() == null ? 1 : 2;
        int undone = 0;
        while (undone < plies && facade.undo()) {
            undone++;
        }
        note = undone == 0 ? "No hay jugadas para deshacer" : "Se deshicieron " + undone + " jugada(s)";
        clearSelection();
        refresh();
        startAiTurnIfNeeded();
    }

    private void redo() {
        if (!canHumanAct()) {
            return;
        }
        int plies = setup.aiColor() == null ? 1 : 2;
        int redone = 0;
        while (redone < plies && facade.redo()) {
            redone++;
        }
        note = redone == 0 ? "No hay jugadas para rehacer" : "Se rehicieron " + redone + " jugada(s)";
        clearSelection();
        refresh();
        startAiTurnIfNeeded();
    }

    private void offerDraw() {
        if (!canHumanAct()) {
            return;
        }
        boolean accepted;
        if (setup.aiColor() != null) {
            accepted = new AiPlayer(aiStrategy).acceptsDraw(facade);
        } else {
            String opponent = playerName(facade.sideToMove().orElseThrow().opposite());
            accepted = JOptionPane.showConfirmDialog(this, opponent + ", ¿aceptás las tablas?",
                    "Oferta de tablas", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }
        if (accepted) {
            facade.agreeDraw();
        } else {
            note = "Las tablas fueron rechazadas";
            refresh();
        }
    }

    private void resign() {
        if (!canHumanAct()) {
            return;
        }
        chess.domain.model.Color side = facade.sideToMove().orElseThrow();
        int answer = JOptionPane.showConfirmDialog(this, playerName(side) + ", ¿querés rendirte?",
                "Confirmar rendición", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            facade.resign(side);
        }
    }

    private void startAiTurnIfNeeded() {
        if (!isAiTurn() || aiThinking || facade.isOver()) {
            return;
        }
        aiThinking = true;
        note = "La IA está pensando…";
        refresh();
        new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() {
                return aiStrategy.chooseMove(facade.boardSnapshot(), setup.aiColor());
            }

            @Override
            protected void done() {
                try {
                    facade.play(get().toString());
                } catch (Exception exception) {
                    note = "La IA no pudo jugar: " + exception.getMessage();
                } finally {
                    aiThinking = false;
                    refresh();
                }
            }
        }.execute();
    }

    private boolean canHumanAct() {
        return !facade.isOver() && !aiThinking && !isAiTurn();
    }

    private boolean isAiTurn() {
        return setup.aiColor() != null && facade.sideToMove().filter(setup.aiColor()::equals).isPresent();
    }

    private void clearSelection() {
        selectedSquare = null;
        targets.clear();
    }

    private void refresh() {
        Board board = facade.boardSnapshot();
        List<Move> moves = facade.moveHistory();
        Set<String> kingsInCheck = new HashSet<>();
        for (chess.domain.model.Color color : chess.domain.model.Color.values()) {
            if (facade.isInCheck(color)) {
                board.findKing(color).ifPresent(king -> kingsInCheck.add(king.toString()));
            }
        }
        boardView.setState(board, selectedSquare, targets,
                moves.isEmpty() ? null : moves.getLast(), kingsInCheck, canHumanAct());
        history.setText(formatHistory(moves));
        history.setCaretPosition(history.getDocument().getLength());
        status.setText(statusText());
        boolean enabled = canHumanAct();
        undo.setEnabled(enabled);
        redo.setEnabled(enabled);
        draw.setEnabled(enabled);
        resign.setEnabled(enabled);
    }

    private String statusText() {
        if (facade.isOver()) {
            return facade.result().orElseThrow().toString();
        }
        if (aiThinking) {
            return note;
        }
        chess.domain.model.Color side = facade.sideToMove().orElseThrow();
        return "Turno de " + playerName(side) + " — " + note;
    }

    private String playersLabel() {
        String ai = setup.aiColor() == null ? "" : "  ·  " + SwingApp.difficultyLabel(setup.difficulty());
        return "Blancas: " + setup.whiteName() + "    vs.    Negras: " + setup.blackName() + ai;
    }

    private String playerName(chess.domain.model.Color color) {
        return color == chess.domain.model.Color.WHITE ? setup.whiteName() : setup.blackName();
    }

    private static String formatHistory(List<Move> moves) {
        if (moves.isEmpty()) {
            return "(sin jugadas todavía)";
        }
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < moves.size(); index++) {
            if (index % 2 == 0) {
                text.append(index / 2 + 1).append(". ");
            }
            text.append(moves.get(index)).append(index % 2 == 1 ? "\n" : "   ");
        }
        return text.toString();
    }

    private final class UiListener implements GameEventListener {
        @Override
        public void onMoveMade(MoveEvent event) {
            onUiThread(() -> {
                note = playerName(event.mover()) + " jugó " + event.move();
                refresh();
            });
        }

        @Override
        public void onCheck(chess.domain.model.Color colorInCheck) {
            onUiThread(() -> {
                note = "¡Jaque a " + playerName(colorInCheck) + "!";
                refresh();
            });
        }

        @Override
        public void onMoveUndone(Move move) {
            onUiThread(() -> {
                note = "Se deshizo " + move;
                refresh();
            });
        }

        @Override
        public void onGameOver(GameResult result) {
            onUiThread(() -> {
                note = result.toString();
                refresh();
                JOptionPane.showMessageDialog(SwingGamePanel.this, result, "Fin de la partida",
                        JOptionPane.INFORMATION_MESSAGE);
            });
        }

        private void onUiThread(Runnable action) {
            if (SwingUtilities.isEventDispatchThread()) {
                action.run();
            } else {
                SwingUtilities.invokeLater(action);
            }
        }
    }
}
