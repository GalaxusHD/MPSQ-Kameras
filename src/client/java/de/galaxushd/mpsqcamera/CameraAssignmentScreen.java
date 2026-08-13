package de.galaxushd.mpsqcamera;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

/**
 * Manages all cameras belonging to one screen. A click adds an unassigned
 * camera; clicking an already assigned camera removes only that assignment.
 */
public final class CameraAssignmentScreen extends Screen {
    private static final int TOP = 58;
    private static final int BOTTOM = 62;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_GAP = 22;

    private final Screen parent;
    private final UUID screenId;
    private int scroll;
    private String status = "";
    private boolean saving;

    public CameraAssignmentScreen(Screen parent, UUID screenId) {
        super(Text.literal("Kameras verwalten"));
        this.parent = parent;
        this.screenId = screenId;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(
                        Text.translatable("gui.mpsqcamera.back"),
                        button -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 36, 150, 20)
                .build());
        clampScroll();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int centerX = width / 2;
        int rowY = TOP - scroll;
        List<UUID> assigned = ScreenCameraStore.cameras(screenId);

        context.drawCenteredTextWithShadow(textRenderer, title, centerX, 28, MpsqTheme.TEXT_TITEL);
        context.fill(centerX - 140, 44, centerX + 140, 45, 0x44FFFFFF);
        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Ausgewählt: " + assigned.size()),
                centerX,
                47,
                MpsqTheme.TEXT_GEDAEMPT
        );

        if (!status.isBlank()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), centerX, height - 52, 0xFFFF5555);
        }

        List<LocalCameraStore.CameraData> cameras = LocalCameraStore.getAll();
        if (cameras.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Keine Kameras vorhanden."), centerX, 74, MpsqTheme.TEXT_GEDAEMPT);
            return;
        }

        for (LocalCameraStore.CameraData camera : cameras) {
            if (rowY + ROW_HEIGHT > TOP && rowY < height - BOTTOM) {
                boolean isAssigned = assigned.contains(camera.id());
                context.fill(centerX - 140, rowY, centerX + 140, rowY + ROW_HEIGHT,
                        isAssigned ? 0x77555555 : 0x55000000);
                String marker = isAssigned ? "✓ " : "+ ";
                String label = marker + camera.name();
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), centerX, rowY + 6,
                        isAssigned ? MpsqTheme.TEXT_NORMAL : MpsqTheme.TEXT_GEDAEMPT);
            }
            rowY += ROW_GAP;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || saving) return super.mouseClicked(mouseX, mouseY, button);

        int rowY = TOP - scroll;
        for (LocalCameraStore.CameraData camera : LocalCameraStore.getAll()) {
            if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT && rowY >= TOP && rowY < height - BOTTOM) {
                if (ScreenCameraStore.cameras(screenId).contains(camera.id())) {
                    remove(camera);
                } else {
                    add(camera);
                }
                return true;
            }
            rowY += ROW_GAP;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void add(LocalCameraStore.CameraData camera) {
        JsonObject body = new JsonObject();
        body.addProperty("cameraId", camera.id().toString());
        body.addProperty("sortOrder", ScreenCameraStore.cameras(screenId).size());
        save(MpsqApiClient.post("/screens/" + screenId + "/cameras", body), "Kamera hinzugefügt.");
    }

    private void remove(LocalCameraStore.CameraData camera) {
        save(MpsqApiClient.delete("/screens/" + screenId + "/cameras/" + camera.id()), "Kamera entfernt.");
    }

    private void save(java.util.concurrent.CompletableFuture<?> request, String success) {
        saving = true;
        status = "Speichere ...";
        request.thenCompose(ignored -> ScreenSyncManager.refresh()).whenComplete((ignored, error) -> client.execute(() -> {
            saving = false;
            if (error != null) {
                Throwable cause = error.getCause() == null ? error : error.getCause();
                status = "Nicht gespeichert: " + (cause.getMessage() == null ? "Serverfehler" : cause.getMessage());
            } else {
                status = success;
            }
            clearAndInit();
        }));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll -= (int) Math.signum(verticalAmount) * ROW_GAP * 2;
        clampScroll();
        return true;
    }

    private void clampScroll() {
        int availableHeight = height - TOP - BOTTOM;
        int contentHeight = LocalCameraStore.getAll().size() * ROW_GAP;
        scroll = Math.max(0, Math.min(scroll, Math.max(0, contentHeight - availableHeight)));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
