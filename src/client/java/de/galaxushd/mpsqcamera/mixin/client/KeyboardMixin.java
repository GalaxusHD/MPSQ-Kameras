package de.galaxushd.mpsqcamera.mixin.client;

import de.galaxushd.mpsqcamera.ScreenCreationManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
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
        if (key != GLFW.GLFW_KEY_ESCAPE || !ScreenCreationManager.isCameraViewActive()) {
            return;
        }

        // Preserve ESC for the chat and every other open vanilla screen. The
        // camera manager is notified so it does not interpret this same press
        // as its leave-camera key.
        if (MinecraftClient.getInstance().currentScreen != null) {
            if (action == GLFW.GLFW_PRESS) {
                ScreenCreationManager.ignoreEscapeForOpenScreen();
            }
            return;
        }

        // With no UI open, ESC leaves the camera but must not open Minecraft's
        // pause menu in the same key press.
        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            ci.cancel();
        }
    }
}
