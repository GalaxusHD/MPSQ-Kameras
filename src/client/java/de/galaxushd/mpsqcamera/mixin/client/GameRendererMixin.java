package de.galaxushd.mpsqcamera.mixin.client;

import de.galaxushd.mpsqcamera.ScreenCreationManager;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The held player item must not be rendered from a remote camera view. */
@Mixin(GameRenderer.class)
public final class GameRendererMixin {
    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void mpsq$hideHeldItemForCamera(float tickProgress, boolean sleeping,
                                             Matrix4f positionMatrix, CallbackInfo ci) {
        if (ScreenCreationManager.isCameraViewActive()) {
            ci.cancel();
        }
    }
}
