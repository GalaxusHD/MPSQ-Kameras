package de.galaxushd.mpsqcamera.mixin.client;

import de.galaxushd.mpsqcamera.ScreenCreationManager;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps the client-only camera proxy in sync with Minecraft's mouse update. */
@Mixin(Mouse.class)
public final class MouseMixin {
    @Unique private float mpsq$yawBeforeMouse;
    @Unique private float mpsq$pitchBeforeMouse;

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void mpsq$rememberLookBeforeMouse(double timeDelta, CallbackInfo ci) {
        var player = net.minecraft.client.MinecraftClient.getInstance().player;
        if (player == null) return;
        mpsq$yawBeforeMouse = player.getYaw();
        mpsq$pitchBeforeMouse = player.getPitch();
    }

    @Inject(method = "updateMouse", at = @At("TAIL"))
    private void mpsq$applyCameraLook(double timeDelta, CallbackInfo ci) {
        ScreenCreationManager.onMouseLookUpdated(mpsq$yawBeforeMouse, mpsq$pitchBeforeMouse);
    }
}
