package de.galaxushd.mpsqcamera;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Rendert Bildschirme und Kamera-Positionen als sichtbare Wireframe-Boxen im Welt-Raum.
 *
 * Farben:
 *  - Bildschirme (screen-Bereich pos1→pos2): Gelb (R=1.0, G=0.9, B=0.0, A=0.8)
 *  - Kamera-Markierungen (createdFrom-Position): Cyan (R=0.0, G=1.0, B=1.0, A=0.9)
 */
public final class ScreenRenderer {

    /** Sichtweite für Bildschirm-Rendering (in Blöcken). */
    private static final double RENDER_RANGE = 64.0;

    private ScreenRenderer() {}

    public static void initialize() {
        WorldRenderEvents.AFTER_ENTITIES.register(ScreenRenderer::render);
    }

    private static void render(WorldRenderContext ctx) {
        VertexConsumerProvider consumers = ctx.consumers();
        if (consumers == null) return;

        MatrixStack matrices = ctx.matrixStack();
        if (matrices == null) return;

        Vec3d camPos = ctx.camera().getPos();

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        VertexConsumer vc = consumers.getBuffer(RenderLayer.getLines());

        for (LocalScreenStore.LocalScreenData screen : LocalScreenStore.getAllScreens()) {
            BlockPos p1 = screen.pos1();
            BlockPos p2 = screen.pos2();

            // Abstandsprüfung (nur nah genug rendern)
            double dx = p1.getX() + 0.5 - camPos.x;
            double dy = p1.getY() + 0.5 - camPos.y;
            double dz = p1.getZ() + 0.5 - camPos.z;
            if (dx * dx + dy * dy + dz * dz > RENDER_RANGE * RENDER_RANGE) continue;

            double x1 = Math.min(p1.getX(), p2.getX());
            double y1 = Math.min(p1.getY(), p2.getY());
            double z1 = Math.min(p1.getZ(), p2.getZ());
            double x2 = Math.max(p1.getX(), p2.getX()) + 1.0;
            double y2 = Math.max(p1.getY(), p2.getY()) + 1.0;
            double z2 = Math.max(p1.getZ(), p2.getZ()) + 1.0;

            // Gelbe Wireframe-Box für den Bildschirm-Bereich
            VertexRendering.drawBox(matrices, vc, x1, y1, z1, x2, y2, z2,
                    1.0f, 0.9f, 0.0f, 0.8f);

            // Kamera-Marker: kleiner Cyan-Würfel an der createdFrom-Position
            Vec3d cFrom = screen.createdFrom();
            double mx = cFrom.x - 0.15;
            double my = cFrom.y + 0.85;
            double mz = cFrom.z - 0.15;
            VertexRendering.drawBox(matrices, vc, mx, my, mz, mx + 0.3, my + 0.3, mz + 0.3,
                    0.0f, 1.0f, 1.0f, 0.9f);
        }

        matrices.pop();
    }
}
