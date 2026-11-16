package id.edu.homeschool.ui.design;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Read-only detail window used for the full learning material, the assignment instructions and
 * the evaluation feedback. It reuses the shared glass window so the reading experience matches
 * the rest of the application.
 */
public final class DetailDialog {

    /** One titled block of text inside the dialog. */
    public record Section(String heading, String body) { }

    private DetailDialog() { }

    /** Opens the detail window with the given sections and optional badges. */
    public static void show(Component parent, String title, String subtitle, List<Section> sections,
            List<? extends JComponent> badges) {
        JPanel content = new JPanel(new BorderLayout(0, Theme.SPACE_3));
        content.setBackground(Theme.CANVAS);
        content.setOpaque(false);

        if (badges != null && !badges.isEmpty()) {
            JPanel badgeRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, Theme.SPACE_2, 0));
            badgeRow.setBackground(Theme.CANVAS);
            badgeRow.setOpaque(false);
            badges.forEach(badgeRow::add);
            content.add(badgeRow, BorderLayout.NORTH);
        }

        content.add(GlassDialog.scrollBody(textPane(sections)), BorderLayout.CENTER);

        JButton close = Ui.button("Tutup", Ui.Variant.PRIMARY);
        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, Theme.SPACE_2, 0));
        footer.setBackground(Theme.CANVAS);
        footer.setOpaque(false);
        footer.add(close);

        JDialog dialog = GlassDialog.create(Dialogs.owner(parent), title, subtitle, content, footer);
        close.addActionListener(event -> dialog.dispose());
        dialog.setSize(720, 640);
        dialog.setLocationRelativeTo(Dialogs.owner(parent));
        dialog.setVisible(true);
    }

    /** Builds the styled text of the detail window. */
    private static JTextPane textPane(List<Section> sections) {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pane.setFont(Theme.FONT_13);
        pane.setForeground(Theme.TEXT);
        pane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR));

        StyledDocument document = pane.getStyledDocument();
        javax.swing.text.Style base = pane.addStyle("base", null);
        StyleConstants.setFontFamily(base, Theme.FONT_13.getFamily());
        StyleConstants.setFontSize(base, Theme.FONT_13.getSize());
        StyleConstants.setForeground(base, Theme.TEXT);
        StyleConstants.setAlignment(base, StyleConstants.ALIGN_LEFT);

        javax.swing.text.Style headingStyle = pane.addStyle("heading", base);
        StyleConstants.setFontFamily(headingStyle, Theme.FONT_13.getFamily());
        StyleConstants.setFontSize(headingStyle, Theme.FONT_13.getSize());
        StyleConstants.setBold(headingStyle, true);
        StyleConstants.setForeground(headingStyle, Theme.TEXT);

        try {
            for (Section section : sections) {
                if (section == null) continue;
                if (section.heading() != null && !section.heading().isBlank()) {
                    document.insertString(document.getLength(), "\n" + section.heading() + "\n\n", headingStyle);
                }
                if (section.body() != null && !section.body().isBlank()) {
                    document.insertString(document.getLength(), section.body() + "\n", base);
                }
            }
        } catch (javax.swing.text.BadLocationException exception) {
            throw new IllegalStateException("Isi materi tidak dapat ditampilkan.", exception);
        }
        pane.setCaretPosition(0);
        return pane;
    }

    /** Convenience wrapper for building a section list. */
    public static List<Section> sections(Section... values) {
        List<Section> list = new ArrayList<>();
        for (Section value : values) list.add(value);
        return list;
    }
}
