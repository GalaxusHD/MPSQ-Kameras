package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Screen zum Erstellen einer neuen Gruppe.
 * Zeigt alle vorhandenen Bildschirme zur Auswahl an.
 * Ausgewählte Bildschirme erhalten einen gemeinsamen Code.
 */
public class GroupCreationScreen extends Screen {

    private final Screen parent;
    private final List<LocalScreenStore.LocalScreenData> allScreens;
    private final List<UUID> selectedScreenIds = new ArrayList<>();
    private int scrollOffset = 0;

    private static final int ROW_HEIGHT = 20;
    private static final int LIST_TOP_OFFSET = 60;
    private static final int LIST_BOTTOM_MARGIN = 60;

    public GroupCreationScreen(Screen parent) {
        super(Text.literal("Neue Gruppe erstellen"));
        this.parent = parent;
        this.allScreens = LocalScreenStore.getAllScreens();
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int bottomY = this.height - 36;

        // Gruppe erstellen
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Gruppe erstellen"),
                b -> createGroup()
        ).dimensions(cx - 90, bottomY, 86, 20).build());

        // Abbrechen
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Abbrechen"),
                b -> this.client.setScreen(parent)
        ).dimensions(cx + 4, bottomY, 86, 20).build());
    }

    private void createGroup() {
        if (selectedScreenIds.size() < 2) {
            MpsqCameraClient.LOGGER.info("[MPSQ] Mindestens 2 Bildschirme für eine Gruppe auswählen.");
            return;
        }
        LocalScreenStore.LocalGroupData group = LocalScreenStore.createGroup(selectedScreenIds);
        MpsqCameraClient.LOGGER.info("[MPSQ] Gruppe erstellt mit Code: " + group.sharedCode()
                + " (" + selectedScreenIds.size() + " Bildschirme)");
        this.client.setScreen(parent);
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
        int y = 20;

        // Titel
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, y, MpsqTheme.TEXT_TITEL);
        y += 14;

        // Trennlinie
        context.fill(cx - 130, y, cx + 130, y + 1, 0x44FFFFFF);
        y += 6;

        // Hinweis: Mindestens 2
        String hint = selectedScreenIds.size() < 2
                ? "Wähle mindestens 2 Bildschirme aus (" + selectedScreenIds.size() + " ausgewählt)"
                : selectedScreenIds.size() + " Bildschirme ausgewählt";
        int hintColor = selectedScreenIds.size() < 2 ? MpsqTheme.TEXT_GEDAEMPT : MpsqTheme.TEXT_TITEL;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(hint), cx, y, hintColor);

        if (allScreens.isEmpty()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Keine Bildschirme vorhanden."),
                    cx, LIST_TOP_OFFSET + 20, MpsqTheme.TEXT_GEDAEMPT);
            return;
        }

        // Bildschirm-Liste mit Auswahl
        int listBottom = this.height - LIST_BOTTOM_MARGIN;
        int listY = LIST_TOP_OFFSET - scrollOffset;

        for (LocalScreenStore.LocalScreenData screen : allScreens) {
            if (listY + ROW_HEIGHT < LIST_TOP_OFFSET || listY > listBottom) {
                listY += ROW_HEIGHT + 2;
                continue;
            }

            boolean selected = selectedScreenIds.contains(screen.id());

            // Zeilen-Hintergrund: ausgewählt = leicht rot, sonst dunkel
            int bgColor = selected ? 0x55AA0000 : 0x33000000;
            context.fill(cx - 130, listY - 1, cx + 130, listY + ROW_HEIGHT - 1, bgColor);

            // Rahmen wenn ausgewählt
            if (selected) {
                context.fill(cx - 130, listY - 1, cx - 129, listY + ROW_HEIGHT - 1, MpsqTheme.ROT);
                context.fill(cx + 129, listY - 1, cx + 130, listY + ROW_HEIGHT - 1, MpsqTheme.ROT);
            }

            // Checkbox-Symbol
            String checkbox = selected ? "[✓] " : "[ ] ";
            context.drawTextWithShadow(
                    this.textRenderer, Text.literal(checkbox + screen.name()),
                    cx - 125, listY + 4,
                    selected ? MpsqTheme.TEXT_TITEL : MpsqTheme.TEXT_NORMAL);

            // Gruppen-Status
            if (screen.groupId() != null) {
                context.drawTextWithShadow(
                        this.textRenderer, Text.literal("(Gruppe)"),
                        cx + 70, listY + 4, MpsqTheme.TEXT_GEDAEMPT);
            }

            listY += ROW_HEIGHT + 2;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int cx = this.width / 2;
            int listBottom = this.height - LIST_BOTTOM_MARGIN;
            int listY = LIST_TOP_OFFSET - scrollOffset;

            for (LocalScreenStore.LocalScreenData screen : allScreens) {
                boolean rowVisible = (listY + ROW_HEIGHT >= LIST_TOP_OFFSET) && (listY <= listBottom);
                if (rowVisible
                        && mouseY >= listY - 1 && mouseY < listY + ROW_HEIGHT - 1
                        && mouseX >= cx - 130 && mouseX <= cx + 130) {
                    toggleSelection(screen.id());
                    return true;
                }
                listY += ROW_HEIGHT + 2;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void toggleSelection(UUID screenId) {
        if (selectedScreenIds.contains(screenId)) {
            selectedScreenIds.remove(screenId);
        } else {
            selectedScreenIds.add(screenId);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (int)(verticalAmount * 16);
        int maxScroll = Math.max(0, allScreens.size() * (ROW_HEIGHT + 2) - (this.height - LIST_TOP_OFFSET - LIST_BOTTOM_MARGIN));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }
}
