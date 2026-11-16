package id.edu.homeschool.ui.design;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Page title, supporting description and an optional slot for page level actions. */
public final class PageHeader extends JPanel {

    private final JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, Theme.SPACE_2, 0));
    private final JLabel description = Ui.muted("");
    private final JLabel eyebrow = Ui.subtle("");
    private final JLabel heading;

    public PageHeader(String title, String description) {
        this(title, description, null);
    }

    /**
     * @param title       the name of the page
     * @param description one line that explains the content of the page
     * @param eyebrow     a short label above the title, used for greetings and context
     */
    public PageHeader(String title, String description, String eyebrow) {
        super(new BorderLayout(Theme.SPACE_4, 0));
        setBackground(Theme.CANVAS);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SPACE_4, 0));

        JPanel text = new JPanel(new BorderLayout(0, 3));
        text.setBackground(Theme.CANVAS);
        text.setOpaque(false);
        heading = Ui.title(title);
        this.description.setText(description == null ? "" : description);
        this.eyebrow.setFont(Theme.FONT_11_BOLD);
        this.eyebrow.setForeground(Theme.ACCENT);
        this.eyebrow.setVisible(false);
        text.add(this.eyebrow, BorderLayout.NORTH);
        text.add(heading, BorderLayout.CENTER);
        text.add(this.description, BorderLayout.SOUTH);
        if (eyebrow != null && !eyebrow.isBlank()) setEyebrow(eyebrow);

        actions.setBackground(Theme.CANVAS);
        actions.setOpaque(false);

        add(text, BorderLayout.CENTER);
        add(actions, BorderLayout.EAST);
    }

    public PageHeader addAction(Component component) {
        actions.add(component);
        return this;
    }

    public void setDescription(String text) {
        description.setText(text == null ? "" : text);
    }

    /** Replaces the title of the page, used by the greeting of the dashboard. */
    public void setTitle(String text) {
        heading.setText(text == null ? "" : text);
    }

    /** Replaces the small label above the title. */
    public void setEyebrow(String text) {
        String value = text == null ? "" : text.trim();
        eyebrow.setText(value.toUpperCase(java.util.Locale.ROOT));
        eyebrow.setVisible(!value.isEmpty());
    }
}
