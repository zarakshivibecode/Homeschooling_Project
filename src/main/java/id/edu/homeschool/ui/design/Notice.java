package id.edu.homeschool.ui.design;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * An inline message shown next to a form or under a header.
 *
 * <p>The notice is a small rounded panel with a coloured dot and a tinted body. It replaces the
 * plain label that used to carry the validation message, so a problem is recognisable at a
 * glance and stays readable on the frosted surfaces.</p>
 */
public final class Notice extends JPanel {

    private final JLabel message = new JLabel(" ");
    private Theme.StatusStyle style = Theme.neutral();
    private boolean visible;

    public Notice() {
        super(new BorderLayout(Theme.SPACE_2, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_2, Theme.SPACE_3, Theme.SPACE_2, Theme.SPACE_3));
        message.setFont(Theme.FONT_12);
        message.setForeground(Theme.TEXT);
        add(message, BorderLayout.CENTER);
        setVisible(false);
    }

    /** Shows a message with the given tone. */
    public void show(String text, Theme.StatusStyle style) {
        this.style = style == null ? Theme.neutral() : style;
        message.setText(text == null ? "" : text);
        message.setForeground(this.style.foreground());
        this.visible = true;
        setVisible(true);
        revalidate();
        repaint();
    }

    /** Hides the notice. */
    public void clear() {
        message.setText(" ");
        visible = false;
        setVisible(false);
        revalidate();
        repaint();
    }

    public boolean hasMessage() { return visible; }

    @Override protected void paintComponent(Graphics graphics) {
        if (!visible) return;
        Graphics2D g = (Graphics2D) graphics.create();
        Glass.prepare(g);
        int width = getWidth();
        int height = getHeight();
        g.setColor(style.background());
        g.fillRoundRect(0, 0, width, height, Theme.RADIUS_SM, Theme.RADIUS_SM);
        g.setColor(Theme.alpha(style.foreground(), 60));
        g.drawRoundRect(0, 0, width - 1, height - 1, Theme.RADIUS_SM, Theme.RADIUS_SM);
        g.setColor(style.foreground());
        g.fillOval(Theme.SPACE_3, (height - 7) / 2, 7, 7);
        g.dispose();
        super.paintComponent(graphics);
    }

    @Override public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width, Math.max(size.height, 32));
    }
}
