package de.galaxushd.mpsqcamera.mixin.client;

import de.galaxushd.mpsqcamera.ScreenCreationManager;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps the experience display out of the camera view without hiding action messages. */
@Mixin(InGameHud.class)
public final class InGameHudMixin {
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void mpsq$hideHotbarAndExperienceForCamera(
            net.minecraft.client.gui.DrawContext context,
            RenderTickCounter tickCounter,
            CallbackInfo ci
    ) {
        if (ScreenCreationManager.isCameraViewActive()) {
            ci.cancel();
        }
    }
}
