package id.edu.homeschool.ui.design;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 * A rounded status pill with a small colour dot.
 *
 * <p>A plain coloured label reads as a rectangle on the frosted surfaces of the application.
 * This badge paints an actual pill, keeps the text fully opaque and adds a dot that carries the
 * same semantic colour, so the status stays readable even when the tint is very light.</p>
 */
public class StatusBadge extends JComponent {

    private String text;
    private Theme.StatusStyle style;
    private boolean dot = true;

    public StatusBadge(String text, Theme.StatusStyle style) {
        this.text = text == null ? "" : text;
        this.style = style == null ? Theme.neutral() : style;
        setFont(Theme.FONT_11_BOLD);
        setOpaque(false);
        setPreferredSize(measure());
    }

    /** Updates the text and the colours of the badge. */
    public StatusBadge set(String text, Theme.StatusStyle style) {
        this.text = text == null ? "" : text;
        this.style = style == null ? Theme.neutral() : style;
        setPreferredSize(measure());
        repaint();
        return this;
    }

    /** Hides the colour dot, used for compact tags such as the demo marker. */
    public StatusBadge withoutDot() {
        dot = false;
        setPreferredSize(measure());
        repaint();
        return this;
    }

    public String text() { return text; }

    @Override public void setFont(Font font) {
        super.setFont(font);
        if (text != null && font != null) setPreferredSize(measure());
    }

    private Dimension measure() {
        FontMetrics metrics = getFontMetrics(getFont());
        int width = metrics.stringWidth(text) + Theme.SPACE_4 + (dot ? Theme.SPACE_3 : 0);
        return new Dimension(Math.max(width, 24), 24);
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Glass.prepare(g);
        int width = getWidth();
        int height = getHeight();

        // Translucent tinted background
        g.setColor(style.background());
        g.fillRoundRect(0, 0, width, height, height, height);

        // Thin border
        g.setColor(style.border());
        g.setStroke(new java.awt.BasicStroke(1f));
        g.drawRoundRect(0, 0, width - 1, height - 1, height, height);

        // Specular highlight line along upper curve
        g.setColor(Theme.alpha(java.awt.Color.WHITE, 150));
        g.drawLine(height / 2, 1, Math.max(height / 2, width - height / 2), 1);

        int left = Theme.SPACE_2 + 2;
        if (dot) {
            // Glowing dual-layer dot
            g.setColor(Theme.alpha(style.foreground(), 45));
            g.fillOval(left - 1, (height - 8) / 2, 8, 8);
            g.setColor(style.foreground());
            g.fillOval(left, (height - 6) / 2, 6, 6);
            left += Theme.SPACE_3 + 1;
        }

        g.setFont(getFont());
        g.setColor(style.foreground());
        FontMetrics metrics = g.getFontMetrics();
        int textY = (height + metrics.getAscent() - metrics.getDescent()) / 2;
        int available = width - left - Theme.SPACE_2;
        String shown = fit(text, metrics, available);
        int textWidth = metrics.stringWidth(shown);
        int textX = left + Math.max(0, (width - left - textWidth) / 2);
        g.drawString(shown, textX, textY);
        g.dispose();
    }

    /** Shortens the text with an ellipsis when the badge is too narrow for it. */
    private String fit(String value, FontMetrics metrics, int available) {
        if (available <= 0) return "";
        if (metrics.stringWidth(value) <= available) return value;
        StringBuilder shortened = new StringBuilder(value);
        while (shortened.length() > 1 && metrics.stringWidth(shortened + "…") > available) {
            shortened.setLength(shortened.length() - 1);
        }
        return shortened + "…";
    }
}
