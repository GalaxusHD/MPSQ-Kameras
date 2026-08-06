package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class CameraListScreen extends Screen {
    private static final int LIST_TOP = 58;
    private static final int LIST_BOTTOM_MARGIN = 62;
    private static final int ROW_HEIGHT = 18;
    private final Screen parent;
    private int scrollOffset;

    public CameraListScreen(Screen parent) {
        super(Text.translatable("gui.mpsqcamera.cameras.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), button -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 28, 150, 20).build());
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
        context.drawCenteredTextWithShadow(textRenderer, title, centerX, 28, MpsqTheme.TEXT_TITEL);
        context.fill(centerX - 140, 44, centerX + 140, 45, 0x44FFFFFF);
        List<LocalCameraStore.CameraData> cameras = LocalCameraStore.getAll();
        if (cameras.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.cameras.empty"), centerX, LIST_TOP + 20, MpsqTheme.TEXT_GEDAEMPT);
            return;
        }
        int listBottom = height - LIST_BOTTOM_MARGIN;
        int y = LIST_TOP - scrollOffset;
        for (LocalCameraStore.CameraData camera : cameras) {
            if (y + ROW_HEIGHT > LIST_TOP && y < listBottom) {
                Text type = Text.translatable(camera.kind() == LocalCameraStore.CameraKind.BODYCAM
                        ? "gui.mpsqcamera.cameras.bodycam" : "gui.mpsqcamera.cameras.static");
                context.fill(centerX - 140, y, centerX + 140, y + ROW_HEIGHT - 2, 0x33000000);
                context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.cameras.entry", camera.name(), type), centerX, y + 4, MpsqTheme.TEXT_NORMAL);
            }
            y += ROW_HEIGHT;
        }
        if (maxScroll(cameras.size()) > 0) context.drawCenteredTextWithShadow(textRenderer, Text.literal("Mausrad zum Scrollen"), centerX, height - 54, MpsqTheme.TEXT_GEDAEMPT);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (int) Math.signum(verticalAmount) * ROW_HEIGHT * 2;
        clampScroll();
        return true;
    }

    private int maxScroll(int entries) { return Math.max(0, entries * ROW_HEIGHT - (height - LIST_TOP - LIST_BOTTOM_MARGIN)); }
    private void clampScroll() { scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll(LocalCameraStore.getAll().size()))); }
    @Override public boolean shouldPause() { return false; }
}
