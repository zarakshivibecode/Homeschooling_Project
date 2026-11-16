package id.edu.homeschool.ui.design;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Central painting helpers for the frosted-glass look. Every translucent surface in the
 * application is drawn through this class so the highlight, hairline edge and shadow stay
 * consistent instead of being re-implemented on every screen.
 *
 * <p>The frost itself is a convincing approximation rather than a real blur: Swing has no
 * dependable backdrop filter, and a per-frame blur would repaint the whole window on every
 * mouse move. Instead a translucent white body, a soft vertical sheen, a luminous top edge and
 * a diffused shadow reproduce the depth of frosted glass at a fraction of the cost.</p>
 */
public final class Glass {

    private Glass() { }

    /** Applies the anti-aliasing hints used by every glass painter. */
    public static void prepare(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /**
     * Paints a floating frosted surface: diffused multi-layer shadow, translucent body,
     * luminous hairline edge and a specular highlight along the top of the shape.
     */
    public static void paintSurface(Graphics2D graphics, int x, int y, int width, int height, int radius,
            Color fill, Color outline, boolean shadow) {
        if (width <= 0 || height <= 0) return;
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, width, height, radius, radius);

        if (shadow) paintShadow(graphics, shape, x, y, width, height, radius);

        // Liquid glass refraction gradient: subtly lighter and luminous at the top
        Color top = Theme.lighten(fill, 0.22f);
        Color bottom = Theme.darken(fill, 0.02f);
        graphics.setPaint(new GradientPaint(x, y, top, x, y + height, bottom));
        graphics.fill(shape);

        if (outline != null) {
            graphics.setColor(outline);
            graphics.setStroke(new BasicStroke(1f));
            graphics.draw(shape);
        }
        paintHighlight(graphics, x, y, width, height, radius);
    }

    /** Paints a soft, multi-tier ambient shadow so the glass surface appears to float with depth. */
    private static void paintShadow(Graphics2D graphics, RoundRectangle2D shape, int x, int y,
            int width, int height, int radius) {
        // Micro contact shadow
        graphics.setColor(Theme.alpha(Theme.SHADOW, 10));
        graphics.fill(new RoundRectangle2D.Float(x, y + 1, width, height, radius, radius));
        // Soft ambient layer 1
        graphics.setColor(Theme.alpha(Theme.SHADOW, 16));
        graphics.fill(new RoundRectangle2D.Float(x, y + 3, width, height, radius, radius));
        // Floating elevation layer 2
        graphics.setColor(Theme.alpha(Theme.SHADOW, 18));
        graphics.fill(new RoundRectangle2D.Float(x, y + 6, width, height, radius, radius));
        // Diffused feather layer 3
        graphics.setColor(Theme.alpha(Theme.SHADOW, 12));
        graphics.fill(new RoundRectangle2D.Float(x, y + 10, width, height, radius, radius));
    }

    /** Draws the luminous specular reflection along the upper rim of the glass. */
    private static void paintHighlight(Graphics2D graphics, int x, int y, int width, int height, int radius) {
        if (height < 6) return;
        // Inner luminous rim
        graphics.setColor(Theme.alpha(Color.WHITE, 160));
        graphics.setStroke(new BasicStroke(1f));
        int inset = Math.max(3, radius / 2);
        graphics.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, width - 1f, height - 1f,
                radius, radius));

        // Luminous top reflection line with gradient falloff
        GradientPaint sheen = new GradientPaint(
                x + inset, y + 1, Theme.alpha(Color.WHITE, 220),
                x + width - inset, y + 1, Theme.alpha(Color.WHITE, 90));
        graphics.setPaint(sheen);
        graphics.drawLine(x + inset, y + 1, x + width - inset, y + 1);

        // Soft secondary highlight just beneath the top edge
        if (height > 16) {
            graphics.setColor(Theme.alpha(Color.WHITE, 45));
            graphics.drawLine(x + inset + 2, y + 2, x + width - inset - 2, y + 2);
        }
    }

    /** Paints the background gradient with rich blue-lavender ambient lighting and subtle refraction waves. */
    public static void paintCanvas(Graphics2D graphics, int width, int height) {
        if (width <= 0 || height <= 0) return;
        graphics.setPaint(new GradientPaint(0, 0, Theme.CANVAS_TOP, 0, height, Theme.CANVAS_BOTTOM));
        graphics.fillRect(0, 0, width, height);

        // Ambient blue-lavender lighting glows
        float maxDim = Math.max(width, height);
        paintBloom(graphics, width * 0.08f, height * 0.04f, maxDim * 0.44f, Theme.ICE);
        paintBloom(graphics, width * 0.92f, height * 0.10f, maxDim * 0.42f, Theme.LAVENDER);
        paintBloom(graphics, width * 0.48f, height * 0.96f, maxDim * 0.38f, Theme.ICE);
        paintBloom(graphics, width * 0.85f, height * 0.78f, maxDim * 0.30f, Theme.LAVENDER);

        // Subtle, elegant refraction contour in background for layered liquid depth
        paintRefractionCurve(graphics, width, height);
    }

    /** Paints a very subtle ambient liquid wave that adds glass depth without distracting. */
    private static void paintRefractionCurve(Graphics2D graphics, int width, int height) {
        if (width < 200 || height < 200) return;
        java.awt.geom.Path2D wave = new java.awt.geom.Path2D.Float();
        wave.moveTo(-50, height * 0.25f);
        wave.curveTo(width * 0.35f, height * 0.12f, width * 0.65f, height * 0.42f, width + 50, height * 0.28f);
        graphics.setPaint(new GradientPaint(0, 0, new Color(59, 130, 246, 14),
                width, height * 0.35f, new Color(167, 139, 250, 14)));
        graphics.setStroke(new BasicStroke(48f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.draw(wave);
    }

    /** Paints one soft radial bloom used to give the canvas depth. */
    private static void paintBloom(Graphics2D graphics, float centerX, float centerY, float radius, Color color) {
        if (radius <= 1) return;
        java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(centerX, centerY);
        graphics.setPaint(new java.awt.RadialGradientPaint(center, radius, new float[]{0f, 1f},
                new Color[]{Theme.alpha(color, color.getAlpha()), Theme.alpha(color, 0)}));
        graphics.fill(new java.awt.geom.Ellipse2D.Float(centerX - radius, centerY - radius, radius * 2, radius * 2));
    }

    /** Paints a translucent header band that fades into the canvas, used by the top bar. */
    public static void paintHeaderBand(Graphics2D graphics, int width, int height) {
        graphics.setPaint(new GradientPaint(0, 0, Theme.alpha(Color.WHITE, 225), 0, height,
                Theme.alpha(Color.WHITE, 140)));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Theme.alpha(Theme.GLASS_OUTLINE, 180));
        graphics.fillRect(0, height - 1, width, 1);
    }

    /**
     * Paints the body of an input field. The focused state is marked by a ring around the
     * surface instead of a colour fill, so the text always keeps the same contrast.
     */
    public static void paintField(Graphics2D graphics, int x, int y, int width, int height, int radius,
            boolean focused) {
        if (width <= 0 || height <= 0) return;
        graphics.setColor(focused ? Theme.alpha(Color.WHITE, 252) : Theme.alpha(Color.WHITE, 242));
        graphics.fillRoundRect(x, y, width, height, radius, radius);

        if (focused) {
            // A soft glowing blue halo outside the surface followed by a crisp ring
            graphics.setColor(Theme.alpha(Theme.FOCUS, 42));
            graphics.setStroke(new BasicStroke(3.2f));
            graphics.drawRoundRect(x - 1, y - 1, width + 2, height + 2, radius + 2, radius + 2);
            graphics.setColor(Theme.alpha(Theme.FOCUS, 220));
            graphics.setStroke(new BasicStroke(1.5f));
            graphics.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        } else {
            graphics.setColor(Theme.alpha(Theme.BORDER, 220));
            graphics.setStroke(new BasicStroke(1f));
            graphics.drawRoundRect(x, y, width - 1, height - 1, radius, radius);

            // Subtle top highlight for glass feel
            graphics.setColor(Theme.alpha(Color.WHITE, 180));
            graphics.drawLine(x + radius / 2, y + 1, x + width - radius / 2, y + 1);
        }
    }
}
