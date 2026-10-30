package chess.adapters.swing;

import chess.adapters.ai.Difficulty;
import chess.domain.model.Color;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Adaptador gráfico de entrada/salida. La interfaz solo delega acciones a la fachada;
 * las reglas permanecen en el dominio y se reutilizan sin cambios.
 */
public final class SwingApp {

    private JFrame frame;

    /** Debe invocarse desde el Event Dispatch Thread de Swing. */
    public void run() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("SwingApp debe iniciarse en el hilo de Swing");
        }
        frame = new JFrame("Ajedrez - TPO Ingeniería de Software");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 680));
        showMenu();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void showMenu() {
        JPanel page = new JPanel(new BorderLayout(16, 16));
        page.setBorder(BorderFactory.createEmptyBorder(32, 48, 32, 48));
        page.setBackground(UiTheme.BACKGROUND);

        JLabel title = new JLabel("Ajedrez", SwingConstants.CENTER);
        UiTheme.label(title, 32f, true);
        page.add(title, BorderLayout.NORTH);

        JComboBox<GameMode> mode = new JComboBox<>(GameMode.values());
        JComboBox<Difficulty> difficulty = new JComboBox<>(Difficulty.values());
        difficulty.setRenderer(new DifficultyRenderer());
        JTextField whiteName = new JTextField("Blancas", 18);
        JTextField blackName = new JTextField("Negras", 18);
        JLabel difficultyLabel = new JLabel("Dificultad de IA");
        UiTheme.input(mode);
        UiTheme.input(difficulty);
        UiTheme.input(whiteName);
        UiTheme.input(blackName);

        JPanel form = new JPanel(new GridBagLayout());
        UiTheme.surface(form);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.SURFACE_LIGHT), BorderFactory.createEmptyBorder(18, 22, 18, 22)));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(7, 7, 7, 7);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addRow(form, constraints, 0, "Modo", mode);
        addRow(form, constraints, 1, "Nombre de las blancas", whiteName);
        addRow(form, constraints, 2, "Nombre de las negras", blackName);
        addRow(form, constraints, 3, difficultyLabel, difficulty);

        Runnable updateMode = () -> {
            GameMode selected = (GameMode) mode.getSelectedItem();
            boolean whiteAi = selected == GameMode.AI_WHITE_VS_HUMAN;
            boolean blackAi = selected == GameMode.HUMAN_WHITE_VS_AI;
            boolean hasAi = whiteAi || blackAi;

            whiteName.setEnabled(!whiteAi);
            blackName.setEnabled(!blackAi);
            if (whiteAi) {
                whiteName.setText("IA");
            } else if (whiteName.getText().equals("IA")) {
                whiteName.setText("Blancas");
            }
            if (blackAi) {
                blackName.setText("IA");
            } else if (blackName.getText().equals("IA")) {
                blackName.setText("Negras");
            }
            difficulty.setEnabled(hasAi);
            difficultyLabel.setEnabled(hasAi);
        };
        mode.addActionListener(event -> updateMode.run());
        updateMode.run();

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);

        JButton start = new JButton("Comenzar partida");
        start.addActionListener(event -> {
            GameMode selected = (GameMode) mode.getSelectedItem();
            Color aiColor = switch (selected) {
                case HUMAN_VS_HUMAN -> null;
                case HUMAN_WHITE_VS_AI -> Color.BLACK;
                case AI_WHITE_VS_HUMAN -> Color.WHITE;
            };
            GameSetup setup = new GameSetup(
                    nameOrDefault(whiteName.getText(), "Blancas"),
                    nameOrDefault(blackName.getText(), "Negras"),
                    aiColor,
                    (Difficulty) difficulty.getSelectedItem());
            showGame(setup);
        });

        JButton exit = new JButton("Salir");
        exit.addActionListener(event -> frame.dispose());
        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        UiTheme.button(start, UiTheme.ButtonStyle.PRIMARY);
        UiTheme.button(exit, UiTheme.ButtonStyle.NEUTRAL);
        actions.add(start);
        actions.add(exit);
        page.add(actions, BorderLayout.SOUTH);

        setPage(page);
    }

    private void showGame(GameSetup setup) {
        SwingGamePanel game = new SwingGamePanel(setup, this::showMenu);
        setPage(game);
    }

    private void setPage(JPanel page) {
        frame.setContentPane(page);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }

    private static void addRow(JPanel panel, GridBagConstraints constraints, int row, String label, java.awt.Component component) {
        addRow(panel, constraints, row, new JLabel(label), component);
    }

    private static void addRow(JPanel panel, GridBagConstraints constraints, int row, JLabel label, java.awt.Component component) {
        UiTheme.label(label, 14f, false);
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.weightx = 0;
        panel.add(label, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(component, constraints);
    }

    private static String nameOrDefault(String name, String fallback) {
        return name == null || name.isBlank() ? fallback : name.trim();
    }

    private enum GameMode {
        HUMAN_VS_HUMAN("Humano vs. humano"),
        HUMAN_WHITE_VS_AI("Humano (blancas) vs. IA"),
        AI_WHITE_VS_HUMAN("IA (blancas) vs. humano");

        private final String label;

        GameMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class DifficultyRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                                                               int index, boolean isSelected, boolean cellHasFocus) {
            Object label = value instanceof Difficulty level ? difficultyLabel(level) : value;
            JLabel renderer = (JLabel) super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
            renderer.setBackground(isSelected ? UiTheme.ACCENT_DARK : UiTheme.SURFACE_LIGHT);
            renderer.setForeground(UiTheme.TEXT);
            return renderer;
        }
    }

    static String difficultyLabel(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "Fácil (aleatoria)";
            case MEDIUM -> "Media (prioriza material)";
            case HARD -> "Difícil (minimax)";
        };
    }
}

record GameSetup(String whiteName, String blackName, Color aiColor, Difficulty difficulty) {
}
