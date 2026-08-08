package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/** Name confirmation shown before the C key creates a camera. */
public final class CameraCreateScreen extends Screen {
    private static final int WIDTH = 220;
    private TextFieldWidget nameField;
    private ButtonWidget createButton;
    private String status = "";

    public CameraCreateScreen() {
        super(Text.literal("Kamera erstellen"));
    }

    @Override
    protected void init() {
        int x = width / 2 - WIDTH / 2;
        int y = height / 2 - 26;
        nameField = new TextFieldWidget(textRenderer, x, y, WIDTH, 20, Text.literal("Kameraname"));
        nameField.setMaxLength(64);
        nameField.setPlaceholder(Text.literal("Kameraname eingeben"));
        nameField.setChangedListener(ignored -> updateCreateButton());
        addDrawableChild(nameField);
        createButton = addDrawableChild(ButtonWidget.builder(Text.literal("Erstellen"), button -> create())
                .dimensions(x, y + 28, 108, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), button -> close())
                .dimensions(x + 112, y + 28, 108, 20).build());
        updateCreateButton();
        setInitialFocus(nameField);
    }

    private void updateCreateButton() {
        if (nameField == null || createButton == null) return;
        String wanted = nameField.getText().trim();
        boolean duplicate = LocalCameraStore.getAll().stream()
                .anyMatch(camera -> camera.name().trim().equalsIgnoreCase(wanted));
        createButton.active = !wanted.isEmpty() && !duplicate;
        status = duplicate ? "Dieser Kameraname wird bereits verwendet." : "";
    }

    private void create() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) return;
        CameraCreationManager.createNamedCamera(client, name);
        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = width / 2;
        context.drawCenteredTextWithShadow(textRenderer, title, centerX, height / 2 - 52, MpsqTheme.TEXT_TITEL);
        if (!status.isEmpty()) context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), centerX, height / 2 + 56, MpsqTheme.TEXT_GEDAEMPT);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, width, height);
    }

    @Override public boolean shouldPause() { return false; }
}
