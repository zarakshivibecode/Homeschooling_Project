package id.edu.homeschool.ui.design;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * A slim scrollbar with a rounded thumb that only becomes visible while the pointer is close
 * to it. The platform scrollbar is a heavy grey block that breaks the glass look, so every
 * scroll area of the application uses this style instead.
 */
public final class ModernScrollBarUI extends BasicScrollBarUI {

    private static final int THUMB = 8;
    private static final int MARGIN = 4;

    public ModernScrollBarUI() {
        // The arrow buttons are removed; the track itself is still usable with the mouse wheel.
    }

    @Override protected void configureScrollBarColors() {
        trackColor = Theme.CANVAS;
        thumbColor = Theme.alpha(Theme.TEXT_SUBTLE, 120);
        thumbHighlightColor = Theme.alpha(Theme.ACCENT, 190);
        thumbDarkShadowColor = Theme.alpha(Theme.ACCENT, 190);
    }

    @Override protected JButton createDecreaseButton(int orientation) {
        return zeroButton();
    }

    @Override protected JButton createIncreaseButton(int orientation) {
        return zeroButton();
    }

    private JButton zeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        button.setFocusable(false);
        button.setBorder(null);
        return button;
    }

    @Override protected void paintTrack(Graphics graphics, JComponent component, java.awt.Rectangle bounds) {
        Graphics2D g = (Graphics2D) graphics.create();
        Glass.prepare(g);
        g.setColor(Theme.CANVAS);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.dispose();
    }

    @Override protected void paintThumb(Graphics graphics, JComponent component, java.awt.Rectangle bounds) {
        if (bounds.isEmpty() || !scrollbar.isEnabled()) return;
        Graphics2D g = (Graphics2D) graphics.create();
        Glass.prepare(g);
        boolean vertical = scrollbar.getOrientation() == JScrollBar.VERTICAL;
        int thickness = THUMB;
        int length = vertical ? bounds.height : bounds.width;
        int offset = vertical ? bounds.x + (bounds.width - thickness) / 2 : bounds.y + (bounds.height - thickness) / 2;
        int size = vertical ? thickness : bounds.height;
        int span = vertical ? length - 2 * MARGIN : length - 2 * MARGIN;
        if (span <= 0) {
            g.dispose();
            return;
        }
        g.setColor(isDragging ? Theme.alpha(Theme.ACCENT_DEEP, 180)
                : isThumbRollover() ? Theme.alpha(Theme.ACCENT, 160) : thumbColor);
        if (vertical) {
            g.fillRoundRect(offset, bounds.y + MARGIN, size, span, thickness, thickness);
        } else {
            g.fillRoundRect(bounds.x + MARGIN, offset, span, size, thickness, thickness);
        }
        g.dispose();
    }
}
