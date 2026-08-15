package de.galaxushd.mpsqcamera;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;

/** Shows active camera usage at the lower-right of the HUD. */
public final class CameraUsageHud {
    private static List<CameraUsage> active = List.of();
    private static int ticks;
    private static boolean loading;

    private CameraUsageHud() { }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(CameraUsageHud::tick);
        HudRenderCallback.EVENT.register((context, tickDelta) -> render(context));
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || !TeamVisibilitySettings.visible() || loading || !MpsqApiClient.isReady()) return;
        if (++ticks < 20) return;
        ticks = 0;
        TeamProfile self = TeamStateStore.self().orElse(null);
        if (self == null || !self.canOpenTeamArea()) { active = List.of(); return; }
        loading = true;
        MpsqApiClient.loadCameraUsage().whenComplete((usages, error) -> client.execute(() -> {
            loading = false;
            if (error == null && usages != null) active = usages;
        }));
    }

    private static void render(net.minecraft.client.gui.DrawContext context) {
        if (!TeamVisibilitySettings.visible() || active.isEmpty()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        int y = context.getScaledWindowHeight() - 42;
        for (int i = active.size() - 1; i >= 0; i--) {
            CameraUsage usage = active.get(i);
            Text text = Text.translatable("gui.mpsqcamera.team.camera_usage", usage.viewerName(), usage.cameraName());
            int x = context.getScaledWindowWidth() - client.textRenderer.getWidth(text) - 6;
            context.drawTextWithShadow(client.textRenderer, text, x, y, 0x55FF55);
            y -= 11;
        }
    }
}
