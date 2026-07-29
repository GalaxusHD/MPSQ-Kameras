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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public final class ScreenCreationManager {

	private static boolean wasAttackPressedLastTick = false;

	/**
	 * Erste markierte Position für die Bildschirm-Erstellung.
	 * Null = kein aktiver Auswahl-Modus.
	 */
	private static BlockPos selectionPos1 = null;

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

	// ── Getter für SelectionRenderer ─────────────────────────────────────────

	/** Erste Auswahlposition – null wenn kein Auswahl-Modus aktiv. */
	public static BlockPos getSelectionPos1() {
		return selectionPos1;
	}

	/**
	 * Aktuelle Vorschau-Endposition (Block am Fadenkreuz).
	 * Wird vom SelectionRenderer für den roten Live-Rahmen genutzt.
	 */
	public static BlockPos getSelectionPos2Preview() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.crosshairTarget == null) return null;
		if (client.crosshairTarget.getType() != HitResult.Type.BLOCK) return null;
		return ((BlockHitResult) client.crosshairTarget).getBlockPos();
	}

	// ── Tick-Handler ─────────────────────────────────────────────────────────

	private static void onEndTick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.options == null) return;

		boolean attackPressed = client.options.attackKey.isPressed();
		boolean sneakPressed  = client.player.isSneaking();

		// Auswahl-Abbruch per Rechtsklick (Benutzen-Taste)
		if (selectionPos1 != null && client.options.useKey.isPressed()) {
			selectionPos1 = null;
			client.player.sendMessage(
					Text.translatable("gui.mpsqcamera.auswahl.abgebrochen"), true);
		}

		// Shift + Linksklick mit Werkzeug-Item → Positions-Markierung (zweistufig)
		if (attackPressed && !wasAttackPressedLastTick && sneakPressed) {
			tryMarkPosition(client, client.player);
		}
		wasAttackPressedLastTick = attackPressed;

		// M → Hauptmenü öffnen
		while (hauptMenuKey.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new ModConfigScreen());
			}
		}

		// O → Konfig für nächsten Bildschirm öffnen
		while (bildschirmConfigKey.wasPressed()) {
			openNearestScreenConfig(client, client.player);
		}
	}

	private static void tryMarkPosition(MinecraftClient client, ClientPlayerEntity player) {
		// Konfiguriertes Werkzeug-Item auflösen (Fallback: Pfeil)
		Identifier itemId = Identifier.tryParse(ModConfig.toolItemId);
		Item toolItem = (itemId != null)
				? Registries.ITEM.getOrEmpty(itemId).orElse(Items.ARROW)
				: Items.ARROW;

		boolean holdingTool =
				player.getMainHandStack().isOf(toolItem) ||
				player.getOffHandStack().isOf(toolItem);
		if (!holdingTool) return;

		if (client.crosshairTarget == null ||
				client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
			player.sendMessage(
					Text.translatable("gui.mpsqcamera.auswahl.kein_block"), true);
			return;
		}

		BlockHitResult hit    = (BlockHitResult) client.crosshairTarget;
		BlockPos       target = hit.getBlockPos();
		BlockState     state  = client.world.getBlockState(target);

		if (state.isAir()) {
			player.sendMessage(
					Text.translatable("gui.mpsqcamera.auswahl.luft"), true);
			return;
		}

		if (selectionPos1 == null) {
			// ── Erster Klick: Startpunkt markieren ───────────────────────────
			selectionPos1 = target.toImmutable();
			player.sendMessage(
					Text.translatable("gui.mpsqcamera.auswahl.pos1_gesetzt"), true);
			MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Pos 1 markiert: {}", selectionPos1);
		} else {
			// ── Zweiter Klick: Endpunkt → automatisch bestätigen & Menü öffnen
			BlockPos pos1 = selectionPos1;
			BlockPos pos2 = target.toImmutable();
			selectionPos1 = null; // Auswahl-Modus beenden

			MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Pos 2 markiert: {} → Erstell-Menü öffnen", pos2);
			client.setScreen(new BildschirmErstellenScreen(pos1, pos2));
		}
	}

	private static void openNearestScreenConfig(MinecraftClient client, ClientPlayerEntity player) {
		LocalScreenStore.findNearest(player.getPos(), 8.0).ifPresentOrElse(
				screen -> client.setScreen(new ScreenConfigScreen(screen)),
				() -> MpsqCameraClient.LOGGER.info(
						"[MPSQ Kameras] Kein Bildschirm in der Nähe zum Konfigurieren.")
		);
	}
}
