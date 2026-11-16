package id.edu.homeschool.ui.design;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * A panel that paints itself as a frosted-glass surface. The body is translucent so the canvas
 * gradient shows through, while a hairline edge and a soft shadow keep the panel readable.
 */
public class GlassPanel extends javax.swing.JPanel {

    private Color fill = Theme.GLASS;
    private Color outline = Theme.GLASS_OUTLINE;
    private boolean shadow = true;
    private int radius = Theme.RADIUS;

    public GlassPanel() {
        this(new java.awt.BorderLayout());
    }

    public GlassPanel(java.awt.LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    public GlassPanel setFill(Color value) {
        this.fill = value == null ? Theme.GLASS : value;
        repaint();
        return this;
    }

    public GlassPanel setOutline(Color value) {
        this.outline = value;
        repaint();
        return this;
    }

    public GlassPanel setShadow(boolean value) {
        this.shadow = value;
        repaint();
        return this;
    }

    public GlassPanel setRadius(int value) {
        this.radius = value;
        repaint();
        return this;
    }

    public Color fill() { return fill; }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Glass.prepare(g);
        Glass.paintSurface(g, 0, 0, getWidth(), getHeight(), radius, fill, outline, shadow);
        g.dispose();
        super.paintComponent(graphics);
    }
}
