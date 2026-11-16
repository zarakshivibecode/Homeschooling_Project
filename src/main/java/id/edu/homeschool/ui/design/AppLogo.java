package id.edu.homeschool.ui.design;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 * The application logo: a glossy three-dimensional {@code H} mark that fades from light blue on
 * the left to violet on the right, wrapped by a ribbon that connects the two bars, plus the
 * "HomeLearn" wordmark.
 *
 * <p>The mark is drawn with plain Java2D primitives, so the application keeps shipping without
 * binary image assets and the logo stays crisp on every screen density. When a PNG is placed on
 * the classpath at {@code /logo.png} that file is drawn instead, which lets the official artwork
 * be dropped in without touching a single line of code.</p>
 */
public final class AppLogo extends JComponent {

    /** Classpath location of the optional PNG that overrides the drawn mark. */
    public static final String IMAGE_RESOURCE = "/logo.png";

    /* Brand colours of the drawn mark: Liquid Glass blue-lavender palette. */
    private static final Color BAR_LEFT_TOP = new Color(0x38, 0xBD, 0xF8);
    private static final Color BAR_LEFT_BOTTOM = new Color(0x3B, 0x82, 0xF6);
    private static final Color BAR_RIGHT_TOP = new Color(0xC0, 0x84, 0xFC);
    private static final Color BAR_RIGHT_BOTTOM = new Color(0x43, 0x38, 0xCA);
    private static final Color RIBBON_LEFT = new Color(0x93, 0xC5, 0xFD);
    private static final Color RIBBON_RIGHT = new Color(0xA7, 0x8B, 0xFA);
    private static final Color WORD_LEFT = new Color(0x17, 0x25, 0x54);
    private static final Color WORD_RIGHT = new Color(0x3B, 0x82, 0xF6);

    /** The wordmark shown next to the mark. */
    public static final String WORDMARK = "HomeLearn";

    private final BufferedImage image;
    private final boolean withWordmark;

    private AppLogo(BufferedImage image, boolean withWordmark) {
        this.image = image;
        this.withWordmark = withWordmark;
        setOpaque(false);
    }

    /**
     * The square logo mark, used where only a symbol fits.
     *
     * @param size the edge length in pixels
     */
    public static AppLogo mark(int size) {
        AppLogo logo = new AppLogo(loadImage(), false);
        logo.setPreferredSize(new Dimension(size, size));
        logo.setMinimumSize(new Dimension(size, size));
        logo.setMaximumSize(new Dimension(size, size));
        return logo;
    }

    /**
     * The mark with the wordmark underneath, used on the authentication screen.
     *
     * @param markSize the edge length of the mark in pixels
     * @param fontSize the point size of the wordmark
     */
    public static AppLogo lockup(int markSize, int fontSize) {
        AppLogo logo = new AppLogo(loadImage(), true);
        logo.wordmarkFont = new Font(Theme.FONT_14.getFamily(), Font.BOLD, fontSize);
        int width = Math.max(markSize, 170);
        int height = markSize + (fontSize + 10);
        logo.setPreferredSize(new Dimension(width, height));
        logo.setMinimumSize(new Dimension(width, height));
        logo.setMaximumSize(new Dimension(width, height));
        return logo;
    }

    private Font wordmarkFont = new Font(Theme.FONT_14.getFamily(), Font.BOLD, 26);

    /** Reads the optional PNG from the classpath; returns null when it is not present. */
    private static BufferedImage loadImage() {
        try (InputStream stream = AppLogo.class.getResourceAsStream(IMAGE_RESOURCE)) {
            if (stream == null) return null;
            return ImageIO.read(stream);
        } catch (IOException exception) {
            // A missing or broken artwork file must never stop the application from starting.
            return null;
        }
    }

    @Override protected void paintComponent(java.awt.Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int markSize = withWordmark ? Math.min(width, height - wordmarkFont.getSize() - 12) : Math.min(width, height);

        if (image != null) {
            paintImage(g, image, 0, 0, width, markSize);
        } else {
            paintMark(g, (width - markSize) / 2, 0, markSize);
        }

        if (withWordmark) {
            paintWordmark(g, 0, markSize + 4, width);
        }
        g.dispose();
    }

    /** Draws the PNG artwork, scaled to fit while keeping its aspect ratio. */
    private void paintImage(Graphics2D g, BufferedImage artwork, int x, int y, int width, int height) {
        double ratio = Math.min(width / (double) artwork.getWidth(), height / (double) artwork.getHeight());
        int drawWidth = (int) Math.round(artwork.getWidth() * ratio);
        int drawHeight = (int) Math.round(artwork.getHeight() * ratio);
        int drawX = x + (width - drawWidth) / 2;
        int drawY = y + (height - drawHeight) / 2;
        g.drawImage(artwork, drawX, drawY, drawWidth, drawHeight, null);
    }

    /**
     * Draws the glossy three-dimensional mark: two rounded bars, a ribbon that sweeps between
     * them, and the specular highlights that give the shape its depth.
     */
    private void paintMark(Graphics2D g, int x, int y, int size) {
        float s = size;
        float u = s / 100f;

        // A soft shadow grounds the mark on the surface.
        g.setColor(new Color(63, 86, 190, 18));
        g.fill(new Ellipse2D.Float(x + u * 18, y + u * 90, u * 64, u * 6));

        // The two vertical bars.
        paintBar(g, x + u * 8, y + u * 4, u * 34, u * 84, BAR_LEFT_TOP, BAR_LEFT_BOTTOM);
        paintBar(g, x + u * 58, y + u * 4, u * 34, u * 84, BAR_RIGHT_TOP, BAR_RIGHT_BOTTOM);

        // The ribbon that connects the bars: it leaves the left bar, dips through the middle and
        // rises again to the right bar.
        float thickness = u * 22;
        CubicCurve2D curve = new CubicCurve2D.Float(
                x + u * 11, y + u * 37,
                x + u * 33, y + u * 70,
                x + u * 67, y + u * 70,
                x + u * 89, y + u * 35);

        // The shadow of the ribbon on the bars gives the overlap its depth.
        g.setPaint(new GradientPaint(x, y, new Color(30, 40, 120, 70), x + s, y, new Color(30, 40, 120, 40)));
        g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.translate(0, u * 2);
        g.draw(curve);
        g.translate(0, -u * 2);

        g.setPaint(new GradientPaint(x, y, RIBBON_LEFT, x + s, y, RIBBON_RIGHT));
        g.draw(curve);

        // A bright highlight along the upper edge of the ribbon.
        g.setPaint(new GradientPaint(x, y, new Color(255, 255, 255, 210), x + s, y, new Color(255, 255, 255, 70)));
        g.setStroke(new BasicStroke(thickness * 0.24f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        CubicCurve2D highlight = new CubicCurve2D.Float(
                x + u * 13, y + u * 30,
                x + u * 34, y + u * 62,
                x + u * 66, y + u * 62,
                x + u * 87, y + u * 28);
        g.draw(highlight);
    }

    /** Draws one rounded bar with its vertical gradient, rim light, and glossy top. */
    private void paintBar(Graphics2D g, float x, float y, float width, float height,
            Color top, Color bottom) {
        float radius = width / 2f;
        RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, width, height, radius, radius);

        g.setPaint(new GradientPaint(x, y, top, x, y + height, bottom));
        g.fill(shape);

        // The rim light along the edge of the bar.
        g.setColor(new Color(255, 255, 255, 130));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, width - 1f, height - 1f, radius, radius));

        // The glossy dome on the upper part of the bar.
        GeneralPath gloss = new GeneralPath(new RoundRectangle2D.Float(x, y, width, height, radius, radius));
        g.setClip(gloss);
        g.setPaint(new GradientPaint(x, y, new Color(255, 255, 255, 185), x, y + height * 0.42f,
                new Color(255, 255, 255, 0)));
        g.fill(new Ellipse2D.Float(x - width * 0.2f, y - height * 0.02f, width * 1.4f, height * 0.4f));
        // A darker inner edge at the bottom completes the rounded volume.
        g.setPaint(new GradientPaint(x, y + height * 0.72f, new Color(0, 0, 60, 0),
                x, y + height, new Color(0, 0, 60, 55)));
        g.fill(new Ellipse2D.Float(x, y + height * 0.55f, width, height * 0.5f));
        g.setClip(null);
    }

    /** Draws the wordmark with a blue to violet gradient. */
    private void paintWordmark(Graphics2D g, int x, int y, int width) {
        g.setFont(wordmarkFont);
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(WORDMARK);
        int textX = x + (width - textWidth) / 2;
        int textY = y + metrics.getAscent();
        g.setPaint(new GradientPaint(textX, 0, WORD_LEFT, textX + textWidth, 0, WORD_RIGHT));
        g.drawString(WORDMARK, textX, textY);
    }

    /** The logo as an image, for reports and documentation. */
    public static BufferedImage render(int size) {
        AppLogo logo = mark(size);
        logo.setSize(size, size);
        BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = canvas.createGraphics();
        logo.paintComponent(g);
        g.dispose();
        return canvas;
    }

    /** The square mark as the icon of the application window. */
    public static java.awt.Image windowIcon() {
        return render(64);
    }
}
