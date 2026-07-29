package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

/**
 * Zeigt die Liste aller verfügbaren Bildschirme an.
 * (eigene + eingeladene)
 */
public class BildschirmListScreen extends Screen {

    private final Screen parent;
    private List<BildschirmEntry> bildschirme = new ArrayList<>();
    private int scrollOffset = 0;

    public BildschirmListScreen(Screen parent) {
        super(Text.literal("Bildschirme"));
        this.parent = parent;
        // TODO: Bildschirme aus Daten laden
    }

    @Override
    protected void init() {
        // Zurück-Button unten
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Zurück"),
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

        int cx = this.width / 2;
        int startY = 28;

        // Titel
        context.drawCenteredTextWithShadow(
                this.textRenderer, this.title, cx, startY, MpsqTheme.TEXT_TITEL);
        startY += 16;

        // Trennlinie
        context.fill(cx - 130, startY, cx + 130, startY + 1, MpsqTheme.WEINROT);
        startY += 10;

        // Bildschirm-Liste
        int lineY = startY - scrollOffset;
        for (BildschirmEntry bs : bildschirme) {
            if (lineY > startY && lineY < this.height - 50) {
                context.drawCenteredTextWithShadow(
                        this.textRenderer, Text.literal(bs.getName()),
                        cx, lineY, MpsqTheme.TEXT_NORMAL);
            }
            lineY += 16;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (int)(verticalAmount * 16);
        scrollOffset = Math.max(0, scrollOffset);
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }

    // ── Hilfklasse ──────────────────────────────────────────────────────────
    public static class BildschirmEntry {
        private String id;
        private String name;
        private boolean isCreator;

        public BildschirmEntry(String id, String name, boolean isCreator) {
            this.id = id;
            this.name = name;
            this.isCreator = isCreator;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public boolean isCreator() { return isCreator; }
    }
}
