package chess.adapters.swing;

import chess.domain.model.Board;
import chess.domain.model.Move;
import chess.domain.model.Piece;
import chess.domain.model.Position;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/** Tablero responsivo dibujado a medida; admite click y arrastrar/soltar. */
final class ChessBoardPanel extends JPanel {

    private static final Color LIGHT_WOOD = new Color(229, 202, 154);
    private static final Color DARK_WOOD = new Color(123, 75, 48);
    private static final Color LAST_MOVE = new Color(61, 174, 197, 125);
    private static final Color MOVE_TARGET = new Color(43, 54, 42, 108);
    private static final Color CAPTURE_TARGET = new Color(54, 61, 63, 92);
    private static final Color SELECTED = new Color(250, 204, 21);
    private static final Color CHECK = new Color(239, 68, 68, 180);
    private static final Map<Character, BufferedImage> PIECES = loadPieces();

    private final Consumer<String> squareClick;
    private Board board;
    private Set<String> targets = Set.of();
    private Set<String> kingsInCheck = Set.of();
    private String selectedSquare;
    private Move lastMove;
    private boolean interactionEnabled;
    private String pressedSquare;
    private boolean dragging;
    private Point mouse;

    ChessBoardPanel(Consumer<String> squareClick) {
        this.squareClick = squareClick;
        setPreferredSize(new Dimension(620, 620));
        setBackground(UiTheme.BACKGROUND);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        MouseAdapter input = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (!interactionEnabled) {
                    return;
                }
                pressedSquare = squareAt(event.getPoint());
                dragging = false;
                mouse = event.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (pressedSquare != null) {
                    dragging = true;
                    mouse = event.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (!interactionEnabled || pressedSquare == null) {
                    return;
                }
                String releasedSquare = squareAt(event.getPoint());
                if (releasedSquare != null) {
                    if (dragging && !pressedSquare.equals(releasedSquare)) {
                        if (!pressedSquare.equals(selectedSquare)) {
                            squareClick.accept(pressedSquare);
                        }
                        squareClick.accept(releasedSquare);
                    } else {
                        squareClick.accept(releasedSquare);
                    }
                }
                pressedSquare = null;
                dragging = false;
                mouse = null;
                repaint();
            }
        };
        addMouseListener(input);
        addMouseMotionListener(input);
    }

    void setState(Board board, String selectedSquare, Set<String> targets, Move lastMove, Set<String> kingsInCheck,
                  boolean interactionEnabled) {
        this.board = board;
        this.selectedSquare = selectedSquare;
        this.targets = Set.copyOf(targets);
        this.lastMove = lastMove;
        this.kingsInCheck = Set.copyOf(kingsInCheck);
        this.interactionEnabled = interactionEnabled;
        setCursor(Cursor.getPredefinedCursor(interactionEnabled ? Cursor.HAND_CURSOR : Cursor.WAIT_CURSOR));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (board == null) {
            return;
        }
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            BoardGeometry geometry = geometry();
            paintBackdrop(g, geometry);
            paintSquares(g, geometry);
            paintCoordinates(g, geometry);
            paintPieces(g, geometry);
            paintDraggedPiece(g, geometry);
        } finally {
            g.dispose();
        }
    }

    private void paintBackdrop(Graphics2D g, BoardGeometry geometry) {
        int shadow = 8;
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(geometry.left + shadow, geometry.top + shadow, geometry.size, geometry.size, 18, 18);
        g.setPaint(new GradientPaint(geometry.left, geometry.top, new Color(213, 181, 133),
                geometry.left + geometry.size, geometry.top + geometry.size, new Color(83, 53, 38)));
        g.fillRoundRect(geometry.left - 4, geometry.top - 4, geometry.size + 8, geometry.size + 8, 18, 18);
    }

    private void paintSquares(Graphics2D g, BoardGeometry geometry) {
        RoundRectangle2D clip = new RoundRectangle2D.Double(geometry.left, geometry.top, geometry.size, geometry.size, 12, 12);
        g.clip(clip);
        for (int row = 0; row < Position.SIZE; row++) {
            for (int column = 0; column < Position.SIZE; column++) {
                Position position = Position.of(row, column);
                String square = position.toString();
                int x = geometry.left + column * geometry.cell;
                int y = geometry.top + row * geometry.cell;
                paintWoodSquare(g, x, y, geometry.cell, row, column);
                if (lastMove != null && (lastMove.from().equals(position) || lastMove.to().equals(position))) {
                    g.setColor(LAST_MOVE);
                    g.fillRect(x, y, geometry.cell, geometry.cell);
                }
                if (kingsInCheck.contains(square)) {
                    g.setColor(CHECK);
                    g.fillRect(x, y, geometry.cell, geometry.cell);
                }
                if (targets.contains(square)) {
                    if (board.getPiece(position) == null) {
                        g.setColor(MOVE_TARGET);
                        int diameter = Math.max(14, geometry.cell / 4);
                        g.fillOval(x + (geometry.cell - diameter) / 2, y + (geometry.cell - diameter) / 2,
                                diameter, diameter);
                    } else {
                        // El aro se pinta antes de la pieza: queda como una pista discreta detrás de ella.
                        int inset = Math.max(7, geometry.cell / 10);
                        g.setColor(CAPTURE_TARGET);
                        g.setStroke(new BasicStroke(Math.max(2, geometry.cell / 28f)));
                        g.drawOval(x + inset, y + inset, geometry.cell - 2 * inset, geometry.cell - 2 * inset);
                    }
                }
                if (square.equals(selectedSquare)) {
                    g.setColor(SELECTED);
                    g.setStroke(new BasicStroke(Math.max(3, geometry.cell / 18f)));
                    g.drawRect(x + 3, y + 3, geometry.cell - 6, geometry.cell - 6);
                }
            }
        }
    }

    private static void paintWoodSquare(Graphics2D g, int x, int y, int cell, int row, int column) {
        Color base = (row + column) % 2 == 0 ? LIGHT_WOOD : DARK_WOOD;
        Color edge = (row + column) % 2 == 0 ? base.darker() : base.brighter();
        g.setPaint(new GradientPaint(x, y, base.brighter(), x + cell, y + cell, edge));
        g.fillRect(x, y, cell, cell);

        int grainCount = Math.max(2, cell / 34);
        for (int grain = 0; grain < grainCount; grain++) {
            int seed = row * 37 + column * 53 + grain * 29;
            int grainY = y + 8 + Math.floorMod(seed, Math.max(1, cell - 16));
            int wave = Math.max(3, cell / 14);
            g.setColor((row + column) % 2 == 0
                    ? new Color(92, 57, 35, 25)
                    : new Color(255, 229, 188, 23));
            g.setStroke(new BasicStroke(Math.max(1f, cell / 70f)));
            g.draw(new QuadCurve2D.Float(x + 4, grainY, x + cell / 2f, grainY + wave,
                    x + cell - 4, grainY + (seed % (2 * wave + 1) - wave)));
        }
    }

    private void paintCoordinates(Graphics2D g, BoardGeometry geometry) {
        g.setColor(UiTheme.MUTED);
        g.setFont(getFont().deriveFont(Font.BOLD, Math.max(11f, geometry.cell / 8f)));
        FontMetrics metrics = g.getFontMetrics();
        for (int index = 0; index < Position.SIZE; index++) {
            String file = String.valueOf((char) ('a' + index));
            int x = geometry.left + index * geometry.cell + (geometry.cell - metrics.stringWidth(file)) / 2;
            g.drawString(file, x, geometry.top + geometry.size + metrics.getAscent() + 7);
            String rank = String.valueOf(Position.SIZE - index);
            int y = geometry.top + index * geometry.cell + (geometry.cell + metrics.getAscent()) / 2;
            g.drawString(rank, geometry.left - metrics.stringWidth(rank) - 9, y);
        }
    }

    private void paintPieces(Graphics2D g, BoardGeometry geometry) {
        for (int row = 0; row < Position.SIZE; row++) {
            for (int column = 0; column < Position.SIZE; column++) {
                Position position = Position.of(row, column);
                if (dragging && position.toString().equals(pressedSquare)) {
                    continue;
                }
                Piece piece = board.getPiece(position);
                if (piece != null) {
                    drawPiece(g, piece, geometry.left + column * geometry.cell + geometry.cell / 2,
                            geometry.top + row * geometry.cell + geometry.cell / 2, geometry.cell, 1f);
                }
            }
        }
    }

    private void paintDraggedPiece(Graphics2D g, BoardGeometry geometry) {
        if (!dragging || mouse == null || pressedSquare == null) {
            return;
        }
        Piece piece = board.getPiece(Position.parse(pressedSquare));
        if (piece != null) {
            drawPiece(g, piece, mouse.x, mouse.y, geometry.cell, .78f);
        }
    }

    private void drawPiece(Graphics2D g, Piece piece, int centerX, int centerY, int cell, float alpha) {
        BufferedImage sprite = PIECES.get(piece.symbol());
        if (sprite != null) {
            double scale = Math.min((cell * .80) / sprite.getWidth(), (cell * .83) / sprite.getHeight());
            int width = Math.max(1, (int) Math.round(sprite.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(sprite.getHeight() * scale));
            int destinationX = centerX - width / 2;
            // Todas las piezas apoyan su base en la misma línea; no dependen del alto de cada sprite.
            int baseline = centerY + Math.round(cell * .38f);
            int destinationY = baseline - height;
            java.awt.Composite previousComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.drawImage(sprite, destinationX, destinationY, destinationX + width, destinationY + height,
                    0, 0, sprite.getWidth(), sprite.getHeight(), null);
            g.setComposite(previousComposite);
            return;
        }
        String glyph = glyph(piece.symbol());
        Font font = new Font("DejaVu Sans", Font.PLAIN, Math.max(24, (int) (cell * .72)));
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        int x = centerX - metrics.stringWidth(glyph) / 2;
        int y = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;
        int transparency = Math.round(255 * alpha);
        g.setColor(new Color(0, 0, 0, Math.round(160 * alpha)));
        g.drawString(glyph, x + 2, y + 3);
        Color color = piece.getColor() == chess.domain.model.Color.WHITE
                ? new Color(255, 250, 240, transparency)
                : new Color(31, 41, 55, transparency);
        g.setColor(color);
        g.drawString(glyph, x, y);
    }

    private static SpriteCell spriteFor(char symbol) {
        return switch (symbol) {
            case 'K' -> new SpriteCell(0, 0);
            case 'Q' -> new SpriteCell(1, 0);
            case 'R' -> new SpriteCell(2, 0);
            case 'B' -> new SpriteCell(3, 0);
            case 'N' -> new SpriteCell(0, 1);
            case 'P' -> new SpriteCell(1, 1);
            case 'k' -> new SpriteCell(2, 1);
            case 'q' -> new SpriteCell(3, 1);
            case 'r' -> new SpriteCell(0, 2);
            case 'b' -> new SpriteCell(1, 2);
            case 'n' -> new SpriteCell(2, 2);
            case 'p' -> new SpriteCell(3, 2);
            default -> null;
        };
    }

    private static Map<Character, BufferedImage> loadPieces() {
        BufferedImage sheet = loadPieceSheet();
        if (sheet == null) {
            return Map.of();
        }
        Map<Character, BufferedImage> pieces = new HashMap<>();
        for (char symbol : "KQRBNPkqrbnp".toCharArray()) {
            pieces.put(symbol, cropSprite(sheet, spriteFor(symbol)));
        }
        return Map.copyOf(pieces);
    }

    private static BufferedImage loadPieceSheet() {
        try (InputStream image = ChessBoardPanel.class.getResourceAsStream("/chess/adapters/swing/pieces-sprite.png")) {
            return image == null ? null : ImageIO.read(image);
        } catch (IOException ignored) {
            return null; // La fuente Unicode conserva la jugabilidad si faltara el recurso.
        }
    }

    /** Recorta cada celda por sus píxeles visibles para que las siluetas sean realmente simétricas. */
    private static BufferedImage cropSprite(BufferedImage sheet, SpriteCell sprite) {
        int sourceWidth = sheet.getWidth() / 4;
        int sourceHeight = sheet.getHeight() / 3;
        int startX = sprite.column() * sourceWidth;
        int startY = sprite.row() * sourceHeight;
        int minX = sourceWidth;
        int minY = sourceHeight;
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < sourceHeight; y++) {
            for (int x = 0; x < sourceWidth; x++) {
                if (((sheet.getRGB(startX + x, startY + y) >>> 24) & 0xFF) > 12) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX < 0) {
            return sheet.getSubimage(startX, startY, sourceWidth, sourceHeight);
        }
        int margin = 3;
        minX = Math.max(0, minX - margin);
        minY = Math.max(0, minY - margin);
        maxX = Math.min(sourceWidth - 1, maxX + margin);
        maxY = Math.min(sourceHeight - 1, maxY + margin);
        return sheet.getSubimage(startX + minX, startY + minY, maxX - minX + 1, maxY - minY + 1);
    }

    private String squareAt(Point point) {
        BoardGeometry geometry = geometry();
        if (point.x < geometry.left || point.x >= geometry.left + geometry.size
                || point.y < geometry.top || point.y >= geometry.top + geometry.size) {
            return null;
        }
        int row = (point.y - geometry.top) / geometry.cell;
        int column = (point.x - geometry.left) / geometry.cell;
        return Position.of(row, column).toString();
    }

    private BoardGeometry geometry() {
        int available = Math.max(8, Math.min(getWidth() - 52, getHeight() - 52));
        int cell = Math.max(1, available / Position.SIZE);
        int size = cell * Position.SIZE;
        return new BoardGeometry((getWidth() - size) / 2, (getHeight() - size) / 2 - 4, size, cell);
    }

    private static String glyph(char symbol) {
        return switch (symbol) {
            case 'K' -> "♔";
            case 'Q' -> "♕";
            case 'R' -> "♖";
            case 'B' -> "♗";
            case 'N' -> "♘";
            case 'P' -> "♙";
            case 'k' -> "♚";
            case 'q' -> "♛";
            case 'r' -> "♜";
            case 'b' -> "♝";
            case 'n' -> "♞";
            case 'p' -> "♟";
            default -> "?";
        };
    }

    private record BoardGeometry(int left, int top, int size, int cell) {
    }

    private record SpriteCell(int column, int row) {
    }
}
