package id.edu.homeschool.ui.design;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * Modal form dialog. The submit action runs inside the dialog so a validation error keeps the
 * form open and shows the reason instead of silently closing.
 *
 * <p>Every field carries its own label, a hint and a place for a message that belongs to that
 * field, so a problem is reported next to the input that caused it. A message about the form as
 * a whole, for example a rule of the service, is shown below the fields.</p>
 */
public final class FormDialog {

    /** One labelled input. The key is used to read the value back after the form is submitted. */
    public record Field(String key, String label, JComponent editor, String hint, boolean required) {

        public static Field text(String key, String label, String value) {
            return new Field(key, label, Ui.textField(value), null, true);
        }

        public static Field optionalText(String key, String label, String value) {
            return new Field(key, label, Ui.textField(value), null, false);
        }

        /** A text field with a hint that is shown while the field is still empty. */
        public static Field hintedText(String key, String label, String value, String placeholder) {
            JComponent editor = Ui.textField(value);
            if (editor instanceof ModernTextField modern) modern.setPlaceholder(placeholder);
            return new Field(key, label, editor, null, true);
        }

        public static Field password(String key, String label) {
            return new Field(key, label, Ui.passwordField(), null, true);
        }

        /** A multi line input used for instructions, descriptions and learning content. */
        public static Field multiline(String key, String label, String value, int rows) {
            JTextArea area = new JTextArea(value == null ? "" : value);
            area.setFont(Theme.FONT_13);
            area.setForeground(Theme.TEXT);
            area.setBackground(Theme.alpha(java.awt.Color.WHITE, 235));
            area.setCaretColor(Theme.TEXT);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Theme.alpha(Theme.BORDER_STRONG, 190)),
                    BorderFactory.createEmptyBorder(7, 10, 7, 10)));
            area.setRows(rows);
            area.setPreferredSize(new Dimension(320, rows * 18 + 14));
            return new Field(key, label, area, null, false);
        }

        public static <T> Field choice(String key, String label, List<T> options, T selected) {
            JComboBox<T> combo = Ui.combo();
            options.forEach(combo::addItem);
            if (selected != null) combo.setSelectedItem(selected);
            return new Field(key, label, combo, null, true);
        }
    }

    /** One field of the form together with the label that shows its own message. */
    private static final class Slot {
        private final Field field;
        private final JLabel message = new JLabel(" ");

        Slot(Field field) {
            this.field = field;
            message.setFont(Theme.FONT_11);
            message.setForeground(Theme.DANGER);
        }

        void clear() {
            message.setText(" ");
        }

        void fail(String text) {
            message.setText(text);
        }
    }

    private FormDialog() { }

    /**
     * Opens the dialog. {@code onSubmit} receives the collected values; throwing inside it keeps
     * the dialog open and renders the message as an inline error.
     */
    public static void show(Component parent, String title, String description, List<Field> fields,
            Consumer<Map<String, Object>> onSubmit) {
        show(parent, title, description, fields, onSubmit, 520, 560);
    }

    /** Opens the dialog with an explicit size, used by forms that need a taller body. */
    public static void show(Component parent, String title, String description, List<Field> fields,
            Consumer<Map<String, Object>> onSubmit, int width, int height) {
        java.awt.Window owner = Dialogs.owner(parent);

        List<Slot> slots = new ArrayList<>();
        for (Field field : fields) slots.add(new Slot(field));

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Theme.CANVAS);
        body.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, Theme.SPACE_3, 0);
        int row = 0;
        for (Slot slot : slots) {
            constraints.gridy = row++;
            body.add(slotPanel(slot), constraints);
        }
        // A filler row keeps the fields at the top when the dialog is taller than the content.
        constraints.gridy = row;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        body.add(Box.createVerticalGlue(), constraints);

        Notice notice = new Notice();

        JPanel content = new JPanel(new BorderLayout(0, Theme.SPACE_2));
        content.setBackground(Theme.CANVAS);
        content.setOpaque(false);
        content.add(GlassDialog.scrollBody(body), BorderLayout.CENTER);
        content.add(notice, BorderLayout.SOUTH);

        JButton save = Ui.button("Simpan", Ui.Variant.PRIMARY);
        JButton cancel = Ui.button("Batal", Ui.Variant.SECONDARY);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACE_2, 0));
        buttons.setBackground(Theme.CANVAS);
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(save);

        JDialog dialog = GlassDialog.create(owner, title, description, content, buttons);
        int preferredHeight = 250 + fields.size() * 76;
        dialog.setSize(width, Math.max(340, Math.min(780, preferredHeight)));
        dialog.setLocationRelativeTo(owner);

        Runnable submit = () -> {
            slots.forEach(Slot::clear);
            notice.clear();
            Map<String, Object> values = collect(fields);
            Slot missing = firstMissing(slots, values);
            if (missing != null) {
                missing.fail(missing.field.label() + " wajib diisi.");
                missing.field.editor().requestFocusInWindow();
                return;
            }
            try {
                onSubmit.accept(values);
                dialog.dispose();
            } catch (RuntimeException exception) {
                notice.show(exception.getMessage() == null ? "Data tidak dapat disimpan." : exception.getMessage(),
                        new Theme.StatusStyle(Theme.DANGER, Theme.DANGER_SOFT));
            }
        };

        save.addActionListener(event -> submit.run());
        cancel.addActionListener(event -> dialog.dispose());
        dialog.getRootPane().setDefaultButton(save);
        dialog.getRootPane().registerKeyboardAction(event -> dialog.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.setVisible(true);
    }

    /** Builds the label, the editor and the message of a single field. */
    private static JPanel slotPanel(Slot slot) {
        Field field = slot.field;
        JPanel column = new JPanel(new BorderLayout(0, 5));
        column.setBackground(Theme.CANVAS);
        column.setOpaque(false);

        JLabel label = new JLabel(field.label());
        label.setFont(Theme.FONT_13);
        label.setForeground(Theme.TEXT);
        if (field.required()) {
            // The required marker keeps its own colour so the legend stays readable.
            JPanel caption = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
            caption.setBackground(Theme.CANVAS);
            caption.setOpaque(false);
            caption.add(label);
            JLabel marker = new JLabel("*");
            marker.setFont(Theme.FONT_13);
            marker.setForeground(Theme.DANGER);
            caption.add(marker);
            column.add(caption, BorderLayout.NORTH);
        } else {
            column.add(label, BorderLayout.NORTH);
        }

        column.add(field.editor(), BorderLayout.CENTER);
        if (field.hint() != null && !field.hint().isBlank()) {
            column.add(Ui.subtle(field.hint()), BorderLayout.SOUTH);
        }
        column.add(slot.message, BorderLayout.SOUTH);
        return column;
    }

    private static Map<String, Object> collect(List<Field> fields) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Field field : fields) {
            Object value;
            if (field.editor() instanceof JTextField text) {
                value = text.getText().trim();
            } else if (field.editor() instanceof JPasswordField password) {
                value = password.getPassword();
            } else if (field.editor() instanceof JTextArea area) {
                value = area.getText().trim();
            } else if (field.editor() instanceof JComboBox<?> combo) {
                value = combo.getSelectedItem();
            } else {
                value = null;
            }
            values.put(field.key(), value);
        }
        return values;
    }

    /** The first required field that has no value, or null when the form is complete. */
    private static Slot firstMissing(List<Slot> slots, Map<String, Object> values) {
        for (Slot slot : slots) {
            Field field = slot.field;
            if (!field.required()) continue;
            Object value = values.get(field.key());
            boolean empty = value == null
                    || (value instanceof String text && text.isBlank())
                    || (value instanceof char[] secret && secret.length == 0);
            if (empty) return slot;
        }
        return null;
    }

    /** Convenience wrapper for a list of fields built in place. */
    public static List<Field> fields(Field... values) {
        List<Field> list = new ArrayList<>();
        for (Field value : values) list.add(value);
        return list;
    }

    /** Reads a text value from a submitted form. */
    public static String text(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? "" : value.toString().trim();
    }

    /** Reads a password value from a submitted form. */
    public static char[] password(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value instanceof char[] secret ? secret : new char[0];
    }

    /** Reads a selection from a submitted form. */
    @SuppressWarnings("unchecked")
    public static <T> T choice(Map<String, Object> values, String key) {
        return (T) values.get(key);
    }
}
