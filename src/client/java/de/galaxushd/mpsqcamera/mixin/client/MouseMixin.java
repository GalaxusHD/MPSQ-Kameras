package de.galaxushd.mpsqcamera.mixin.client;

import de.galaxushd.mpsqcamera.ScreenCreationManager;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
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
                    // Mouse.updateMouse invokes the method with its concrete
                    // ClientPlayerEntity owner, not the inherited Entity owner.
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
            )
    )
    private void mpsq$routeLookInput(ClientPlayerEntity player, double cursorDeltaX, double cursorDeltaY) {
        if (ScreenCreationManager.isCameraViewActive()) {
            ScreenCreationManager.applyCameraLook(cursorDeltaX, cursorDeltaY);
            return;
        }
        player.changeLookDirection(cursorDeltaX, cursorDeltaY);
    }
}
