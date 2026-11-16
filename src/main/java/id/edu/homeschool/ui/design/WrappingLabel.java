package id.edu.homeschool.ui.design;

import java.awt.Dimension;
import javax.swing.JLabel;

/**
 * A label that wraps its text to the width of the component.
 *
 * <p>Plain HTML labels need a fixed width, which breaks as soon as the window becomes narrower.
 * This label rebuilds its markup whenever it is resized, so a paragraph always fills the width
 * that is available without ever being cut off.</p>
 */
public final class WrappingLabel extends JLabel {

    private String raw = "";
    private boolean updating;

    public WrappingLabel(String text) {
        setOpaque(false);
        setVerticalAlignment(javax.swing.SwingConstants.TOP);
        setText(text);
    }

    @Override public void setText(String text) {
        this.raw = text == null ? "" : text;
        updateMarkup();
    }

    /**
     * The label is repositioned with {@code setBounds}, which never calls {@code setSize}, so the
     * markup is rebuilt here whenever the width changes.
     */
    @Override public void setBounds(int x, int y, int width, int height) {
        boolean changed = getWidth() != width;
        super.setBounds(x, y, width, height);
        if (changed && updateMarkup()) revalidate();
    }

    /** Rebuilds the markup and reports whether the text changed. */
    private boolean updateMarkup() {
        if (updating) return false;
        updating = true;
        try {
            int width = Math.max(160, getWidth() - 4);
            String markup = "<html><div style='width:" + width + "px'>" + escape(raw) + "</div></html>";
            if (markup.equals(getText())) return false;
            super.setText(markup);
            return true;
        } finally {
            updating = false;
        }
    }

    /** Keeps the markup of the text valid. */
    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(Math.max(size.width, 160), size.height);
    }
}
