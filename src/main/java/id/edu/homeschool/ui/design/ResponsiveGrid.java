package id.edu.homeschool.ui.design;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * A grid that reflows its children when the available width changes.
 *
 * <p>The summary cards of the dashboard and the progress report used a fixed column count,
 * which squeezed the cards as soon as the window became narrower. This layout keeps a minimum
 * column width and recomputes the number of columns on every layout pass, so the cards always
 * keep a comfortable size and wrap onto another row instead of being cut off.</p>
 */
public final class ResponsiveGrid extends JPanel {

    private final int minColumnWidth;
    private final int maxColumns;
    private final int gap;

    /**
     * @param minColumnWidth the narrowest width a single card may take
     * @param maxColumns     the largest number of columns used at the preferred width
     * @param gap            the space between two cards
     */
    public ResponsiveGrid(int minColumnWidth, int maxColumns, int gap) {
        this.minColumnWidth = minColumnWidth;
        this.maxColumns = maxColumns;
        this.gap = gap;
        setBackground(Theme.CANVAS);
        setOpaque(false);
        setLayout(new Grid());
    }

    /** Adds a card to the grid. */
    public ResponsiveGrid add(Component child) {
        super.add(child);
        return this;
    }

    /**
     * The preferred size follows the width that is currently available, so the grid always asks
     * for the height of the rows that it really uses.
     */
    @Override public Dimension getPreferredSize() {
        return size(getWidth() > 0 ? getWidth() : fallbackWidth());
    }

    @Override public Dimension getMinimumSize() {
        return new Dimension(minColumnWidth, 0);
    }

    /** A new width can change the number of rows, so the parent is asked to lay out again. */
    @Override public void setBounds(int x, int y, int width, int height) {
        boolean resized = getWidth() != width;
        super.setBounds(x, y, width, height);
        if (resized) revalidate();
    }

    /** The width that is assumed before the component has been laid out for the first time. */
    private int fallbackWidth() {
        return maxColumns * minColumnWidth + gap * (maxColumns - 1);
    }

    /** The number of columns that fit into the given width. */
    public int columnsFor(int width) {
        if (width <= 0) return 1;
        int usable = width - gap;
        int columns = usable / (minColumnWidth + gap);
        return Math.max(1, Math.min(maxColumns, columns));
    }

    /** The columns that are used for the given width, with balanced rows. */
    private int columnsFor(Container parent, int width) {
        int columns = columnsFor(width);
        int count = visibleChildren(parent).size();
        if (count <= columns) return columns;
        int rows = (count + columns - 1) / columns;
        int lastRow = count - (rows - 1) * columns;
        if (lastRow < columns - 1) {
            // A last row that is almost empty looks unbalanced, so the columns are reduced.
            int balanced = (int) Math.ceil((double) count / rows);
            if (balanced < columns && balanced >= 1) columns = balanced;
        }
        return columns;
    }

    /** The preferred size of the grid for a given width. */
    private Dimension size(int width) {
        List<Component> children = visibleChildren(this);
        if (children.isEmpty()) return new Dimension(minColumnWidth, 0);
        int columns = columnsFor(this, width);
        int rows = rowCount(children, columns);
        Insets insets = getInsets();
        int height = 0;
        for (int row = 0; row < rows; row++) {
            height += rowHeight(children, row, columns) + gap;
        }
        height = Math.max(0, height - gap);
        return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
    }

    private static int rowCount(List<Component> children, int columns) {
        return children.isEmpty() ? 0 : (children.size() + columns - 1) / columns;
    }

    /** The tallest card of a row defines the height of that row. */
    private static int rowHeight(List<Component> children, int row, int columns) {
        int height = 0;
        for (int column = 0; column < columns; column++) {
            int index = row * columns + column;
            if (index >= children.size()) break;
            height = Math.max(height, children.get(index).getPreferredSize().height);
        }
        return height;
    }

    private static List<Component> visibleChildren(Container parent) {
        List<Component> list = new ArrayList<>();
        for (Component child : parent.getComponents()) {
            if (child.isVisible()) list.add(child);
        }
        return list;
    }

    /** The layout that positions the cards in a responsive number of columns. */
    private final class Grid implements LayoutManager {

        @Override public void addLayoutComponent(String name, Component component) { }

        @Override public void removeLayoutComponent(Component component) { }

        @Override public Dimension preferredLayoutSize(Container parent) {
            return size(parent.getWidth() > 0 ? parent.getWidth() : fallbackWidth());
        }

        @Override public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(minColumnWidth, 0);
        }

        @Override public void layoutContainer(Container parent) {
            int columns = columnsFor(parent, parent.getWidth());
            List<Component> children = visibleChildren(parent);
            int rows = rowCount(children, columns);
            if (rows == 0) return;

            Insets insets = parent.getInsets();
            int totalWidth = parent.getWidth() - insets.left - insets.right;
            int cellWidth = (totalWidth - gap * (columns - 1)) / columns;
            if (cellWidth <= 0) return;

            int y = insets.top;
            for (int row = 0; row < rows; row++) {
                int rowHeight = rowHeight(children, row, columns);
                for (int column = 0; column < columns; column++) {
                    int index = row * columns + column;
                    if (index >= children.size()) break;
                    Component child = children.get(index);
                    child.setBounds(insets.left + column * (cellWidth + gap), y, cellWidth, rowHeight);
                }
                y += rowHeight + gap;
            }
        }
    }
}
