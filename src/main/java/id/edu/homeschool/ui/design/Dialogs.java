package id.edu.homeschool.ui.design;

import java.awt.Component;
import java.awt.Window;

/** Consistent dialogs for errors, confirmations and read-only information. */
public final class Dialogs {

    private Dialogs() { }

    public static void error(Component parent, String title, String message) {
        GlassDialog.message(parent, title, message, GlassDialog.Kind.ERROR);
    }

    public static void info(Component parent, String title, String message) {
        GlassDialog.message(parent, title, message, GlassDialog.Kind.INFO);
    }

    public static void success(Component parent, String title, String message) {
        GlassDialog.message(parent, title, message, GlassDialog.Kind.SUCCESS);
    }

    public static void warn(Component parent, String title, String message) {
        GlassDialog.message(parent, title, message, GlassDialog.Kind.WARNING);
    }

    public static boolean confirm(Component parent, String title, String message) {
        return GlassDialog.confirm(parent, title, message);
    }

    /** Shows a long, selectable block of text such as a generated invitation code. */
    public static void code(Component parent, String title, String message, String code) {
        GlassDialog.code(parent, title, message, code);
    }

    /** Returns the window that owns the component, used to centre modal dialogs. */
    public static Window owner(Component component) {
        return component == null ? null : javax.swing.SwingUtilities.getWindowAncestor(component);
    }
}
