package id.edu.homeschool.ui.design;

import java.awt.Color;
import javax.swing.UIManager;

/**
 * The shared look and feel defaults.
 *
 * <p>Every control that is not painted by the application itself, most notably the dropdown
 * popup, reads its colours from the look and feel. Applying the defaults in one place keeps
 * those parts in the same palette as the painted surfaces and makes the appearance independent
 * of the look and feel of the host platform.</p>
 */
public final class LookAndFeel {

    private LookAndFeel() { }

    /** Applies the shared defaults; safe to call more than once. */
    public static void apply() {
        try {
            put("TextField.font", Theme.FONT_13);
            put("Label.font", Theme.FONT_13);
            put("Button.font", Theme.FONT_13);
            put("TextArea.font", Theme.FONT_13);
            put("List.font", Theme.FONT_13);
            put("ComboBox.font", Theme.FONT_13);
            put("CheckBox.font", Theme.FONT_13);
            put("RadioButton.font", Theme.FONT_13);
            put("TabbedPane.font", Theme.FONT_13);
            put("Table.font", Theme.FONT_13);
            put("TableHeader.font", Theme.FONT_11_BOLD);

            // The dropdown body is transparent so the painted glass surface stays visible.
            put("ComboBox.background", new Color(0, 0, 0, 0));
            put("ComboBox.disabledBackground", new Color(0, 0, 0, 0));
            put("ComboBox.foreground", Theme.TEXT);
            put("ComboBox.disabledForeground", Theme.TEXT_SUBTLE);
            put("ComboBox.selectionBackground", Theme.alpha(Theme.ACCENT, 34));
            put("ComboBox.selectionForeground", Theme.TEXT);
            put("ComboBox.buttonBackground", new Color(0, 0, 0, 0));
            put("ComboBox.buttonDarkShadow", Theme.TEXT_MUTED);
            put("ComboBox.buttonHighlight", Theme.alpha(Color.WHITE, 140));
            put("ComboBox.buttonShadow", Theme.alpha(Theme.TEXT_SUBTLE, 140));
            // A slim arrow keeps the dropdown in proportion with the rounded text fields.
            put("ComboBox.squareButton", Boolean.FALSE);

            // The popup list of the dropdown uses the same palette as the tables.
            put("List.background", Theme.SURFACE);
            put("List.foreground", Theme.TEXT);
            put("List.selectionBackground", Theme.alpha(Theme.ACCENT, 34));
            put("List.selectionForeground", Theme.TEXT);
            put("List.focusBackground", Theme.alpha(Theme.ACCENT, 34));

            put("ScrollBar.background", new Color(0, 0, 0, 0));
            put("ScrollBar.thumb", Theme.alpha(Theme.TEXT_SUBTLE, 120));
            put("ScrollBar.thumbShadow", Theme.alpha(Theme.TEXT_SUBTLE, 120));
            put("ScrollBar.thumbHighlight", Theme.alpha(Theme.ACCENT, 190));
            put("ScrollBar.track", new Color(0, 0, 0, 0));
            put("ScrollBar.width", 10);

            put("ToolTip.background", Theme.GLASS_SOLID);
            put("ToolTip.foreground", Theme.TEXT);
            put("ToolTip.font", Theme.FONT_12);
            put("ToolTip.border", javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));

            put("Viewport.background", Theme.GLASS_TABLE);
            put("TableHeader.background", Theme.alpha(Theme.ACCENT_SOFT, 190));
            put("TableHeader.foreground", Theme.TEXT_MUTED);
        } catch (RuntimeException ignored) {
            // A restricted environment keeps the platform defaults; the painted parts still work.
        }
    }

    private static void put(String key, Object value) {
        UIManager.put(key, value);
    }
}
