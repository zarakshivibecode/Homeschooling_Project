package id.edu.homeschool.ui.design;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Informative placeholder shown when a list has nothing to display. */
public final class EmptyState extends JPanel {

    public EmptyState(String title, String description) {
        super(new BorderLayout());
        setBackground(Theme.CANVAS);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_7, Theme.SPACE_5, Theme.SPACE_7, Theme.SPACE_5));

        JPanel column = new JPanel();
        column.setLayout(new javax.swing.BoxLayout(column, javax.swing.BoxLayout.Y_AXIS));
        column.setBackground(Theme.CANVAS);
        column.setOpaque(false);

        JLabel glyph = new JLabel(new GlyphIcon());
        glyph.setAlignmentX(0.5f);
        glyph.setMaximumSize(new Dimension(Integer.MAX_VALUE, GlyphIcon.SIZE));

        JLabel heading = Ui.heading(title);
        heading.setAlignmentX(0.5f);
        heading.setHorizontalAlignment(JLabel.CENTER);

        JLabel body = new JLabel("<html><div style='text-align:center;width:420px'>"
                + (description == null ? "" : description) + "</div></html>");
        body.setFont(Theme.FONT_12);
        body.setForeground(Theme.TEXT_MUTED);
        body.setAlignmentX(0.5f);
        body.setHorizontalAlignment(JLabel.CENTER);

        column.add(javax.swing.Box.createVerticalGlue());
        column.add(glyph);
        column.add(javax.swing.Box.createVerticalStrut(Theme.SPACE_4));
        column.add(heading);
        column.add(javax.swing.Box.createVerticalStrut(Theme.SPACE_2));
        column.add(body);
        column.add(javax.swing.Box.createVerticalGlue());

        add(column, BorderLayout.CENTER);
    }

    /** A restrained glyph so the empty state is recognisable without shipping image assets. */
    private static final class GlyphIcon implements javax.swing.Icon {
        private static final int SIZE = 34;

        @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Theme.alpha(Theme.ACCENT, 24));
            g.fillRoundRect(x, y, SIZE, SIZE, Theme.RADIUS_SM, Theme.RADIUS_SM);
            g.setColor(Theme.alpha(Theme.ACCENT, 170));
            g.setStroke(new java.awt.BasicStroke(1.6f));
            g.drawRoundRect(x + 9, y + 11, 16, 11, 3, 3);
            g.drawLine(x + 9, y + 15, x + 25, y + 15);
            g.drawLine(x + 13, y + 18, x + 21, y + 18);
            g.dispose();
        }

        @Override public int getIconWidth() { return SIZE; }
        @Override public int getIconHeight() { return SIZE; }
    }
}
