package id.edu.homeschool.ui.design;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Design tokens for the whole application. Every colour, font and spacing value used by the
 * interface comes from here so the portal, the auth screen and every page stay visually consistent.
 *
 * <p>The palette follows a "liquid glass" direction: a soft ice-blue to lavender canvas, layered
 * translucent white surfaces, hairline luminous edges and a restrained indigo accent. Text colours
 * stay fully opaque so readability never depends on the translucency of the surface below.</p>
 */
public final class Theme {

    /* ---------- canvas ---------- */

    /** Top colour of the application background gradient (Main background: #F5F8FF). */
    public static final Color CANVAS_TOP = new Color(0xF5, 0xF8, 0xFF);
    /** Bottom colour of the application background gradient (Secondary background: #EEF2FF). */
    public static final Color CANVAS_BOTTOM = new Color(0xEE, 0xF2, 0xFF);
    /**
     * Fully transparent canvas colour. Panels painted with it keep the gradient behind them
     * visible, which is what gives the layered glass composition its depth.
     */
    public static final Color CANVAS = new Color(0, 0, 0, 0);

    /* ---------- glass surfaces ---------- */

    /** Standard frosted surface used for cards, sidebars and bars. */
    public static final Color GLASS = new Color(255, 255, 255, 215);
    /** Slightly denser frosted surface used for the large entry surfaces. */
    public static final Color GLASS_CARD = new Color(255, 255, 255, 235);
    /** Denser frosted surface used where content readability matters most. */
    public static final Color GLASS_SOLID = new Color(255, 255, 255, 248);
    /** Table body background; almost opaque so text never fights the gradient. */
    public static final Color GLASS_TABLE = new Color(255, 255, 255, 245);
    /** Alternating table row tint. */
    public static final Color GLASS_ROW_ALT = new Color(0xF8, 0xFA, 0xFC, 245);
    /** Luminous inner specular edge of a glass surface. */
    public static final Color GLASS_EDGE = new Color(255, 255, 255, 235);
    /** Outer hairline that separates a glass surface from the canvas (#DCE6F7). */
    public static final Color GLASS_OUTLINE = new Color(0xDC, 0xE6, 0xF7, 210);
    /** Soft ambient shadow cast by floating surfaces. */
    public static final Color SHADOW = new Color(30, 41, 59, 28);
    /** Ice-blue tint used for the soft background bloom (#3B82F6). */
    public static final Color ICE = new Color(0x3B, 0x82, 0xF6, 80);
    /** Lavender tint used for the soft background bloom (#A78BFA). */
    public static final Color LAVENDER = new Color(0xA7, 0x8B, 0xFA, 80);

    /* ---------- readable solid surfaces ---------- */

    /** Near opaque white used behind dense content such as tables and text areas. */
    public static final Color SURFACE = new Color(255, 255, 255, 250);
    /** Sidebar placeholder; the sidebar paints its own glass surface. */
    public static final Color SIDEBAR = new Color(255, 255, 255, 0);

    /* ---------- lines ---------- */

    /** Subtle border matching design system specification (#DCE6F7). */
    public static final Color BORDER = new Color(0xDC, 0xE6, 0xF7);
    public static final Color BORDER_STRONG = new Color(0xC7, 0xD2, 0xFE);

    /* ---------- text ---------- */

    /** Primary text: Deep navy-blue (#172554) for maximum readability and elegance. */
    public static final Color TEXT = new Color(0x17, 0x25, 0x54);
    /** Secondary text: Slate-500 (#64748B) for subtitles, labels, and hints. */
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);
    /** Subtle text: Slate-400 (#94A3B8) for placeholders and inactive captions. */
    public static final Color TEXT_SUBTLE = new Color(0x94, 0xA3, 0xB8);
    public static final Color TEXT_ON_ACCENT = Color.WHITE;

    /* ---------- accent ---------- */

    /** Primary blue (#3B82F6) representing modern Liquid Glass primary tone. */
    public static final Color ACCENT = new Color(0x3B, 0x82, 0xF6);
    /** Deep indigo (#4338CA) paired with primary blue in restrained gradients. */
    public static final Color ACCENT_DEEP = new Color(0x43, 0x38, 0xCA);
    /** Lavender accent (#A78BFA) for luminous highlights and blooms. */
    public static final Color ACCENT_LAVENDER = new Color(0xA7, 0x8B, 0xFA);
    public static final Color ACCENT_HOVER = new Color(0x25, 0x63, 0xEB);
    public static final Color ACCENT_PRESSED = new Color(0x1D, 0x4E, 0xD8);
    public static final Color ACCENT_SOFT = new Color(0xEE, 0xF2, 0xFF);
    public static final Color ACCENT_BORDER = new Color(0xC7, 0xD2, 0xFE);

    /* ---------- status ---------- */

    public static final Color SUCCESS = new Color(0x05, 0x96, 0x69);
    public static final Color SUCCESS_SOFT = new Color(0xEC, 0xFD, 0xF5);
    public static final Color SUCCESS_BORDER = new Color(0xA7, 0xF3, 0xD0);

    public static final Color WARNING = new Color(0xD9, 0x77, 0x06);
    public static final Color WARNING_SOFT = new Color(0xFF, 0xFB, 0xEB);
    public static final Color WARNING_BORDER = new Color(0xFD, 0xE6, 0x8A);

    public static final Color DANGER = new Color(0xDC, 0x26, 0x26);
    public static final Color DANGER_SOFT = new Color(0xFE, 0xF2, 0xF2);
    public static final Color DANGER_BORDER = new Color(0xFE, 0xCA, 0xCA);

    public static final Color INFO = new Color(0x25, 0x63, 0xEB);
    public static final Color INFO_SOFT = new Color(0xEF, 0xF6, 0xFF);
    public static final Color INFO_BORDER = new Color(0xBF, 0xDB, 0xFE);

    /** Neutral status tone used for archived or already consumed records. */
    public static final Color NEUTRAL = new Color(0x64, 0x74, 0x8B);
    /** Soft background of the neutral pill. */
    public static final Color NEUTRAL_SOFT = new Color(0xF1, 0xF5, 0xF9);
    public static final Color NEUTRAL_BORDER = new Color(0xE2, 0xE8, 0xF0);

    /* ---------- accents used by the summary icons ---------- */

    /** Indigo tint of the icon chips, matching the primary accent. */
    public static final Color TINT_INDIGO = new Color(0x43, 0x38, 0xCA);
    /** Sky tint used for the schedule and calendar chips. */
    public static final Color TINT_SKY = new Color(0x02, 0x84, 0xC7);
    /** Teal tint used for the material and library chips. */
    public static final Color TINT_TEAL = new Color(0x0D, 0x94, 0x88);
    /** Violet tint used for the goal and target chips. */
    public static final Color TINT_VIOLET = new Color(0x7C, 0x3A, 0xED);
    /** Amber tint used for the task and assignment chips. */
    public static final Color TINT_AMBER = new Color(0xD9, 0x77, 0x06);
    /** Rose tint used for the score and evaluation chips. */
    public static final Color TINT_ROSE = new Color(0xE1, 0x1D, 0x48);

    /** Colour of the focus ring that marks the keyboard focus of a control. */
    public static final Color FOCUS = new Color(0x3B, 0x82, 0xF6);

    /* ---------- typography ---------- */

    private static final String FAMILY = resolveFamily("Inter", "Segoe UI", "Tahoma", "SansSerif");
    public static final Font FONT_10 = new Font(FAMILY, Font.PLAIN, 10);
    public static final Font FONT_10_BOLD = new Font(FAMILY, Font.BOLD, 10);
    public static final Font FONT_11 = new Font(FAMILY, Font.PLAIN, 11);
    public static final Font FONT_11_BOLD = new Font(FAMILY, Font.BOLD, 11);
    public static final Font FONT_12 = new Font(FAMILY, Font.PLAIN, 12);
    public static final Font FONT_12_BOLD = new Font(FAMILY, Font.BOLD, 12);
    public static final Font FONT_13 = new Font(FAMILY, Font.PLAIN, 13);
    public static final Font FONT_13_BOLD = new Font(FAMILY, Font.BOLD, 13);
    public static final Font FONT_14 = new Font(FAMILY, Font.PLAIN, 14);
    public static final Font FONT_14_SEMIBOLD = new Font(FAMILY, Font.BOLD, 14);
    public static final Font FONT_16 = new Font(FAMILY, Font.PLAIN, 16);
    public static final Font FONT_16_BOLD = new Font(FAMILY, Font.BOLD, 16);
    public static final Font FONT_20 = new Font(FAMILY, Font.BOLD, 20);
    public static final Font FONT_24 = new Font(FAMILY, Font.BOLD, 24);
    public static final Font FONT_28 = new Font(FAMILY, Font.BOLD, 28);

    /* ---------- spacing scale ---------- */

    public static final int SPACE_1 = 4;
    public static final int SPACE_2 = 8;
    public static final int SPACE_3 = 12;
    public static final int SPACE_4 = 16;
    public static final int SPACE_5 = 20;
    public static final int SPACE_6 = 24;
    public static final int SPACE_7 = 32;
    public static final int SPACE_8 = 40;

    /* ---------- shape ---------- */

    /** Corner radius of the standard glass surface. */
    public static final int RADIUS = 16;
    /** Corner radius of compact surfaces such as inputs and pills. */
    public static final int RADIUS_SM = 10;
    /** Corner radius of large floating surfaces such as the sidebar. */
    public static final int RADIUS_LG = 22;
    public static final int CONTROL_HEIGHT = 38;
    public static final int SIDEBAR_WIDTH = 236;

    private Theme() { }

    private static String resolveFamily(String... candidates) {
        try {
            Set<String> available = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            for (String candidate : candidates) {
                if (available.contains(candidate)) return candidate;
            }
        } catch (RuntimeException ignored) {
            // Headless or restricted environments fall back to the logical font below.
        }
        return Font.SANS_SERIF;
    }

    /** Returns a copy of the colour with a different alpha channel. */
    public static Color alpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /** Mixes the colour towards white. */
    public static Color lighten(Color color, float amount) {
        return mix(color, Color.WHITE, amount);
    }

    /** Mixes the colour towards black. */
    public static Color darken(Color color, float amount) {
        return mix(color, new Color(0, 0, 0), amount);
    }

    private static Color mix(Color base, Color target, float amount) {
        float clamped = Math.max(0f, Math.min(1f, amount));
        int red = Math.round(base.getRed() + (target.getRed() - base.getRed()) * clamped);
        int green = Math.round(base.getGreen() + (target.getGreen() - base.getGreen()) * clamped);
        int blue = Math.round(base.getBlue() + (target.getBlue() - base.getBlue()) * clamped);
        int alpha = Math.round(base.getAlpha() + (target.getAlpha() - base.getAlpha()) * clamped);
        return new Color(red, green, blue, alpha);
    }

    /** Resolves a status keyword to a readable label plus its soft accent colours. */
    public static StatusStyle statusOf(String status) {
        if (status == null) return neutral();
        String value = status.toUpperCase(Locale.ROOT);
        return switch (value) {
            case "ACTIVE", "APPROVED", "TERCAI", "SELESAI", "TERCAPAI" ->
                    new StatusStyle(SUCCESS, SUCCESS_SOFT, SUCCESS_BORDER);
            case "PENDING", "BELUM SELESAI", "DALAM PROSES", "MENUNGGU" ->
                    new StatusStyle(WARNING, WARNING_SOFT, WARNING_BORDER);
            case "REJECTED", "TERLAMBAT", "TERBATAS", "EXPIRED", "DITOLAK", "KEDALUWARSA" ->
                    new StatusStyle(DANGER, DANGER_SOFT, DANGER_BORDER);
            case "USED", "REVOKED", "ENDED", "TERDAKIR", "SUDAH DIPAKAI", "DICABUT" -> neutral();
            default -> new StatusStyle(INFO, INFO_SOFT, INFO_BORDER);
        };
    }

    /** The neutral tone used for records that are no longer active. */
    public static StatusStyle neutral() {
        return new StatusStyle(NEUTRAL, NEUTRAL_SOFT, NEUTRAL_BORDER);
    }

    public record StatusStyle(Color foreground, Color background, Color border) {
        public StatusStyle(Color foreground, Color background) {
            this(foreground, background, Theme.alpha(foreground, 50));
        }
    }
}
