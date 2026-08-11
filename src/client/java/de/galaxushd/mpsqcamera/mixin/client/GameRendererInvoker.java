package de.galaxushd.mpsqcamera.mixin.client;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Invokes Minecraft's normal world renderer for the private camera framebuffer. */
@Mixin(GameRenderer.class)
public interface GameRendererInvoker {
    @Invoker("renderWorld")
    void mpsq$renderWorld(RenderTickCounter tickCounter);
}
