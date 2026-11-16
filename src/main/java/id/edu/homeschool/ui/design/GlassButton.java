package id.edu.homeschool.ui.design;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * A button that paints its own glass surface. Swing's own button rendering cannot produce the
 * translucent layered look, so every button in the application is drawn by this class and the
 * look and feel never changes the appearance.
 */
public final class GlassButton extends JButton {

    private final Ui.Variant variant;
    private boolean hover;
    private boolean pressed;

    GlassButton(String text, Ui.Variant variant) {
        super(text);
        this.variant = variant;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setMargin(new java.awt.Insets(0, 0, 0, 0));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent event) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent event) { hover = false; pressed = false; repaint(); }
            @Override public void mousePressed(MouseEvent event) { pressed = true; repaint(); }
            @Override public void mouseReleased(MouseEvent event) { pressed = false; repaint(); }
        });
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Glass.prepare(g);
        int width = getWidth();
        int height = getHeight();
        int radius = Theme.RADIUS_SM;

        if (variant == Ui.Variant.PRIMARY) {
            if (!isEnabled()) {
                Glass.paintSurface(g, 0, 0, width, height, radius, new Color(226, 232, 240),
                        Theme.alpha(Theme.BORDER, 150), false);
            } else {
                // Soft glow drop shadow under primary button
                if (!pressed) {
                    g.setColor(new Color(67, 56, 202, hover ? 45 : 30));
                    g.fillRoundRect(1, 3, width - 2, height - 2, radius, radius);
                    g.setColor(new Color(59, 130, 246, hover ? 35 : 20));
                    g.fillRoundRect(2, 5, width - 4, height - 4, radius, radius);
                }

                // Restrained blue-indigo gradient for Liquid Glass
                Color top = pressed ? new Color(0x25, 0x63, 0xEB) : hover ? new Color(0x60, 0xA5, 0xFA) : Theme.ACCENT;
                Color bottom = pressed ? Theme.ACCENT_PRESSED : hover ? Theme.ACCENT : Theme.ACCENT_DEEP;
                java.awt.geom.RoundRectangle2D shape = new java.awt.geom.RoundRectangle2D.Float(0, 0, width, height, radius, radius);
                g.setPaint(new java.awt.GradientPaint(0, 0, top, 0, height, bottom));
                g.fill(shape);

                // Subtle outline
                g.setColor(Theme.alpha(Color.WHITE, hover ? 110 : 80));
                g.setStroke(new java.awt.BasicStroke(1f));
                g.draw(shape);

                // Top specular liquid reflection line
                g.setColor(Theme.alpha(Color.WHITE, hover ? 160 : 120));
                g.drawLine(radius / 2, 1, width - radius / 2, 1);
            }
        } else if (variant == Ui.Variant.SECONDARY) {
            Color body = !isEnabled() ? Theme.alpha(Color.WHITE, 120)
                    : pressed ? new Color(0xE0, 0xE7, 0xFF) : hover ? Theme.alpha(Theme.ACCENT_SOFT, 245) : Theme.alpha(Color.WHITE, 230);
            Color edge = !isEnabled() ? Theme.alpha(Theme.BORDER, 120)
                    : hover ? Theme.ACCENT_BORDER : Theme.alpha(Theme.BORDER, 220);
            Glass.paintSurface(g, 0, 0, width, height, radius, body, edge, hover && !pressed);
        } else if (variant == Ui.Variant.DANGER) {
            Color body = !isEnabled() ? Theme.alpha(Color.WHITE, 120)
                    : pressed ? new Color(0xFE, 0xCD, 0xCD) : hover ? new Color(0xFE, 0xE2, 0xE2) : Theme.DANGER_SOFT;
            Color edge = !isEnabled() ? Theme.alpha(Theme.BORDER, 120)
                    : hover ? new Color(0xF8, 0x71, 0x71) : Theme.DANGER_BORDER;
            Glass.paintSurface(g, 0, 0, width, height, radius, body, edge, false);
        } else {
            // Ghost and link variants stay flat so they do not compete with the primary action.
            if (hover || pressed) {
                Color body = variant == Ui.Variant.LINK ? Theme.alpha(Theme.ACCENT_SOFT, 220)
                        : Theme.alpha(Theme.ACCENT_SOFT, 160);
                g.setColor(body);
                g.fillRoundRect(0, 0, width, height, radius, radius);
            }
        }

        // A visible ring marks the control that owns the keyboard focus.
        if (isFocusable() && hasFocus()) {
            g.setColor(Theme.alpha(Theme.FOCUS, 190));
            g.setStroke(new java.awt.BasicStroke(2f));
            g.drawRoundRect(2, 2, width - 5, height - 5, radius - 2, radius - 2);
        }

        g.setFont(variant == Ui.Variant.PRIMARY ? Theme.FONT_13_BOLD : getFont());
        g.setColor(foregroundFor());
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(getText());
        int textY = (height + metrics.getAscent() - metrics.getDescent()) / 2;
        g.drawString(getText(), Math.max(0, (width - textWidth) / 2), textY);
        g.dispose();
    }

    private Color foregroundFor() {
        if (!isEnabled()) return Theme.TEXT_SUBTLE;
        return switch (variant) {
            case PRIMARY -> Theme.TEXT_ON_ACCENT;
            case SECONDARY -> Theme.TEXT;
            case GHOST -> Theme.TEXT_MUTED;
            case LINK -> Theme.ACCENT;
            case DANGER -> Theme.DANGER;
        };
    }

    /** Sizes the button to its label so the layout stays predictable and comfortable. */
    void applyPreferredSize() {
        FontMetrics metrics = getFontMetrics(variant == Ui.Variant.PRIMARY ? Theme.FONT_13_BOLD : getFont());
        int width = metrics.stringWidth(getText()) + Theme.SPACE_5 * 2;
        setPreferredSize(new Dimension(Math.max(width, 76), Theme.CONTROL_HEIGHT));
    }
}
