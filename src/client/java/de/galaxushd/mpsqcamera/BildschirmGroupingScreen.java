package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

/**
 * Menü zum Verwalten von Bildschirm-Gruppen.
 * - Erstelle neue Gruppen
 * - Ordne Bildschirme zu/ab
 * - Auto-Delete von Gruppen bei nur 1 Screen
 */
public class BildschirmGroupingScreen extends Screen {

    private final Screen parent;
    private List<GroupEntry> groups = new ArrayList<>();
    private int scrollOffset = 0;

    public BildschirmGroupingScreen(Screen parent) {
        super(Text.literal("Bildschirme Gruppieren"));
        this.parent = parent;
        // TODO: Gruppen aus Daten laden
    }

    @Override
    protected void init() {
        // Neue Gruppe erstellen
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Neue Gruppe"),
                b -> createNewGroup()
        ).dimensions(this.width / 2 - 70, 40, 140, 20).build());

        // Zurück-Button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Zurück"),
                b -> this.client.setScreen(parent)
        ).dimensions(this.width / 2 - 75, this.height - 36, 150, 20).build());
    }

    private void createNewGroup() {
        MpsqCameraClient.LOGGER.info("[MPSQ] Neue Gruppe erstellt.");
        // TODO: Neue Gruppe erstellen & Editor öffnen
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

        context.drawCenteredTextWithShadow(
                this.textRenderer, this.title, cx, startY, MpsqTheme.TEXT_TITEL);
        startY += 16;

        context.fill(cx - 130, startY, cx + 130, startY + 1, MpsqTheme.WEINROT);
        startY += 20;

        // Gruppen-Liste
        int lineY = startY - scrollOffset;
        for (GroupEntry group : groups) {
            if (lineY > startY && lineY < this.height - 50) {
                context.drawCenteredTextWithShadow(
                        this.textRenderer,
                        Text.literal(group.getName() + " (" + group.getScreenCount() + " Screens)"),
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
    public static class GroupEntry {
        private String id;
        private String name;
        private int screenCount;

        public GroupEntry(String id, String name, int screenCount) {
            this.id = id;
            this.name = name;
            this.screenCount = screenCount;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public int getScreenCount() { return screenCount; }
    }
}
