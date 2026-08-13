package de.galaxushd.mpsqcamera.mixin.client;

import de.galaxushd.mpsqcamera.ScreenCreationManager;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps Minecraft's pause menu from opening when ESC ends a camera view. */
@Mixin(Keyboard.class)
public final class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void mpsq$consumeEscapeForCamera(long window, int key, int scanCode,
                                              int action, int modifiers, CallbackInfo ci) {
        if (key == GLFW.GLFW_KEY_ESCAPE && ScreenCreationManager.isCameraViewActive()) {
            // ScreenCreationManager reads the actual key state on the next tick
            // and exits the camera view. Cancel only Vanilla's pause-menu action.
            ci.cancel();
        }
    }
}
