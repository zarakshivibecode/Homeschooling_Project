package id.edu.homeschool.ui.design;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/** A compact, theme-aware progress track for summaries of persisted current-state counts. */
public final class ProgressTrack extends JComponent {

    private double fraction;
    private java.awt.Color fill = Theme.SUCCESS;

    public ProgressTrack() {
        setOpaque(false);
        setPreferredSize(new Dimension(240, 14));
        setMinimumSize(new Dimension(60, 14));
    }

    public void setProgress(long completed, long total, java.awt.Color color) {
        fraction = total <= 0 ? 0 : Math.max(0, Math.min(1, completed / (double) total));
        fill = color == null ? Theme.SUCCESS : color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        int radius = Math.max(1, height);
        g.setColor(Theme.ACCENT_SOFT);
        g.fillRoundRect(0, 0, width, height, radius, radius);
        int filled = (int) Math.round(width * fraction);
        if (filled > 0) {
            g.setColor(fill);
            g.fillRoundRect(0, 0, filled, height, radius, radius);
        }
        g.dispose();
    }
}
