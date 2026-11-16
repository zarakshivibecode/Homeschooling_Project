package id.edu.homeschool.ui.design;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A summary tile that shows a caption, a value and a hint with a clear visual hierarchy.
 *
 * <p>The tile is painted as a frosted surface. The caption sits next to a small tinted icon
 * chip, the value is the largest element on the card and the hint below it stays quiet, so the
 * eye reads the number first and the supporting text second.</p>
 */
public final class StatCard extends GlassPanel {

    private final JLabel value = new JLabel("—");
    private final JLabel hint = Ui.subtle("");
    private final IconChip chip;
    private final JLabel title;

    private boolean hover;

    public StatCard(String caption, String initialValue) {
        this(caption, initialValue, null, Theme.ACCENT);
    }

    /**
     * @param caption the name of the metric
     * @param initialValue the value shown before the data is loaded
     * @param icon the symbol of the metric; null keeps the card without an icon
     * @param tint the accent colour of the icon chip
     */
    public StatCard(String caption, String initialValue, StatIcon icon, Color tint) {
        super(new BorderLayout(0, Theme.SPACE_3));
        setFill(Theme.GLASS);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_5, Theme.SPACE_5, Theme.SPACE_5, Theme.SPACE_5));

        title = Ui.muted(caption);
        title.setFont(Theme.FONT_12_BOLD);
        title.setForeground(Theme.TEXT_MUTED);
        chip = new IconChip(icon, tint);

        JPanel head = new JPanel(new BorderLayout(Theme.SPACE_3, 0));
        head.setBackground(Theme.CANVAS);
        head.setOpaque(false);
        if (icon != null) {
            head.add(chip, BorderLayout.WEST);
        }
        head.add(title, BorderLayout.CENTER);

        value.setFont(Theme.FONT_28);
        value.setForeground(Theme.TEXT);

        hint.setFont(Theme.FONT_11);
        hint.setForeground(Theme.TEXT_SUBTLE);
        hint.setVisible(false);

        add(head, BorderLayout.NORTH);
        add(value, BorderLayout.CENTER);
        add(hint, BorderLayout.SOUTH);
        setValue(initialValue);

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                hover = true;
                setFill(Theme.GLASS_CARD);
                repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                hover = false;
                setFill(Theme.GLASS);
                repaint();
            }
        });
    }

    public void setValue(String text) {
        value.setText(text == null || text.isBlank() ? "—" : text);
    }

    public void setHint(String text) {
        hint.setText(text == null ? "" : text);
        hint.setVisible(!hint.getText().isEmpty());
    }

    /** The small tinted square that holds the symbol of the metric. */
    private static final class IconChip extends JPanel {

        private final StatIcon icon;
        private final Color tint;

        IconChip(StatIcon icon, Color tint) {
            this.icon = icon;
            this.tint = tint == null ? Theme.ACCENT : tint;
            setPreferredSize(new Dimension(34, 34));
            setMinimumSize(new Dimension(34, 34));
            setMaximumSize(new Dimension(34, 34));
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Glass.prepare(g);
            int size = Math.min(getWidth(), getHeight());
            int radius = Theme.RADIUS_SM;

            // Translucent tinted body with soft gradient
            g.setPaint(new java.awt.GradientPaint(0, 0, Theme.alpha(tint, 32), 0, size, Theme.alpha(tint, 14)));
            g.fillRoundRect(0, 0, size, size, radius, radius);

            // Subtle border
            g.setColor(Theme.alpha(tint, 65));
            g.setStroke(new java.awt.BasicStroke(1f));
            g.drawRoundRect(0, 0, size - 1, size - 1, radius, radius);

            // Specular top highlight
            g.setColor(Theme.alpha(Color.WHITE, 140));
            g.drawLine(radius / 2, 1, size - radius / 2, 1);

            if (icon != null) icon.paint(g, (size - 16) / 2, (size - 16) / 2, 16, tint);
            g.dispose();
        }
    }
}
