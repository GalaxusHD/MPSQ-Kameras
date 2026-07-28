package de.galaxushd.mpsqcamera;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ScreenCreationManager {
	private static KeyBinding openCreatorKey;

	private ScreenCreationManager() {}

	public static void initialize() {
		openCreatorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.mpsqcamera.open_creator",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_O,
			"category.mpsqcamera.main"
		));

		ClientTickEvents.END_CLIENT_TICK.register(ScreenCreationManager::onEndTick);
	}

	private static void onEndTick(MinecraftClient client) {
		while (openCreatorKey.wasPressed()) {
			if (client.player != null) {
				MpsqCameraClient.LOGGER.info("Open screen creator requested.");
				// Next step: open custom GUI for size + placement.
			}
		}
	}
}
