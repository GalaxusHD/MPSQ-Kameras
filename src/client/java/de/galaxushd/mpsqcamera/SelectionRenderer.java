package de.galaxushd.mpsqcamera;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Zeichnet einen roten Wireframe-Rahmen im Welt-Raum während der zweistufigen
 * Positions-Auswahl (Shift + Klick mit Werkzeug-Item).
 *
 * Farbe: Rot (RGBA 255, 0, 0, 0.8)
 */
public final class SelectionRenderer {

    private SelectionRenderer() {}

    public static void initialize() {
        WorldRenderEvents.AFTER_ENTITIES.register(SelectionRenderer::render);
    }

    private static void render(WorldRenderContext ctx) {
        BlockPos pos1 = ScreenCreationManager.getSelectionPos1();
        if (pos1 == null) return;

        // Vorschau-Endpunkt: aktuell anvisierter Block (oder pos1 wenn kein Block)
        BlockPos pos2 = ScreenCreationManager.getSelectionPos2Preview();
        if (pos2 == null) pos2 = pos1;

        VertexConsumerProvider consumers = ctx.consumers();
        if (consumers == null) return;

        MatrixStack matrices = ctx.matrixStack();
        if (matrices == null) return;

        Vec3d camPos = ctx.camera().getPos();

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        // Begrenzende Box zwischen den beiden Positionen (je +1 für volle Blockausdehnung)
        double x1 = Math.min(pos1.getX(), pos2.getX());
        double y1 = Math.min(pos1.getY(), pos2.getY());
        double z1 = Math.min(pos1.getZ(), pos2.getZ());
        double x2 = Math.max(pos1.getX(), pos2.getX()) + 1.0;
        double y2 = Math.max(pos1.getY(), pos2.getY()) + 1.0;
        double z2 = Math.max(pos1.getZ(), pos2.getZ()) + 1.0;

        VertexConsumer vc = consumers.getBuffer(RenderLayer.getLines());
        // Roter Wireframe-Rahmen (R=1.0, G=0.0, B=0.0, A=0.8)
        WorldRenderer.drawBox(matrices, vc, x1, y1, z1, x2, y2, z2,
                1.0f, 0.0f, 0.0f, 0.8f);

        matrices.pop();
    }
}
