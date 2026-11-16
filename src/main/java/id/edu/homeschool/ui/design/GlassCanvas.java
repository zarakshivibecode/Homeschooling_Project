package id.edu.homeschool.ui.design;

import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 * The application background: a soft ice-blue to lavender gradient with two gentle blooms.
 * It is painted once per repaint and never animates, so it costs nothing at runtime.
 */
public final class GlassCanvas extends JPanel {

    public GlassCanvas() {
        setOpaque(true);
        setBackground(Theme.CANVAS_TOP);
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Glass.prepare(g);
        Glass.paintCanvas(g, getWidth(), getHeight());
        g.dispose();
    }
}
