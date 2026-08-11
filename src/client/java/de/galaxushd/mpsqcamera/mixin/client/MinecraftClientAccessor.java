package de.galaxushd.mpsqcamera.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Access to the render target used only during an off-screen camera pass. */
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("framebuffer")
    @Mutable
    void mpsq$setFramebuffer(Framebuffer framebuffer);

    @Accessor("gameRenderer")
    GameRenderer mpsq$getGameRenderer();
}
