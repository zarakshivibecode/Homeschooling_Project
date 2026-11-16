package id.edu.homeschool.ui.design;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 * A dropdown painted as a frosted glass surface that matches the text fields.
 *
 * <p>The component keeps the standard {@link JComboBox} behaviour, including the popup list and
 * every item listener, but renders itself with the shared material: a translucent white body, a
 * hairline edge, an indigo ring while it has the focus and a slim arrow in the accent colour.
 * The look and feel is pinned to the basic UI so the appearance never changes with the host
 * platform.</p>
 */
public class ModernComboBox<T> extends JComboBox<T> {

    private boolean focused;
    private boolean applying;

    public ModernComboBox() {
        applyLook();
        setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                setFont(Theme.FONT_13);
                setForeground(selected ? Theme.ACCENT_DEEP : Theme.TEXT);
                setBackground(selected ? Theme.alpha(Theme.ACCENT, 34) : Theme.SURFACE);
                setOpaque(selected);
                setBorder(BorderFactory.createEmptyBorder(6, Theme.SPACE_3, 6, Theme.SPACE_3));
                return this;
            }
        });
        addFocusListener(new java.awt.event.FocusListener() {
            @Override public void focusGained(java.awt.event.FocusEvent event) { focused = true; repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent event) { focused = false; repaint(); }
        });
    }

    /** Pins the look and feel and makes the control transparent so the glass body shows. */
    private void applyLook() {
        if (applying) return;
        applying = true;
        try {
            setUI(new GlassComboUI());
            setFont(Theme.FONT_13);
            setForeground(Theme.TEXT);
            setOpaque(false);
            // The combo box forces the opaque flag through a client property, so it is overridden here.
            putClientProperty("opaque", Boolean.FALSE);
            setFocusable(false);
            setBorder(BorderFactory.createEmptyBorder(0, Theme.SPACE_2, 0, 0));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.CONTROL_HEIGHT));
        } finally {
            applying = false;
        }
    }

    /** Keeps the glass look when the look and feel of the host changes. */
    @Override public void updateUI() {
        applyLook();
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Glass.prepare(g);
        Glass.paintField(g, 0, 0, getWidth(), getHeight(), Theme.RADIUS_SM, focused);
        g.dispose();

        super.paintComponent(graphics);
    }

    /**
     * The basic look and feel with a transparent arrow button. The arrow keeps its own click
     * behaviour, so the popup still opens from the arrow.
     */
    private static final class GlassComboUI extends BasicComboBoxUI {

        @Override protected void installDefaults() {
            super.installDefaults();
            // A slim arrow keeps the dropdown in proportion with the rounded text fields.
            squareButton = false;
        }

        @Override protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override protected void paintComponent(Graphics graphics) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    Glass.prepare(g);
                    int w = getWidth();
                    int h = getHeight();
                    int cx = w / 2;
                    int cy = h / 2 - 1;
                    g.setColor(Theme.TEXT_MUTED);
                    g.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    g.drawLine(cx - 4, cy - 2, cx, cy + 2);
                    g.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    g.dispose();
                }
            };
            button.setName("ComboBox.arrowButton");
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            return button;
        }

        @Override public void installUI(JComponent component) {
            super.installUI(component);
            if (arrowButton != null) {
                arrowButton.setBorder(BorderFactory.createEmptyBorder());
                arrowButton.setOpaque(false);
                arrowButton.setContentAreaFilled(false);
                arrowButton.setFocusPainted(false);
            }
        }

        @Override public void configureArrowButton() {
            super.configureArrowButton();
            if (arrowButton != null) {
                arrowButton.setBorder(BorderFactory.createEmptyBorder());
                arrowButton.setOpaque(false);
                arrowButton.setContentAreaFilled(false);
                arrowButton.setFocusPainted(false);
            }
        }
    }
}
