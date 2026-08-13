package de.galaxushd.mpsqcamera;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Scrollable camera list. Rows deliberately use the same MPSQ rendering as screen rows. */
public final class CameraListScreen extends Screen {
    private static final int LIST_TOP = 82;
    private static final int LIST_BOTTOM_MARGIN = 62;
    private static final int ROW_HEIGHT = 22;
    private static final int ROW_SPACING = 3;
    private final Screen parent;
    private final Set<UUID> selected = new LinkedHashSet<>();
    private int scrollOffset;

    public CameraListScreen(Screen parent) {
        super(Text.translatable("gui.mpsqcamera.cameras.title"));
        this.parent = parent;
    }

    @Override protected void init() {
        // A bodycam may have been accepted while this client was not in the
        // menu. Refresh once whenever the list opens so it appears immediately.
        MpsqApiClient.refreshCameras();
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), b -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 36, 150, 20).build());
        clampScroll();
    }

    @Override public void renderBackground(DrawContext c, int x, int y, float d) { super.renderBackground(c, x, y, d); MpsqTheme.drawBackground(c, width, height); }

    @Override public void render(DrawContext c, int mouseX, int mouseY, float d) {
        super.render(c, mouseX, mouseY, d);
        int cx = width / 2;
        c.drawCenteredTextWithShadow(textRenderer, title, cx, 28, MpsqTheme.TEXT_TITEL);
        c.fill(cx - 140, 44, cx + 140, 45, 0x44FFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Klick: verwalten   |   Rechtsklick: auswählen"), cx, 58, MpsqTheme.TEXT_GEDAEMPT);
        if (!selected.isEmpty()) {
            c.fill(cx - 140, 66, cx + 140, 78, 0x55000000);
            c.drawCenteredTextWithShadow(textRenderer, Text.literal(selected.size() + " ausgewählt – Entf zum Löschen"), cx, 68, MpsqTheme.TEXT_GEDAEMPT);
        }
        List<LocalCameraStore.CameraData> cameras = LocalCameraStore.getAll();
        if (cameras.isEmpty()) { c.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.cameras.empty"), cx, LIST_TOP + 20, MpsqTheme.TEXT_GEDAEMPT); return; }
        int bottom = height - LIST_BOTTOM_MARGIN;
        int y = LIST_TOP - scrollOffset;
        for (LocalCameraStore.CameraData camera : cameras) {
            if (y + ROW_HEIGHT > LIST_TOP && y < bottom) {
                boolean marked = selected.contains(camera.id());
                c.fill(cx - 140, y, cx + 140, y + ROW_HEIGHT, marked ? 0x66557A9B : 0x55000000);
                c.drawTextWithShadow(textRenderer, Text.literal(marked ? "[x]" : "[ ]"), cx - 134, y + 7, MpsqTheme.TEXT_NORMAL);
                Text type = Text.translatable(camera.kind() == LocalCameraStore.CameraKind.BODYCAM ? "gui.mpsqcamera.cameras.bodycam" : "gui.mpsqcamera.cameras.static");
                c.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.cameras.entry", camera.name(), type), cx + 12, y + 7, MpsqTheme.TEXT_NORMAL);
            }
            y += ROW_HEIGHT + ROW_SPACING;
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int cx = width / 2, y = LIST_TOP - scrollOffset, bottom = height - LIST_BOTTOM_MARGIN;
        for (LocalCameraStore.CameraData camera : LocalCameraStore.getAll()) {
            if (y >= LIST_TOP && y < bottom && mouseX >= cx - 140 && mouseX <= cx + 140 && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                if (button == 1 || mouseX < cx - 108) toggle(camera.id());
                else client.setScreen(new CameraDetailScreen(this, camera.id()));
                return true;
            }
            y += ROW_HEIGHT + ROW_SPACING;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if ((key == 261 || key == 259) && !selected.isEmpty()) { confirmDeleteSelected(); return true; }
        return super.keyPressed(key, scanCode, modifiers);
    }
    private void toggle(UUID id) { if (!selected.add(id)) selected.remove(id); }
    private void confirmDeleteSelected() {
        client.setScreen(new ConfirmScreen(ok -> { if (!ok) { client.setScreen(this); return; } deleteNext(List.copyOf(selected), 0); }, Text.literal("Kameras löschen"), Text.literal(selected.size() + " Kamera(s) wirklich löschen?"), Text.literal("Löschen"), Text.literal("Abbrechen")));
    }
    private void deleteNext(List<UUID> ids, int index) {
        if (index >= ids.size()) { MpsqApiClient.refreshCameras().whenComplete((v,e) -> client.execute(() -> { selected.clear(); client.setScreen(this); })); return; }
        MpsqApiClient.delete("/cameras/" + ids.get(index)).whenComplete((v,e) -> deleteNext(ids, index + 1));
    }
    @Override public boolean mouseScrolled(double x, double y, double h, double v) { scrollOffset -= (int) Math.signum(v) * (ROW_HEIGHT + ROW_SPACING) * 2; clampScroll(); return true; }
    private int maxScroll() { return Math.max(0, LocalCameraStore.getAll().size() * (ROW_HEIGHT + ROW_SPACING) - (height - LIST_TOP - LIST_BOTTOM_MARGIN)); }
    private void clampScroll() { scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll())); }
    @Override public boolean shouldPause() { return false; }
}
