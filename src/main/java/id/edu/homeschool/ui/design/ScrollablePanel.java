package id.edu.homeschool.ui.design;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * A panel that can live inside a scroll area and always follows the width of the viewport.
 *
 * <p>Without this, a page that is put into a scroll pane keeps the width of its preferred
 * size, so a responsive grid inside it would never see the real width of the window and the
 * content could run past the right edge. Tracking the viewport width keeps the layout inside
 * the visible area while the height may still grow beyond it.</p>
 */
public class ScrollablePanel extends JPanel implements Scrollable {

    public ScrollablePanel(LayoutManager layout) {
        super(layout);
    }

    @Override public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
        return 20;
    }

    @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? visible.height : visible.width;
    }

    /** The content always uses the width of the viewport. */
    @Override public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    /** The content may grow taller than the viewport, so it stays scrollable. */
    @Override public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
