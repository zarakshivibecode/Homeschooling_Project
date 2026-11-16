package id.edu.homeschool.ui.design;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Application bar above the page content: the current scope on the left, the account on the right. */
public final class TopBar extends GlassPanel {

    private final JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, Theme.SPACE_2, 0));
    private final JPanel right = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, Theme.SPACE_2, 0));
    private final JPanel identity = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, Theme.SPACE_2, 0));

    public TopBar() {
        super(new BorderLayout());
        setFill(Theme.GLASS_SOLID);
        setRadius(Theme.RADIUS);
        setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_2, Theme.SPACE_4, Theme.SPACE_2, Theme.SPACE_3));

        left.setBackground(Theme.CANVAS);
        left.setOpaque(false);
        right.setBackground(Theme.CANVAS);
        right.setOpaque(false);
        identity.setBackground(Theme.CANVAS);
        identity.setOpaque(false);
        right.add(identity);
        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
    }

    public TopBar addLeft(JComponent component) {
        left.add(component);
        return this;
    }

    public TopBar addRight(JComponent component) {
        right.add(component);
        return this;
    }

    /** Replaces the account chip so it follows the signed-in account after a profile edit. */
    public TopBar setIdentity(JComponent component) {
        identity.removeAll();
        identity.add(component);
        identity.revalidate();
        identity.repaint();
        return this;
    }

    /** Builds the account chip shown on the right of the bar. */
    public static JComponent accountChip(String name, String role) {
        JPanel chip = new JPanel(new BorderLayout(Theme.SPACE_3, 0));
        chip.setBackground(Theme.CANVAS);
        chip.setOpaque(false);

        JLabel avatar = new JLabel(initials(name), javax.swing.SwingConstants.CENTER);
        avatar.setFont(Theme.FONT_11_BOLD);
        avatar.setForeground(Theme.TEXT_ON_ACCENT);
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(30, 30));

        JPanel badge = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(java.awt.Graphics graphics) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                Glass.prepare(g);
                int w = getWidth();
                int h = getHeight();
                int r = Theme.RADIUS_SM;
                g.setPaint(new java.awt.GradientPaint(0, 0, Theme.ACCENT, 0, h, Theme.ACCENT_DEEP));
                g.fillRoundRect(0, 0, w, h, r, r);
                g.setColor(Theme.alpha(java.awt.Color.WHITE, 120));
                g.drawLine(r / 2, 1, w - r / 2, 1);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(30, 30));
        badge.add(avatar, BorderLayout.CENTER);

        JPanel text = new JPanel(new BorderLayout(0, 1));
        text.setBackground(Theme.CANVAS);
        text.setOpaque(false);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(Theme.FONT_13_BOLD);
        nameLabel.setForeground(Theme.TEXT);
        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(Theme.FONT_11);
        roleLabel.setForeground(Theme.TEXT_MUTED);
        text.add(nameLabel, BorderLayout.NORTH);
        text.add(roleLabel, BorderLayout.CENTER);

        chip.add(badge, BorderLayout.WEST);
        chip.add(text, BorderLayout.CENTER);
        return chip;
    }

    /** Builds a compact logout control. */
    public static JButton logoutButton() {
        JButton button = Ui.button("Keluar", Ui.Variant.SECONDARY);
        button.setToolTipText("Akhiri sesi dan kembali ke layar masuk");
        return button;
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase(java.util.Locale.ROOT);
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase(java.util.Locale.ROOT);
    }
}
