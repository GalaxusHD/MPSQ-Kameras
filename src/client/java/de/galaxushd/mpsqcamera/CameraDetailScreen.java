package de.galaxushd.mpsqcamera;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

/** Owner management menu for one static camera. */
public final class CameraDetailScreen extends Screen {
    private static final int WIDTH = 220;
    private static final int ROW_GAP = 26;

    private final Screen parent;
    private final UUID cameraId;
    private TextFieldWidget nameField;
    private ButtonWidget saveButton;
    private String status = "";

    public CameraDetailScreen(Screen parent, UUID cameraId) {
        super(Text.literal("Kamera verwalten"));
        this.parent = parent;
        this.cameraId = cameraId;
    }

    @Override
    protected void init() {
        LocalCameraStore.CameraData camera = LocalCameraStore.find(cameraId).orElse(null);
        if (camera == null) {
            client.setScreen(parent);
            return;
        }

        int x = width / 2 - WIDTH / 2;
        int y = 62;
        nameField = new TextFieldWidget(textRenderer, x, y, WIDTH, 20, Text.literal("Kameraname"));
        nameField.setMaxLength(64);
        nameField.setText(camera.name());
        nameField.setChangedListener(ignored -> updateSaveButton());
        addDrawableChild(nameField);
        y += ROW_GAP;

        saveButton = addDrawableChild(ButtonWidget.builder(Text.literal("Name speichern"), button -> saveName())
                .dimensions(x, y, WIDTH, 20).build());
        y += ROW_GAP;

        if (camera.kind() == LocalCameraStore.CameraKind.STATIC) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Hier verschieben"), button -> moveHere())
                    .dimensions(x, y, WIDTH, 20).build());
            y += ROW_GAP;
            addDrawableChild(ButtonWidget.builder(Text.literal("In Blickrichtung drehen"), button -> rotateHere())
                    .dimensions(x, y, WIDTH, 20).build());
            y += ROW_GAP;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Kamera löschen").formatted(Formatting.RED), button -> confirmDelete())
                .dimensions(x, y, WIDTH, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), button -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 28, 150, 20).build());
        updateSaveButton();
        setInitialFocus(nameField);
    }

    private void updateSaveButton() {
        if (nameField == null || saveButton == null) return;
        String wanted = nameField.getText().trim();
        boolean duplicate = LocalCameraStore.getAll().stream()
                .anyMatch(camera -> !camera.id().equals(cameraId) && camera.name().trim().equalsIgnoreCase(wanted));
        saveButton.active = !wanted.isEmpty() && !duplicate;
        status = duplicate ? "Dieser Kameraname wird bereits verwendet." : "";
    }

    private void saveName() {
        JsonObject body = new JsonObject();
        body.addProperty("name", nameField.getText().trim());
        saveButton.active = false;
        save(body, "Name gespeichert.");
    }

    private void moveHere() {
        if (client.player == null || client.world == null) return;
        Vec3d position = client.player.getCameraPosVec(1.0F).add(client.player.getRotationVec(1.0F).multiply(0.65));
        JsonObject body = new JsonObject();
        body.addProperty("dimension", client.world.getRegistryKey().getValue().toString());
        body.addProperty("x", position.x);
        body.addProperty("y", position.y);
        body.addProperty("z", position.z);
        body.addProperty("yaw", client.player.getYaw());
        body.addProperty("pitch", client.player.getPitch());
        save(body, "Kamera verschoben.");
    }

    private void rotateHere() {
        if (client.player == null) return;
        JsonObject body = new JsonObject();
        body.addProperty("yaw", client.player.getYaw());
        body.addProperty("pitch", client.player.getPitch());
        save(body, "Kamera gedreht.");
    }

    private void save(JsonObject body, String success) {
        MpsqApiClient.patch("/cameras/" + cameraId, body)
                .thenCompose(ignored -> MpsqApiClient.refreshCameras())
                .whenComplete((ignored, error) -> client.execute(() -> {
                    if (error != null) {
                        Throwable cause = error.getCause() == null ? error : error.getCause();
                        status = "Nicht gespeichert: " + (cause.getMessage() == null ? "Serverfehler" : cause.getMessage());
                        // Do not call updateSaveButton here: it would clear the useful server error.
                        saveButton.active = true;
                    } else {
                        status = success;
                        clearAndInit();
                    }
                }));
    }

    private void confirmDelete() {
        client.setScreen(new ConfirmScreen(confirmed -> {
            if (!confirmed) {
                client.setScreen(this);
                return;
            }
            MpsqApiClient.delete("/cameras/" + cameraId)
                    .thenCompose(ignored -> MpsqApiClient.refreshCameras())
                    .whenComplete((ignored, error) -> client.execute(() -> client.setScreen(parent)));
        }, Text.literal("Kamera löschen"), Text.literal("Kamera wirklich löschen?"),
                Text.literal("Löschen").formatted(Formatting.RED), Text.literal("Abbrechen")));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = width / 2;
        context.drawCenteredTextWithShadow(textRenderer, title, centerX, 30, MpsqTheme.TEXT_TITEL);
        if (!status.isEmpty()) context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), centerX, height - 54, MpsqTheme.TEXT_GEDAEMPT);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, width, height);
    }

    @Override public boolean shouldPause() { return false; }
}
