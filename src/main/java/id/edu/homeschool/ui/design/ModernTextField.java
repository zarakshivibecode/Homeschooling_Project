package id.edu.homeschool.ui.design;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

/**
 * A text input painted as a frosted glass surface.
 *
 * <p>Swing's own text field cannot render the translucent rounded look of the rest of the
 * application, so the field paints its own background: a translucent white body, a hairline
 * edge and a soft indigo ring while the field has the keyboard focus. The caret, the text and
 * every listener keep working exactly as they do in a plain {@link JTextField}.</p>
 */
public class ModernTextField extends JTextField {

    private String placeholder = "";
    private boolean focused;

    public ModernTextField() {
        this("");
    }

    public ModernTextField(String value) {
        super(value);
        setFont(Theme.FONT_13);
        setForeground(Theme.TEXT);
        setCaretColor(Theme.ACCENT);
        setSelectionColor(Theme.alpha(Theme.ACCENT, 45));
        setSelectedTextColor(Theme.TEXT);
        setOpaque(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, Theme.SPACE_4, 0, Theme.SPACE_4));
        setPreferredSize(new Dimension(0, Theme.CONTROL_HEIGHT));
        addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent event) { focused = true; repaint(); }
            @Override public void focusLost(FocusEvent event) { focused = false; repaint(); }
        });
    }

    /** Sets the hint shown while the field is empty and does not have the focus. */
    public ModernTextField setPlaceholder(String text) {
        this.placeholder = text == null ? "" : text;
        repaint();
        return this;
    }

    public String placeholder() { return placeholder; }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Glass.prepare(g);
        int width = getWidth();
        int height = getHeight();
        Glass.paintField(g, 0, 0, width, height, Theme.RADIUS_SM, focused);
        g.dispose();

        super.paintComponent(graphics);

        if (getText().isEmpty() && !placeholder.isEmpty() && !focused) {
            Graphics2D hint = (Graphics2D) graphics.create();
            Glass.prepare(hint);
            hint.setColor(Theme.alpha(Theme.TEXT_SUBTLE, 210));
            hint.setFont(getFont());
            java.awt.FontMetrics metrics = hint.getFontMetrics();
            int textY = (height + metrics.getAscent() - metrics.getDescent()) / 2;
            hint.drawString(placeholder, Theme.SPACE_4, textY);
            hint.dispose();
        }
    }
}
