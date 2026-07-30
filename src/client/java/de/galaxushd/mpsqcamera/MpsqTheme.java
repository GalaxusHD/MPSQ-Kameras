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
    public static final int ROT          = 0xFFB30000;
    /** Dunkelrot – äußerer Rahmen */
    public static final int DUNKELROT    = 0xFF8B0000;
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
        // Dunkelroter Außenrahmen
        context.drawBorder(0, 0, width, height, DUNKELROT);
        context.drawBorder(1, 1, width - 2, height - 2, 0x66350000);
        // Obere rote Akzentlinie
        context.fill(3, 3, width - 3, 7, ROT);
        // Untere weinrote Akzentlinie
        context.fill(3, height - 7, width - 3, height - 3, WEINROT);
        // Schmale seitliche Akzentlinien
        context.fill(3, 7, 5, height - 7, 0x88AA1111);
        context.fill(width - 5, 7, width - 3, height - 7, 0x88AA1111);
    }

    /**
     * Zeichnet ein halbtransparentes Panel mit weinrotem Rahmen.
     */
    public static void drawPanel(DrawContext context, int x, int y, int w, int h) {
        context.fill(x, y, x + w, y + h, PANEL_BG);
        context.drawBorder(x, y, w, h, RAHMEN);
    }
}
