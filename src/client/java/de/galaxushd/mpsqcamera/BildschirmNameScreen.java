package de.galaxushd.mpsqcamera;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.UUID;

/** Small owner-only editor for a screen name. */
public final class BildschirmNameScreen extends Screen {
    private static final int FIELD_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen parent;
    private final UUID screenId;
    private final String previousName;
    private TextFieldWidget nameField;
    private ButtonWidget saveButton;
    private String status = "";

    public BildschirmNameScreen(Screen parent, UUID screenId, String previousName) {
        super(Text.literal("Bildschirm umbenennen"));
        this.parent = parent;
        this.screenId = screenId;
        this.previousName = previousName;
    }

    @Override
    protected void init() {
        int x = width / 2 - FIELD_WIDTH / 2;
        int y = height / 2 - 26;
        nameField = new TextFieldWidget(textRenderer, x, y, FIELD_WIDTH, BUTTON_HEIGHT, Text.literal("Bildschirmname"));
        nameField.setMaxLength(64);
        nameField.setText(previousName);
        nameField.setChangedListener(ignored -> updateSaveButton());
        addDrawableChild(nameField);

        saveButton = addDrawableChild(ButtonWidget.builder(Text.literal("Speichern"), button -> save())
                .dimensions(x, y + 28, 108, BUTTON_HEIGHT).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), button -> client.setScreen(parent))
                .dimensions(x + 112, y + 28, 108, BUTTON_HEIGHT).build());
        updateSaveButton();
        setInitialFocus(nameField);
    }

    private void updateSaveButton() {
        if (saveButton == null || nameField == null) return;
        String wanted = nameField.getText().trim();
        boolean duplicate = LocalScreenStore.getAllScreens().stream()
                .anyMatch(screen -> !screen.id().equals(screenId) && screen.name().trim().equalsIgnoreCase(wanted));
        saveButton.active = !wanted.isEmpty() && !duplicate;
        status = duplicate ? "Dieser Bildschirmname wird bereits verwendet." : "";
    }

    private void save() {
        String wanted = nameField.getText().trim();
        if (wanted.isEmpty()) return;
        JsonObject body = new JsonObject();
        body.addProperty("name", wanted);
        saveButton.active = false;
        MpsqApiClient.patch("/screens/" + screenId, body)
                .thenCompose(ignored -> ScreenSyncManager.refresh())
                .whenComplete((ignored, error) -> client.execute(() -> {
                    if (error != null) {
                        status = "Name konnte nicht gespeichert werden.";
                        updateSaveButton();
                    } else {
                        client.setScreen(parent);
                    }
                }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = width / 2;
        context.drawCenteredTextWithShadow(textRenderer, title, centerX, height / 2 - 52, MpsqTheme.TEXT_TITEL);
        if (!status.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), centerX, height / 2 + 56, MpsqTheme.TEXT_GEDAEMPT);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, width, height);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
