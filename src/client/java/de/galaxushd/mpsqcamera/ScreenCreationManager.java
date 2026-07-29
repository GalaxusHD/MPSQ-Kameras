package de.galaxushd.mpsqcamera;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ScreenCreationManager {
	private static boolean wasAttackPressedLastTick = false;

	/** M – Hauptmenü des Mods */
	private static KeyBinding hauptMenuKey;
	/** O – Konfigurations-Screen für nahegelegene Bildschirme */
	private static KeyBinding bildschirmConfigKey;

	private ScreenCreationManager() {}

	public static void initialize() {
		hauptMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.mpsqcamera.hauptmenu",
				GLFW.GLFW_KEY_M,
				"category.mpsqcamera.main"
		));

		bildschirmConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.mpsqcamera.bildschirm_config",
				GLFW.GLFW_KEY_O,
				"category.mpsqcamera.main"
		));

		ClientTickEvents.END_CLIENT_TICK.register(ScreenCreationManager::onEndTick);
	}

	private static void onEndTick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.options == null) return;

		boolean attackPressed = client.options.attackKey.isPressed();
		boolean sneakPressed  = client.player.isSneaking();

		// Shift + LeftClick with Arrow => Bildschirm erstellen
		if (attackPressed && !wasAttackPressedLastTick && sneakPressed) {
			tryCreateScreen(client, client.player);
		}
		wasAttackPressedLastTick = attackPressed;

		// M => Hauptmenü öffnen
		while (hauptMenuKey.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new ModConfigScreen());
			}
		}

		// O => Konfig für nächsten Bildschirm öffnen
		while (bildschirmConfigKey.wasPressed()) {
			openNearestScreenConfig(client, client.player);
		}
	}

	private static void tryCreateScreen(MinecraftClient client, ClientPlayerEntity player) {
		Item toolItem = getConfiguredToolItem();

		boolean holdingTool =
				player.getMainHandStack().isOf(toolItem) ||
				player.getOffHandStack().isOf(toolItem);

		if (!holdingTool) return;

		if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
			MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Kein Block anvisiert.");
			return;
		}

		BlockHitResult hit = (BlockHitResult) client.crosshairTarget;
		BlockPos targetPos = hit.getBlockPos();
		BlockState state   = client.world.getBlockState(targetPos);

		if (state.isAir()) {
			MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Zielblock ist Luft.");
			return;
		}

		LocalScreenStore.addScreen(targetPos, player.getPos());
		MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Bildschirm erstellt bei {}", targetPos);
	}

	private static void openNearestScreenConfig(MinecraftClient client, ClientPlayerEntity player) {
		LocalScreenStore.findNearest(player.getPos(), 8.0).ifPresentOrElse(
				screen -> client.setScreen(new ScreenConfigScreen(screen)),
				() -> MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Kein Bildschirm in der Nähe zum Konfigurieren.")
		);
	}

	/** Gibt das per Konfiguration eingestellte Werkzeug-Item zurück (Fallback: Pfeil). */
	private static Item getConfiguredToolItem() {
		try {
			Identifier id = Identifier.of(ModConfig.getToolItemId());
			Item item = Registries.ITEM.get(id);
			// Registries.ITEM.get() gibt Items.AIR zurück wenn nicht vorhanden
			return (item == null || item == Items.AIR) ? Items.ARROW : item;
		} catch (Exception e) {
			return Items.ARROW;
		}
	}
}
