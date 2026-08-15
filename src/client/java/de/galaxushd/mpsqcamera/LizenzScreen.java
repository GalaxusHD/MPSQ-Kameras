package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Zeigt die Mod-Lizenzinformationen an (nicht editierbarer Platzhaltertext).
 */
public class LizenzScreen extends Screen {

    private final Screen parent;
    private int scrollOffset = 0;

    // Vollständiger Lizenztext
    private static final String[] LIZENZ_ZEILEN = {
        "MPSQ Team – Nutzungsbedingungen",
        "",
        "Version: 1.0",
        "",
        "Diese Modifikation (\"MPSQ Team\") wurde ausschließlich für das",
        "MixelPixel Squid Game Team entwickelt.",
        "",
        "1. Nutzung",
        "Die Mod darf ausschließlich innerhalb des MixelPixel Squid Game Teams",
        "verwendet werden. Eine Nutzung außerhalb dieses vorgesehenen Zwecks ist",
        "ohne ausdrückliche Genehmigung nicht gestattet.",
        "",
        "2. Missbrauch",
        "Jegliche Form des Missbrauchs oder der Ausnutzung dieser Mod ist untersagt.",
        "",
        "3. Verstöße gegen MixelPixel-Regeln",
        "Sollte diese Mod zur Verletzung der MixelPixel-Regeln verwendet werden,",
        "können neben den üblichen Konsequenzen auch Maßnahmen seitens des",
        "MixelPixel-Teams gegen den jeweiligen Nutzer ergriffen werden.",
        "",
        "4. Weiterverwendung",
        "Eine Weitergabe, Veränderung, Veröffentlichung oder Wiederverwendung",
        "dieser Mod – ganz oder teilweise – ist ausschließlich nach vorheriger",
        "Absprache und ausdrücklicher Genehmigung von \"Galaxus_HD\" gestattet.",
        "",
        "5. Credits",
        "Die Grundidee der Bildschirme stammt von \"chaotischer\".",
        "Vielen Dank für die Inspiration.",
        "",
        "6. Schlussbestimmung",
        "Mit der Nutzung dieser Mod erkennen alle Nutzer diese",
        "Nutzungsbedingungen an.",
        "",
        "© MPSQ Team",
    };

    public LizenzScreen(Screen parent) {
        super(Text.translatable("gui.mpsqcamera.lizenz.titel"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Zurück-Button unten mittig
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.mpsqcamera.lizenz.zurueck"),
                b -> this.client.setScreen(parent)
        ).dimensions(this.width / 2 - 75, this.height - 28, 150, 20).build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, this.width, this.height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx     = this.width / 2;
        int startY = 28;

        // Titel
        context.drawCenteredTextWithShadow(
                this.textRenderer, this.title, cx, startY, MpsqTheme.TEXT_TITEL);
        startY += 16;

        // Trennlinie unter Titel
        context.fill(cx - 130, startY, cx + 130, startY + 1, 0x44FFFFFF);
        startY += 10;

        // Lizenzzeilen (nicht editierbar – nur Anzeige, scrollbar)
        int lineY = startY - scrollOffset;
        int maxDisplayY = this.height - 50;

        for (String zeile : LIZENZ_ZEILEN) {
            if (lineY > startY - 5 && lineY < maxDisplayY) {
                context.drawCenteredTextWithShadow(
                        this.textRenderer, Text.literal(zeile),
                        cx, lineY, MpsqTheme.TEXT_NORMAL);
            }
            lineY += 11;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (int)(verticalAmount * 11); // Eine Zeile = 11px
        int maxScroll = Math.max(0, (LIZENZ_ZEILEN.length * 11) - (this.height - 70));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }
}
