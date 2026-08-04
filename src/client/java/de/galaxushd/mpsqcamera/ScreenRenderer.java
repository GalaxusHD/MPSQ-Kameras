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

/** Renders the client-only black screen surface and its selection frame. */
public final class ScreenRenderer {
    private static final double RENDER_RANGE = 64.0;
    private ScreenRenderer() { }
    public static void initialize() { WorldRenderEvents.AFTER_ENTITIES.register(ScreenRenderer::render); }

    private static void render(WorldRenderContext ctx) {
        VertexConsumerProvider consumers = ctx.consumers(); MatrixStack matrices = ctx.matrixStack();
        if (consumers == null || matrices == null) return;
        Vec3d camera = ctx.camera().getPos();
        matrices.push(); matrices.translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer quads = consumers.getBuffer(RenderLayer.getDebugQuads());
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
        for (LocalScreenStore.LocalScreenData screen : LocalScreenStore.getAllScreens()) {
            BlockPos a = screen.pos1(), b = screen.pos2();
            if (a.getSquaredDistance(camera.x, camera.y, camera.z) > RENDER_RANGE * RENDER_RANGE) continue;
            double x1=Math.min(a.getX(),b.getX()), y1=Math.min(a.getY(),b.getY()), z1=Math.min(a.getZ(),b.getZ());
            double x2=Math.max(a.getX(),b.getX())+1, y2=Math.max(a.getY(),b.getY())+1, z2=Math.max(a.getZ(),b.getZ())+1;
            drawSurface(matrices, quads, x1,y1,z1,x2,y2,z2);
            VertexRendering.drawBox(matrices, lines, x1,y1,z1,x2,y2,z2, 1f,.9f,0f,.8f);
        }
        matrices.pop();
    }

    private static void drawSurface(MatrixStack matrices, VertexConsumer vc, double x1,double y1,double z1,double x2,double y2,double z2) {
        double dx=x2-x1, dy=y2-y1, dz=z2-z1; double e=.003;
        if (dz <= dx && dz <= dy) quad(matrices,vc,x1,y1,(z1+z2)/2-e, x2,y1,(z1+z2)/2-e, x2,y2,(z1+z2)/2-e, x1,y2,(z1+z2)/2-e);
        else if (dx <= dy && dx <= dz) quad(matrices,vc,(x1+x2)/2-e,y1,z1, (x1+x2)/2-e,y1,z2, (x1+x2)/2-e,y2,z2, (x1+x2)/2-e,y2,z1);
        else quad(matrices,vc,x1,(y1+y2)/2-e,z1, x2,(y1+y2)/2-e,z1, x2,(y1+y2)/2-e,z2, x1,(y1+y2)/2-e,z2);
    }

    private static void quad(MatrixStack m, VertexConsumer vc, double ax,double ay,double az,double bx,double by,double bz,double cx,double cy,double cz,double dx,double dy,double dz) {
        vertex(m,vc,ax,ay,az); vertex(m,vc,bx,by,bz); vertex(m,vc,cx,cy,cz); vertex(m,vc,dx,dy,dz);
    }
    private static void vertex(MatrixStack m, VertexConsumer vc,double x,double y,double z) { vc.vertex(m.peek(),(float)x,(float)y,(float)z).color(0,0,0,235); }
}
