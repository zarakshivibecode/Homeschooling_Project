package id.edu.homeschool.ui.design;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

/** Factory methods for the shared components. Pages compose these instead of restyling Swing defaults. */
public final class Ui {

    public enum Variant { PRIMARY, SECONDARY, GHOST, LINK, DANGER }

    private Ui() { }

    /* ---------- layout helpers ---------- */

    public static JPanel panel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Theme.CANVAS);
        return panel;
    }

    public static JPanel transparent(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Theme.CANVAS);
        panel.setOpaque(false);
        return panel;
    }

    public static JPanel row() {
        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, Theme.SPACE_2, 0));
        panel.setBackground(Theme.CANVAS);
        panel.setOpaque(false);
        return panel;
    }

    /* ---------- surfaces ---------- */

    /** A frosted card used to group related content. */
    public static JPanel card() {
        return card(Theme.GLASS_CARD);
    }

    /** A frosted card with a custom body colour. */
    public static JPanel card(Color background) {
        GlassPanel panel = new GlassPanel(new BorderLayout(0, Theme.SPACE_4));
        panel.setFill(background);
        panel.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_6, Theme.SPACE_6,
                Theme.SPACE_6, Theme.SPACE_6));
        return panel;
    }

    /** A compact frosted card used for small summaries. */
    public static JPanel tile() {
        GlassPanel panel = new GlassPanel(new BorderLayout(0, Theme.SPACE_2));
        panel.setFill(Theme.GLASS_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_4, Theme.SPACE_4,
                Theme.SPACE_4, Theme.SPACE_4));
        return panel;
    }

    public static Border padding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    /* ---------- text ---------- */

    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_13);
        label.setForeground(Theme.TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_13);
        label.setForeground(Theme.TEXT_MUTED);
        return label;
    }

    public static JLabel subtle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_12);
        label.setForeground(Theme.TEXT_SUBTLE);
        return label;
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_16_BOLD);
        label.setForeground(Theme.TEXT);
        return label;
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_20);
        label.setForeground(Theme.TEXT);
        return label;
    }

    /** A paragraph that wraps itself to the width that is available. */
    public static WrappingLabel paragraph(String text) {
        return new WrappingLabel(text);
    }

    /** A small pill used for statuses and metadata. */
    public static StatusBadge badge(String text, Theme.StatusStyle style) {
        return new StatusBadge(text, style);
    }

    /** A neutral pill used to mark demo records. */
    public static StatusBadge tag(String text) {
        StatusBadge badge = new StatusBadge(text, new Theme.StatusStyle(Theme.ACCENT, Theme.ACCENT_SOFT, Theme.ACCENT_BORDER));
        badge.setFont(Theme.FONT_10_BOLD);
        return badge.withoutDot();
    }

    /** The heading block used at the top of a card. */
    public static JPanel cardHeader(String title, String description) {
        JPanel head = new JPanel(new BorderLayout(0, 2));
        head.setBackground(Theme.CANVAS);
        head.setOpaque(false);
        head.add(heading(title), BorderLayout.NORTH);
        if (description != null && !description.isBlank()) {
            head.add(subtle(description), BorderLayout.CENTER);
        }
        return head;
    }

    /* ---------- controls ---------- */

    public static JButton button(String text, Variant variant) {
        GlassButton button = new GlassButton(text, variant);
        button.setFont(Theme.FONT_13);
        button.applyPreferredSize();
        return button;
    }

    public static JTextField textField() {
        return new ModernTextField();
    }

    public static JTextField textField(String value) {
        ModernTextField field = new ModernTextField();
        field.setText(value == null ? "" : value);
        return field;
    }

    public static javax.swing.JPasswordField passwordField() {
        return new ModernPasswordField();
    }

    public static <T> JComboBox<T> combo() {
        return new ModernComboBox<>();
    }

    public static JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.GLASS_TABLE);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setOpaque(false);
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        return scroll;
    }

    /** A scroll area that keeps the glass surface of the card visible behind the content. */
    public static JScrollPane scrollTransparent(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.CANVAS);
        scroll.setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setOpaque(false);
        return scroll;
    }

    /**
     * A scroll area for a whole page. The content follows the width of the viewport, so a
     * responsive layout inside it is always computed with the width that is really available.
     */
    public static JScrollPane scrollPage(JComponent content) {
        ScrollablePanel panel = new ScrollablePanel(new BorderLayout());
        panel.setBackground(Theme.CANVAS);
        panel.setOpaque(false);
        panel.add(content, BorderLayout.CENTER);
        return scrollTransparent(panel);
    }

    /* ---------- tables ---------- */

    /** Applies the shared table styling: readable headers, calm rows and a soft selection. */
    public static void styleTable(JTable table) {
        table.setFont(Theme.FONT_13);
        table.setForeground(Theme.TEXT);
        table.setBackground(Theme.GLASS_TABLE);
        table.setSelectionBackground(Theme.alpha(Theme.ACCENT, 32));
        table.setSelectionForeground(Theme.TEXT);
        table.setGridColor(Theme.alpha(Theme.BORDER, 210));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setRowHeight(44);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setFocusable(true);

        table.getTableHeader().setFont(Theme.FONT_11_BOLD);
        table.getTableHeader().setForeground(Theme.TEXT_MUTED);
        table.getTableHeader().setBackground(new Color(0xF1, 0xF5, 0xF9, 235));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));

        // The row under the pointer is tinted so the table reads as interactive.
        HoverState hover = new HoverState();
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent event) {
                int row = table.rowAtPoint(event.getPoint());
                if (row != hover.row) {
                    hover.row = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent event) {
                hover.row = -1;
                table.repaint();
            }
        });

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable owner, Object value, boolean selected,
                    boolean focused, int row, int column) {
                super.getTableCellRendererComponent(owner, value, selected, focused, row, column);
                setFont(Theme.FONT_13);
                setBorder(BorderFactory.createEmptyBorder(0, Theme.SPACE_4, 0, Theme.SPACE_4));
                if (!selected) {
                    if (row == hover.row) {
                        setBackground(Theme.alpha(Theme.ACCENT, 18));
                    } else {
                        setBackground(row % 2 == 0 ? Theme.GLASS_TABLE : Theme.GLASS_ROW_ALT);
                    }
                    setForeground(Theme.TEXT);
                } else {
                    setBackground(Theme.alpha(Theme.ACCENT, 34));
                    setForeground(Theme.TEXT);
                }
                setText(shorten(owner, column, getText()));
                return this;
            }
        };
        for (int column = 0; column < table.getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setCellRenderer(renderer);
        }
    }

    /** Holds the row that is currently under the pointer. */
    private static final class HoverState {
        private int row = -1;
    }

    /** Shortens a cell value so a long text ends with an ellipsis instead of being cut off. */
    private static String shorten(JTable table, int column, String text) {
        if (text == null || text.isEmpty() || column >= table.getColumnCount()) return text;
        int width = table.getColumnModel().getColumn(column).getWidth() - Theme.SPACE_4 * 2;
        if (width <= 0) return text;
        FontMetrics metrics = table.getFontMetrics(Theme.FONT_13);
        if (metrics.stringWidth(text) <= width) return text;
        StringBuilder shortened = new StringBuilder(text);
        while (shortened.length() > 1 && metrics.stringWidth(shortened + "…") > width) {
            shortened.setLength(shortened.length() - 1);
        }
        return shortened + "…";
    }

    /* ---------- helpers ---------- */

    /** Wraps a label in a small pill so it can be placed inside a table cell or a header. */
    public static StatusBadge pill(String text, Color foreground, Color background) {
        return new StatusBadge(text, new Theme.StatusStyle(foreground, background));
    }

    /** Builds a caption/value pair used across the detail dialogs and the profile page. */
    public static JPanel definition(String caption, String value) {
        JPanel row = new JPanel(new BorderLayout(0, 3));
        row.setBackground(Theme.CANVAS);
        row.setOpaque(false);
        JLabel label = subtle(caption);
        label.setFont(Theme.FONT_11);
        JLabel content = label(value == null || value.isBlank() ? "—" : value);
        content.setFont(Theme.FONT_14);
        row.add(label, BorderLayout.NORTH);
        row.add(content, BorderLayout.CENTER);
        return row;
    }

    /** Draws the vertical accent bar used to mark the active navigation entry. */
    public static void paintActiveMarker(Graphics graphics, JComponent component, boolean active) {
        if (!active) return;
        Graphics2D g = (Graphics2D) graphics.create();
        g.setColor(Theme.ACCENT);
        g.fillRoundRect(0, (component.getHeight() - 16) / 2, 3, 16, 3, 3);
        g.dispose();
    }

    /** Adds a hover listener that repaints the component. */
    public static void trackHover(JComponent component, Runnable onEnter, Runnable onExit) {
        component.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent event) { onEnter.run(); }
            @Override public void mouseExited(MouseEvent event) { onExit.run(); }
        });
    }

    /** Formats a list of strings as a readable sentence. */
    public static String join(List<String> values) {
        return values == null || values.isEmpty() ? "—" : String.join(", ", values);
    }

    /** Returns the font metrics of the current UI font. */
    public static FontMetrics metrics(Component component) {
        return component.getFontMetrics(component.getFont());
    }
}
