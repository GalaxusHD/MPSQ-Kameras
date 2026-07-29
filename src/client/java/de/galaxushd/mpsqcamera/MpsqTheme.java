package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;

/**
 * Zentrale Theme-Konstanten und Hilfsrenderer für alle Mod-Screens.
 *
 * Farbpalette: Rot / Weinrot / Dunkelgrau / Schwarz
 */
public final class MpsqTheme {

    private MpsqTheme() {}

    // ── Farb-Palette ─────────────────────────────────────────────────────────

    /** Primärrot – Akzentfarbe, Rahmen oben */
    public static final int ROT          = 0xFFCC0000;
    /** Weinrot – Sekundär-Akzent, Rahmen unten / Panel-Rand */
    public static final int WEINROT      = 0xFF7A0020;
    /** Dunkelgrau – Hintergrund oben */
    public static final int DUNKELGRAU   = 0xFF1A1A1A;
    /** Fast Schwarz – Hintergrund unten */
    public static final int SCHWARZ      = 0xFF050505;
    /** Halbtransparentes dunkles Weinrot für Panels */
    public static final int PANEL_BG     = 0x99150505;
    /** Panel-Rahmen (Weinrot) */
    public static final int RAHMEN       = 0xFF7A0020;
    /** Titel-Text – helles Rot */
    public static final int TEXT_TITEL   = 0xFFFF4444;
    /** Normaler Text – fast Weiß */
    public static final int TEXT_NORMAL  = 0xFFEEEEEE;
    /** Gedämpfter Text – Grau */
    public static final int TEXT_GEDAEMPT = 0xFF888888;

    // ── Hilfsmethoden ────────────────────────────────────────────────────────

    /**
     * Zeichnet den benutzerdefinierten Hintergrund-Gradient.
     * Muss nach {@code super.renderBackground(...)} aufgerufen werden.
     */
    public static void drawBackground(DrawContext context, int width, int height) {
        // Vertikaler Gradient: dunkles Weinrot-Grau → fast Schwarz
        context.fillGradient(0, 0, width, height, 0xCC1A0808, 0xCC050000);
        // Obere rote Akzentlinie (3 px)
        context.fill(0, 0, width, 3, ROT);
        // Untere weinrote Akzentlinie (3 px)
        context.fill(0, height - 3, width, height, WEINROT);
        // Schmale seitliche Akzentlinien (2 px)
        context.fill(0, 3, 2, height - 3, 0x88AA1111);
        context.fill(width - 2, 3, width, height - 3, 0x88AA1111);
    }

    /**
     * Zeichnet ein halbtransparentes Panel mit weinrotem Rahmen.
     */
    public static void drawPanel(DrawContext context, int x, int y, int w, int h) {
        context.fill(x, y, x + w, y + h, PANEL_BG);
        context.drawBorder(x, y, w, h, RAHMEN);
    }
}
