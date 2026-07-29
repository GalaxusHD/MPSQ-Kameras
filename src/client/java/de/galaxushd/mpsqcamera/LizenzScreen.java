package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Zeigt die Mod-Lizenzinformationen an (nicht editierbarer Platzhaltertext).
 *
 * TODO: Tatsächlichen Lizenztext einfügen, wenn vom Mod-Autor bereitgestellt.
 */
public class LizenzScreen extends Screen {

    private final Screen parent;

    // TODO: Diesen Platzhaltertext durch den vollständigen Lizenztext ersetzen.
    private static final String[] LIZENZ_ZEILEN = {
        "MPSQ Kameras – Lizenzinformationen",
        "",
        "Lizenz: LGPL-2.1-or-later",
        "",
        "[TODO: Vollständiger Lizenztext wird hier eingefügt]",
        "",
        "Entwickelt von: GalaxusHD",
        "",
        "Quellcode & weitere Infos:",
        "github.com/GalaxusHD/MPSQ-Kameras",
        "",
        "Alle Rechte vorbehalten.",
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
        ).dimensions(this.width / 2 - 75, this.height - 36, 150, 20).build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, this.width, this.height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int cx     = this.width / 2;
        int startY = 28;

        // Titel
        context.drawCenteredTextWithShadow(
                this.textRenderer, this.title, cx, startY, MpsqTheme.TEXT_TITEL);
        startY += 16;

        // Trennlinie unter Titel
        context.fill(cx - 130, startY, cx + 130, startY + 1, MpsqTheme.WEINROT);
        startY += 10;

        // Lizenzzeilen (nicht editierbar – nur Anzeige)
        for (String zeile : LIZENZ_ZEILEN) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer, Text.literal(zeile),
                    cx, startY, MpsqTheme.TEXT_NORMAL);
            startY += 11;
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
