package de.galaxushd.mpsqcamera.mixin.client;

import de.galaxushd.mpsqcamera.ScreenCreationManager;
import net.minecraft.client.Mouse;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Sends mouse input either to the player or to the active client-only camera. */
@Mixin(Mouse.class)
public final class MouseMixin {
    @Redirect(
            method = "updateMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;changeLookDirection(DD)V"
            )
    )
    private void mpsq$routeLookInput(Entity entity, double cursorDeltaX, double cursorDeltaY) {
        if (ScreenCreationManager.isCameraViewActive()) {
            ScreenCreationManager.applyCameraLook(cursorDeltaX, cursorDeltaY);
            return;
        }
        entity.changeLookDirection(cursorDeltaX, cursorDeltaY);
    }
}
