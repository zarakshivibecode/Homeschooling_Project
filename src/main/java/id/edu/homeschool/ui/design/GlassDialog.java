package id.edu.homeschool.ui.design;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Floating glass windows used for forms, details and messages. The native window decoration is
 * replaced by a custom glass header so the dialogs share the same material as the rest of the
 * application; the window can still be dragged by its header.
 */
public final class GlassDialog {

    /** The kind of feedback a message dialog shows. */
    public enum Kind { INFO, SUCCESS, WARNING, ERROR }

    private GlassDialog() { }

    /**
     * Builds an undecorated glass dialog. The caller adds the body and the footer and finally
     * makes the window visible.
     */
    public static JDialog create(Window owner, String title, String subtitle, JComponent body, JComponent footer) {
        JDialog dialog = new JDialog(owner, title, JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        GlassPanel root = new GlassPanel(new BorderLayout(0, Theme.SPACE_4));
        root.setFill(Theme.GLASS_SOLID);
        root.setRadius(Theme.RADIUS_LG);
        root.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_5, Theme.SPACE_6, Theme.SPACE_5, Theme.SPACE_6));

        root.add(header(title, subtitle, dialog), BorderLayout.NORTH);
        if (body != null) root.add(body, BorderLayout.CENTER);
        if (footer != null) root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setMinimumSize(new Dimension(460, 360));
        dialog.pack();
        dialog.setSize(Math.max(500, dialog.getWidth()), Math.max(380, dialog.getHeight()));
        dialog.setLocationRelativeTo(owner);
        return dialog;
    }

    /** Wraps a component in a transparent scroll area so long content stays reachable. */
    public static JScrollPane scrollBody(JComponent content) {
        TopScrollPane scroll = new TopScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(Theme.CANVAS);
        scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getHorizontalScrollBar().setOpaque(false);
        return scroll;
    }

    /**
     * A scroll area that always opens at the first line. Without this the viewport sometimes
     * starts in the middle of the content because the caret of a text area pulls it down.
     */
    private static final class TopScrollPane extends JScrollPane {
        private boolean positioned;

        TopScrollPane(JComponent view) {
            super(view);
        }

        @Override public void doLayout() {
            super.doLayout();
            if (positioned) return;
            javax.swing.JViewport viewport = getViewport();
            if (viewport == null) return;
            positioned = true;
            viewport.setViewPosition(new java.awt.Point(0, 0));
        }
    }

    private static JComponent header(String title, String subtitle, JDialog dialog) {
        JPanel header = new JPanel(new BorderLayout(Theme.SPACE_4, 2));
        header.setBackground(Theme.CANVAS);
        header.setOpaque(false);

        JPanel text = new JPanel(new BorderLayout(0, 3));
        text.setBackground(Theme.CANVAS);
        text.setOpaque(false);
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.FONT_16_BOLD);
        heading.setForeground(Theme.TEXT);
        text.add(heading, BorderLayout.NORTH);
        if (subtitle != null && !subtitle.isBlank()) {
            JLabel caption = Ui.muted(subtitle);
            caption.setFont(Theme.FONT_12);
            text.add(caption, BorderLayout.CENTER);
        }

        JButton close = new JButton("×") {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                Glass.prepare(g);
                g.setColor(Theme.alpha(Theme.ACCENT, 26));
                g.fillOval(0, 0, getWidth(), getHeight());
                g.setFont(Theme.FONT_13);
                g.setColor(Theme.TEXT_MUTED);
                int textWidth = g.getFontMetrics().stringWidth(getText());
                g.drawString(getText(), (getWidth() - textWidth) / 2, (getHeight() + 5) / 2);
                g.dispose();
            }
        };
        close.setFont(Theme.FONT_13);
        close.setContentAreaFilled(false);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        close.setOpaque(false);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.setPreferredSize(new Dimension(28, 28));
        close.setToolTipText("Tutup");
        close.addActionListener(event -> dialog.dispose());

        header.add(text, BorderLayout.CENTER);
        header.add(close, BorderLayout.EAST);
        makeDraggable(header, dialog);
        return header;
    }

    /** Lets the user move the undecorated window by dragging its header. */
    private static void makeDraggable(JComponent component, JDialog dialog) {
        final java.awt.Point[] offset = { null };
        component.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent event) { offset[0] = event.getPoint(); }
            @Override public void mouseReleased(MouseEvent event) { offset[0] = null; }
        });
        component.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent event) {
                if (offset[0] == null) return;
                java.awt.Point screen = event.getLocationOnScreen();
                dialog.setLocation(screen.x - offset[0].x, screen.y - offset[0].y);
            }
        });
    }

    /* ---------- message dialogs ---------- */

    /** Shows a glass message dialog with the given feedback style. */
    public static void message(Component parent, String title, String message, Kind kind) {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.err.println("[" + title + "] " + (message == null ? "" : message));
            return;
        }
        JTextArea body = new JTextArea(message == null ? "" : message);
        body.setFont(Theme.FONT_13);
        body.setForeground(Theme.TEXT);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        body.setColumns(38);

        JPanel content = new JPanel(new BorderLayout(Theme.SPACE_3, 0));
        content.setBackground(Theme.CANVAS);
        content.setOpaque(false);
        content.add(accentLine(kind), BorderLayout.WEST);
        content.add(body, BorderLayout.CENTER);

        JButton ok = Ui.button("Mengerti", kind == Kind.ERROR ? Ui.Variant.SECONDARY : Ui.Variant.PRIMARY);
        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, Theme.SPACE_2, 0));
        footer.setBackground(Theme.CANVAS);
        footer.setOpaque(false);
        footer.add(ok);

        JDialog dialog = create(Dialogs.owner(parent), title, null, content, footer);
        ok.addActionListener(event -> dialog.dispose());
        dialog.setVisible(true);
    }

    private static JComponent accentLine(Kind kind) {
        GlassPanel line = new GlassPanel();
        line.setFill(switch (kind) {
            case SUCCESS -> Theme.SUCCESS;
            case WARNING -> Theme.WARNING;
            case ERROR -> Theme.DANGER;
            default -> Theme.ACCENT;
        });
        line.setShadow(false);
        line.setRadius(4);
        line.setPreferredSize(new Dimension(4, 44));
        return line;
    }

    /** Shows a confirmation dialog and returns the user's answer. */
    public static boolean confirm(Component parent, String title, String message) {
        if (java.awt.GraphicsEnvironment.isHeadless()) return false;
        JTextArea body = new JTextArea(message == null ? "" : message);
        body.setFont(Theme.FONT_13);
        body.setForeground(Theme.TEXT);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setOpaque(false);
        body.setColumns(38);

        JPanel content = new JPanel(new BorderLayout(Theme.SPACE_3, 0));
        content.setBackground(Theme.CANVAS);
        content.setOpaque(false);
        content.add(accentLine(Kind.WARNING), BorderLayout.WEST);
        content.add(body, BorderLayout.CENTER);

        JButton yes = Ui.button("Ya, lanjutkan", Ui.Variant.PRIMARY);
        JButton no = Ui.button("Batal", Ui.Variant.SECONDARY);
        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, Theme.SPACE_2, 0));
        footer.setBackground(Theme.CANVAS);
        footer.setOpaque(false);
        footer.add(no);
        footer.add(yes);

        JDialog dialog = create(Dialogs.owner(parent), title, null, content, footer);
        final boolean[] answer = { false };
        yes.addActionListener(event -> { answer[0] = true; dialog.dispose(); });
        no.addActionListener(event -> { answer[0] = false; dialog.dispose(); });
        dialog.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) { answer[0] = false; }
        });
        dialog.setVisible(true);
        return answer[0];
    }

    /** Shows a long, selectable block of text such as a generated invitation code. */
    public static void code(Component parent, String title, String message, String code) {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.err.println("[" + title + "] " + message + System.lineSeparator() + code);
            return;
        }
        JTextArea area = new JTextArea(code);
        area.setFont(Theme.FONT_16_BOLD);
        area.setEditable(false);
        area.setLineWrap(false);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_2, Theme.SPACE_2, Theme.SPACE_2, Theme.SPACE_2));

        GlassPanel panel = new GlassPanel(new BorderLayout());
        panel.setFill(Theme.alpha(Theme.ACCENT_SOFT, 220));
        panel.setShadow(false);
        panel.setRadius(Theme.RADIUS_SM);
        panel.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_2, Theme.SPACE_2, Theme.SPACE_2, Theme.SPACE_2));
        panel.add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, Theme.SPACE_3));
        content.setBackground(Theme.CANVAS);
        content.setOpaque(false);
        JLabel caption = Ui.muted(message);
        caption.setFont(Theme.FONT_12);
        content.add(caption, BorderLayout.NORTH);
        content.add(panel, BorderLayout.CENTER);

        JButton ok = Ui.button("Mengerti", Ui.Variant.PRIMARY);
        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, Theme.SPACE_2, 0));
        footer.setBackground(Theme.CANVAS);
        footer.setOpaque(false);
        footer.add(ok);

        JDialog dialog = create(Dialogs.owner(parent), title, null, content, footer);
        ok.addActionListener(event -> dialog.dispose());
        dialog.setVisible(true);
    }

    /** Brings a dialog to the front after it was shown. */
    public static void focus(Window window) {
        if (window == null) return;
        SwingUtilities.invokeLater(() -> window.toFront());
    }
}
