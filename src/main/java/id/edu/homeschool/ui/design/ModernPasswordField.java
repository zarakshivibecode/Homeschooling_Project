package id.edu.homeschool.ui.design;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JPasswordField;

/**
 * The password counterpart of {@link ModernTextField}. The text stays masked and is never
 * copied into a string, so the secure behaviour of a password field is preserved; only the
 * painted surface changes.
 */
public class ModernPasswordField extends JPasswordField {

    private boolean focused;

    public ModernPasswordField() {
        setFont(Theme.FONT_13);
        setForeground(Theme.TEXT);
        setCaretColor(Theme.ACCENT);
        setSelectionColor(Theme.alpha(Theme.ACCENT, 45));
        setSelectedTextColor(Theme.TEXT);
        setOpaque(false);
        setEchoChar('•');
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, Theme.SPACE_4, 0, Theme.SPACE_4));
        setPreferredSize(new Dimension(0, Theme.CONTROL_HEIGHT));
        addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent event) { focused = true; repaint(); }
            @Override public void focusLost(FocusEvent event) { focused = false; repaint(); }
        });
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Glass.prepare(g);
        Glass.paintField(g, 0, 0, getWidth(), getHeight(), Theme.RADIUS_SM, focused);
        g.dispose();

        super.paintComponent(graphics);
    }
}
