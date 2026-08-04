package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class CameraListScreen extends Screen {
    private final Screen parent;

    public CameraListScreen(Screen parent) {
        super(Text.translatable("gui.mpsqcamera.cameras.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), button -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 36, 150, 20).build());
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
        context.drawCenteredTextWithShadow(textRenderer, title, centerX, 28, MpsqTheme.TEXT_TITEL);
        List<LocalCameraStore.CameraData> cameras = LocalCameraStore.getAll();
        if (cameras.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.cameras.empty"), centerX, 62, MpsqTheme.TEXT_GEDAEMPT);
            return;
        }
        int y = 58;
        for (LocalCameraStore.CameraData camera : cameras) {
            Text type = Text.translatable(camera.kind() == LocalCameraStore.CameraKind.BODYCAM
                    ? "gui.mpsqcamera.cameras.bodycam" : "gui.mpsqcamera.cameras.static");
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.cameras.entry", camera.name(), type), centerX, y, MpsqTheme.TEXT_NORMAL);
            y += 18;
        }
    }

    @Override public boolean shouldPause() { return false; }
}
