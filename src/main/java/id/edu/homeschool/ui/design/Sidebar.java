package id.edu.homeschool.ui.design;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Floating glass navigation with grouped entries, line icons and a single active marker. */
public final class Sidebar extends JPanel {

    private final JPanel items = new JPanel();
    private final Map<String, NavItem> entries = new LinkedHashMap<>();
    private String activeId;

    public Sidebar() {
        super(new BorderLayout());
        setBackground(Theme.CANVAS);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_4, Theme.SPACE_4, Theme.SPACE_4, 0));
        setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH + Theme.SPACE_4, 0));

        GlassPanel surface = new GlassPanel(new BorderLayout(0, Theme.SPACE_2));
        surface.setFill(Theme.GLASS_SOLID);
        surface.setRadius(Theme.RADIUS_LG);
        surface.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_5, Theme.SPACE_3, Theme.SPACE_4, Theme.SPACE_3));

        items.setLayout(new javax.swing.BoxLayout(items, javax.swing.BoxLayout.Y_AXIS));
        items.setBackground(Theme.CANVAS);
        items.setOpaque(false);
        items.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SPACE_2, 0));

        surface.add(brand(), BorderLayout.NORTH);
        surface.add(items, BorderLayout.CENTER);
        surface.add(footer(), BorderLayout.SOUTH);
        add(surface, BorderLayout.CENTER);
    }

    private JComponent brand() {
        JPanel brand = new JPanel(new BorderLayout(Theme.SPACE_3, 2));
        brand.setBackground(Theme.CANVAS);
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(0, Theme.SPACE_2, Theme.SPACE_4, Theme.SPACE_2));

        AppLogo mark = AppLogo.mark(36);
        mark.setToolTipText(AppLogo.WORDMARK + " - Personal Homeschooling Learning System");

        JPanel text = new JPanel(new BorderLayout(0, 1));
        text.setBackground(Theme.CANVAS);
        text.setOpaque(false);
        JLabel title = new JLabel(AppLogo.WORDMARK);
        title.setFont(Theme.FONT_16_BOLD);
        title.setForeground(Theme.TEXT);
        JLabel caption = new JLabel("Ruang belajar personal");
        caption.setFont(Theme.FONT_11);
        caption.setForeground(Theme.TEXT_MUTED);
        text.add(title, BorderLayout.NORTH);
        text.add(caption, BorderLayout.CENTER);

        JPanel row = new JPanel(new BorderLayout(Theme.SPACE_3, 0));
        row.setBackground(Theme.CANVAS);
        row.setOpaque(false);
        row.add(mark, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private JComponent footer() {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setBackground(Theme.CANVAS);
        box.setOpaque(false);
        box.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_3, Theme.SPACE_2, Theme.SPACE_2, Theme.SPACE_2));

        GlassPanel chip = new GlassPanel(new BorderLayout(Theme.SPACE_2, 0));
        chip.setFill(Theme.alpha(Theme.ACCENT_SOFT, 180));
        chip.setOutline(Theme.alpha(Theme.BORDER, 160));
        chip.setRadius(Theme.RADIUS_SM);
        chip.setShadow(false);
        chip.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JLabel dot = new JLabel("●");
        dot.setFont(Theme.FONT_10);
        dot.setForeground(Theme.SUCCESS);

        JLabel caption = new JLabel("Penyimpanan lokal aktif");
        caption.setFont(Theme.FONT_10_BOLD);
        caption.setForeground(Theme.TEXT_MUTED);

        chip.add(dot, BorderLayout.WEST);
        chip.add(caption, BorderLayout.CENTER);
        box.add(chip, BorderLayout.CENTER);
        return box;
    }

    /** Adds a small uppercase group label. */
    public void addSection(String title) {
        JLabel label = new JLabel(title.toUpperCase(java.util.Locale.ROOT));
        label.setFont(Theme.FONT_10_BOLD);
        label.setForeground(Theme.TEXT_MUTED);
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_4, Theme.SPACE_3, Theme.SPACE_2, 0));
        items.add(label);
    }

    /** Adds a navigation entry that calls {@code onSelect} when clicked. */
    public void addItem(String id, String label, NavIcon icon, Runnable onSelect) {
        NavItem item = new NavItem(label, icon);
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        item.addActionListener(event -> onSelect.run());
        entries.put(id, item);
        items.add(item);
        items.add(javax.swing.Box.createVerticalStrut(2));
    }

    public void select(String id) {
        activeId = id;
        entries.forEach((key, item) -> item.setActive(key.equals(id)));
    }

    public String activeId() { return activeId; }

    /** A navigation entry that paints its own active state. */
    private static final class NavItem extends JButton {
        private final NavIcon icon;
        private boolean active;
        private boolean hover;

        NavItem(String text, NavIcon icon) {
            super(text);
            this.icon = icon;
            setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            setFont(Theme.FONT_13);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(0, Theme.SPACE_3, 0, Theme.SPACE_2));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent event) { hover = true; repaint(); }
                @Override public void mouseExited(java.awt.event.MouseEvent event) { hover = false; repaint(); }
            });
        }

        void setActive(boolean value) { active = value; repaint(); }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Glass.prepare(g);
            int width = getWidth();
            int height = getHeight();
            int radius = Theme.RADIUS_SM;

            if (active) {
                // Liquid glass pill surface with soft gradient
                Color top = Theme.alpha(Theme.ACCENT_SOFT, 245);
                Color bottom = Theme.alpha(new Color(0xE0, 0xE7, 0xFF), 245);
                g.setPaint(new java.awt.GradientPaint(0, 0, top, 0, height, bottom));
                g.fillRoundRect(0, 0, width, height, radius, radius);

                // Subtle outline
                g.setColor(Theme.alpha(Theme.ACCENT, 95));
                g.setStroke(new java.awt.BasicStroke(1f));
                g.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);

                // Top specular highlight
                g.setColor(Theme.alpha(Color.WHITE, 160));
                g.drawLine(radius / 2, 1, width - radius / 2, 1);

                // Active vertical capsule indicator bar with glowing halo
                g.setColor(Theme.alpha(Theme.ACCENT, 45));
                g.fillRoundRect(2, (height - 20) / 2, 5, 20, 5, 5);
                g.setColor(Theme.ACCENT);
                g.fillRoundRect(3, (height - 18) / 2, 3, 18, 3, 3);
            } else if (hover) {
                g.setColor(Theme.alpha(Theme.ACCENT_SOFT, 180));
                g.fillRoundRect(0, 0, width, height, radius, radius);
                g.setColor(Theme.alpha(Theme.BORDER, 140));
                g.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);
            }

            g.setFont(active ? Theme.FONT_13_BOLD : getFont());
            FontMetrics metrics = g.getFontMetrics();
            int textY = (height + metrics.getAscent() - metrics.getDescent()) / 2;
            Color iconTone = active ? Theme.ACCENT : hover ? Theme.TEXT : Theme.TEXT_SUBTLE;
            Color textTone = active ? Theme.ACCENT_DEEP : hover ? Theme.TEXT : Theme.TEXT_MUTED;
            if (icon != null) icon.paint(g, Theme.SPACE_3 + 2, (height - 16) / 2, 16, iconTone);
            g.setColor(textTone);
            g.drawString(getText(), Theme.SPACE_3 + 28, textY);
            g.dispose();
        }
    }
}
