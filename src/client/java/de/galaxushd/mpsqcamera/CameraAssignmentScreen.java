package de.galaxushd.mpsqcamera;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

/** Adds a camera to a screen and only closes after the API has confirmed and reloaded the assignment. */
public final class CameraAssignmentScreen extends Screen {
    private static final int TOP = 58, BOTTOM = 62, ROW = 22;
    private final Screen parent;
    private final UUID screenId;
    private int scroll;
    private String status = "";
    public CameraAssignmentScreen(Screen parent, UUID screenId) { super(Text.literal("Kamera auswählen")); this.parent = parent; this.screenId = screenId; }
    @Override protected void init() { addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), b -> client.setScreen(parent)).dimensions(width / 2 - 75, height - 36, 150, 20).build()); clamp(); }
    @Override public void renderBackground(DrawContext c, int x, int y, float d) { super.renderBackground(c, x, y, d); MpsqTheme.drawBackground(c, width, height); }
    @Override public void render(DrawContext c, int x, int y, float d) {
        super.render(c, x, y, d); int cx = width / 2, row = TOP - scroll;
        c.drawCenteredTextWithShadow(textRenderer, title, cx, 28, MpsqTheme.TEXT_TITEL); c.fill(cx - 140, 44, cx + 140, 45, 0x44FFFFFF);
        if (!status.isBlank()) c.drawCenteredTextWithShadow(textRenderer, Text.literal(status), cx, 47, 0xFF5555);
        List<LocalCameraStore.CameraData> cameras = LocalCameraStore.getAll();
        if (cameras.isEmpty()) { c.drawCenteredTextWithShadow(textRenderer, Text.literal("Keine Kameras vorhanden."), cx, 70, MpsqTheme.TEXT_GEDAEMPT); return; }
        for (LocalCameraStore.CameraData camera : cameras) { if (row + 20 > TOP && row < height - BOTTOM) { c.fill(cx - 140, row, cx + 140, row + 20, 0x55000000); c.drawCenteredTextWithShadow(textRenderer, Text.literal(camera.name()), cx, row + 6, MpsqTheme.TEXT_NORMAL); } row += ROW; }
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        if (button != 0) return super.mouseClicked(x, y, button);
        int row = TOP - scroll;
        for (LocalCameraStore.CameraData camera : LocalCameraStore.getAll()) {
            if (y >= row && y < row + 20 && row >= TOP && row < height - BOTTOM) { assign(camera); return true; }
            row += ROW;
        }
        return super.mouseClicked(x, y, button);
    }
    private void assign(LocalCameraStore.CameraData camera) {
        JsonObject body = new JsonObject(); body.addProperty("cameraId", camera.id().toString()); body.addProperty("sortOrder", ScreenCameraStore.hasCameras(screenId) ? 1 : 0);
        status = "Speichere ...";
        MpsqApiClient.post("/screens/" + screenId + "/cameras", body).thenCompose(ignored -> ScreenSyncManager.refresh()).whenComplete((ignored, error) -> client.execute(() -> {
            if (error != null) { Throwable c = error.getCause() == null ? error : error.getCause(); status = "Nicht gespeichert: " + (c.getMessage() == null ? "Serverfehler" : c.getMessage()); }
            else client.setScreen(parent);
        }));
    }
    @Override public boolean mouseScrolled(double x, double y, double h, double v) { scroll -= (int)Math.signum(v) * ROW * 2; clamp(); return true; }
    private void clamp() { scroll = Math.max(0, Math.min(scroll, Math.max(0, LocalCameraStore.getAll().size() * ROW - (height - TOP - BOTTOM)))); }
    @Override public boolean shouldPause() { return false; }
}
