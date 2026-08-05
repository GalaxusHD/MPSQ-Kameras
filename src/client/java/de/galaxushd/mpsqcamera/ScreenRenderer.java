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

/** Draws one fixed, client-only front surface for every screen. */
public final class ScreenRenderer {
    private static final double RENDER_RANGE = 64.0;
    private static final double SURFACE_OFFSET = 0.003;

    private ScreenRenderer() { }
    public static void initialize() { WorldRenderEvents.AFTER_ENTITIES.register(ScreenRenderer::render); }

    private static void render(WorldRenderContext context) {
        VertexConsumerProvider consumers = context.consumers();
        MatrixStack matrices = context.matrixStack();
        if (consumers == null || matrices == null) return;
        Vec3d camera = context.camera().getPos();
        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer quads = consumers.getBuffer(RenderLayer.getDebugQuads());
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
        for (LocalScreenStore.LocalScreenData screen : LocalScreenStore.getAllScreens()) {
            BlockPos a = screen.pos1();
            BlockPos b = screen.pos2();
            if (a.getSquaredDistance(camera.x, camera.y, camera.z) > RENDER_RANGE * RENDER_RANGE) continue;
            double x1 = Math.min(a.getX(), b.getX());
            double y1 = Math.min(a.getY(), b.getY());
            double z1 = Math.min(a.getZ(), b.getZ());
            double x2 = Math.max(a.getX(), b.getX()) + 1.0;
            double y2 = Math.max(a.getY(), b.getY()) + 1.0;
            double z2 = Math.max(a.getZ(), b.getZ()) + 1.0;
            drawFrontSurface(matrices, quads, ScreenAccessStore.front(screen.id()), x1, y1, z1, x2, y2, z2);
            VertexRendering.drawBox(matrices, lines, x1, y1, z1, x2, y2, z2, 1.0f, 0.9f, 0.0f, 0.8f);
        }
        matrices.pop();
    }

    /** The front is deterministic: one surface only, never a dynamically mirrored rear surface. */
    private static void drawFrontSurface(MatrixStack matrices, VertexConsumer vertices, String front,
                                         double x1, double y1, double z1, double x2, double y2, double z2) {
        if ("SOUTH".equals(front)) { quad(matrices, vertices, x2, y1, z2 + SURFACE_OFFSET, x1, y1, z2 + SURFACE_OFFSET, x1, y2, z2 + SURFACE_OFFSET, x2, y2, z2 + SURFACE_OFFSET); return; }
        if ("WEST".equals(front)) { quad(matrices, vertices, x1 - SURFACE_OFFSET, y1, z2, x1 - SURFACE_OFFSET, y1, z1, x1 - SURFACE_OFFSET, y2, z1, x1 - SURFACE_OFFSET, y2, z2); return; }
        if ("EAST".equals(front)) { quad(matrices, vertices, x2 + SURFACE_OFFSET, y1, z1, x2 + SURFACE_OFFSET, y1, z2, x2 + SURFACE_OFFSET, y2, z2, x2 + SURFACE_OFFSET, y2, z1); return; }
        if ("UP".equals(front)) { quad(matrices, vertices, x1, y2 + SURFACE_OFFSET, z1, x1, y2 + SURFACE_OFFSET, z2, x2, y2 + SURFACE_OFFSET, z2, x2, y2 + SURFACE_OFFSET, z1); return; }
        if ("DOWN".equals(front)) { quad(matrices, vertices, x2, y1 - SURFACE_OFFSET, z1, x2, y1 - SURFACE_OFFSET, z2, x1, y1 - SURFACE_OFFSET, z2, x1, y1 - SURFACE_OFFSET, z1); return; }
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        if (dz <= dx && dz <= dy) {
            quad(matrices, vertices, x1, y1, z1 - SURFACE_OFFSET, x2, y1, z1 - SURFACE_OFFSET,
                    x2, y2, z1 - SURFACE_OFFSET, x1, y2, z1 - SURFACE_OFFSET);
        } else if (dx <= dy && dx <= dz) {
            quad(matrices, vertices, x1 - SURFACE_OFFSET, y1, z2, x1 - SURFACE_OFFSET, y1, z1,
                    x1 - SURFACE_OFFSET, y2, z1, x1 - SURFACE_OFFSET, y2, z2);
        } else {
            quad(matrices, vertices, x1, y1 - SURFACE_OFFSET, z1, x2, y1 - SURFACE_OFFSET, z1,
                    x2, y1 - SURFACE_OFFSET, z2, x1, y1 - SURFACE_OFFSET, z2);
        }
    }

    private static void quad(MatrixStack matrices, VertexConsumer vertices,
                             double ax, double ay, double az, double bx, double by, double bz,
                             double cx, double cy, double cz, double dx, double dy, double dz) {
        vertex(matrices, vertices, ax, ay, az);
        vertex(matrices, vertices, bx, by, bz);
        vertex(matrices, vertices, cx, cy, cz);
        vertex(matrices, vertices, dx, dy, dz);
    }

    private static void vertex(MatrixStack matrices, VertexConsumer vertices, double x, double y, double z) {
        vertices.vertex(matrices.peek(), (float) x, (float) y, (float) z).color(0, 0, 0, 235);
    }
}
