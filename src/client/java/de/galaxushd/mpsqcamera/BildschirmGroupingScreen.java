package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.List;

/**
 * Übersicht aller Bildschirm-Gruppen.
 * - Erstelle neue Gruppen über den GroupCreationScreen
 * - Zeigt bestehende Gruppen mit Code und Anzahl Bildschirme
 */
public class BildschirmGroupingScreen extends Screen {

    private final Screen parent;
    private int scrollOffset = 0;

    public BildschirmGroupingScreen(Screen parent) {
        super(Text.literal("Bildschirme Gruppieren"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Neue Gruppe erstellen → öffnet GroupCreationScreen
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Neue Gruppe"),
                b -> this.client.setScreen(new GroupCreationScreen(this))
        ).dimensions(this.width / 2 - 70, 40, 140, 20).build());

        // Zurück-Button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Zurück"),
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

        int cx = this.width / 2;
        int startY = 28;

        context.drawCenteredTextWithShadow(
                this.textRenderer, this.title, cx, startY, MpsqTheme.TEXT_TITEL);
        startY += 16;

        context.fill(cx - 130, startY, cx + 130, startY + 1, 0x44FFFFFF);
        startY += 16;

        List<LocalScreenStore.LocalGroupData> groups = LocalScreenStore.getAllGroups();

        if (groups.isEmpty()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Keine Gruppen vorhanden."),
                    cx, startY + 10, MpsqTheme.TEXT_GEDAEMPT);
            return;
        }

        // Gruppen-Liste
        int lineY = startY - scrollOffset;
        int listBottom = this.height - 50;

        for (LocalScreenStore.LocalGroupData group : groups) {
            if (lineY < startY || lineY > listBottom) {
                lineY += 18;
                continue;
            }
            List<LocalScreenStore.LocalScreenData> screens = LocalScreenStore.getScreensInGroup(group.id());
            String label = "Code: " + group.sharedCode() + "  (" + screens.size() + " Bildschirme)";
            context.drawCenteredTextWithShadow(
                    this.textRenderer, Text.literal(label), cx, lineY, MpsqTheme.TEXT_NORMAL);
            lineY += 18;
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
}
