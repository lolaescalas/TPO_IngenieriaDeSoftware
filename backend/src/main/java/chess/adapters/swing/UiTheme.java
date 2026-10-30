package chess.adapters.swing;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

/** Paleta y estilos compartidos por el adaptador Swing. */
final class UiTheme {

    static final Color BACKGROUND = new Color(15, 23, 42);
    static final Color SURFACE = new Color(30, 41, 59);
    static final Color SURFACE_LIGHT = new Color(51, 65, 85);
    static final Color TEXT = new Color(241, 245, 249);
    static final Color MUTED = new Color(148, 163, 184);
    static final Color ACCENT = new Color(20, 184, 166);
    static final Color ACCENT_DARK = new Color(13, 148, 136);
    static final Color DANGER = new Color(190, 56, 67);

    private UiTheme() {
    }

    static void surface(JComponent component) {
        component.setOpaque(true);
        component.setBackground(SURFACE);
        component.setForeground(TEXT);
    }

    static void label(JLabel label, float size, boolean bold) {
        label.setForeground(TEXT);
        label.setFont(label.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN, size));
    }

    static void input(JTextField field) {
        field.setBackground(SURFACE_LIGHT);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105)),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
    }

    static void input(JComboBox<?> box) {
        box.setBackground(SURFACE_LIGHT);
        box.setForeground(TEXT);
        box.setBorder(BorderFactory.createLineBorder(new Color(71, 85, 105)));
    }

    static void textArea(JTextArea area) {
        area.setBackground(SURFACE);
        area.setForeground(TEXT);
        area.setCaretColor(TEXT);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    static void button(JButton button, ButtonStyle style) {
        Color background = switch (style) {
            case PRIMARY -> ACCENT_DARK;
            case NEUTRAL -> SURFACE_LIGHT;
            case DANGER -> DANGER;
        };
        button.setBackground(background);
        button.setForeground(TEXT);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.brighter()),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    enum ButtonStyle { PRIMARY, NEUTRAL, DANGER }
}
