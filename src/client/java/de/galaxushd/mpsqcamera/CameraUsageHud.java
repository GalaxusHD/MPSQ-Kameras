package de.galaxushd.mpsqcamera;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;

/** Zeigt unten links an, welche Kameras gerade verwendet werden. */
public final class CameraUsageHud {
    private static List<CameraUsage> active = List.of();
    private static int ticks;
    private static boolean loading;

    private CameraUsageHud() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(CameraUsageHud::tick);
        HudRenderCallback.EVENT.register((context, tickDelta) -> render(context));
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || !TeamVisibilitySettings.visible() || loading || !MpsqApiClient.isReady()) {
            return;
        }

        if (++ticks < 20) {
            return;
        }

        ticks = 0;
        TeamProfile self = TeamStateStore.self().orElse(null);
        if (self == null || !self.canOpenTeamArea()) {
            active = List.of();
            return;
        }

        loading = true;
        MpsqApiClient.loadCameraUsage().whenComplete((usages, error) -> client.execute(() -> {
            loading = false;
            if (error == null && usages != null) {
                active = usages;
            }
        }));
    }

    private static void render(net.minecraft.client.gui.DrawContext context) {
        if (!TeamVisibilitySettings.visible() || active.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Untere linke Ecke, oberhalb von Chat und Hotbar.
        int y = context.getScaledWindowHeight() - 50;

        for (int i = active.size() - 1; i >= 0; i--) {
            CameraUsage usage = active.get(i);
            Text text = Text.translatable(
                    "gui.mpsqcamera.team.camera_usage",
                    usage.cameraName()
            );

            // Minecraft-Grün / §a
            context.drawTextWithShadow(client.textRenderer, text, 6, y, 0x55FF55);
            y -= 11;
        }
    }
}
