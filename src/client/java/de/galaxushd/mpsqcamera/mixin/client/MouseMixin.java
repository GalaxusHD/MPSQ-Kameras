package de.galaxushd.mpsqcamera.mixin.client;

import de.galaxushd.mpsqcamera.ScreenCreationManager;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps the client-only camera proxy in sync with Minecraft's mouse update. */
@Mixin(Mouse.class)
public final class MouseMixin {
    @Inject(method = "updateMouse", at = @At("TAIL"))
    private void mpsq$applyCameraLook(double timeDelta, CallbackInfo ci) {
        ScreenCreationManager.onMouseLookUpdated();
    }
}
