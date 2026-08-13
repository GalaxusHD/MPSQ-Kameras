package de.galaxushd.mpsqcamera.mixin.client;

import de.galaxushd.mpsqcamera.ScreenCreationManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A remote camera is a clean monitor view.  Vanilla's hotbar, experience bar,
 * level number, crosshair and other in-game overlays would otherwise remain
 * visible over it, so suppress the HUD only while a camera session is active.
 */
@Mixin(InGameHud.class)
public final class InGameHudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void mpsq$hideHudForCamera(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ScreenCreationManager.isCameraViewActive()) {
            ci.cancel();
        }
    }
}
