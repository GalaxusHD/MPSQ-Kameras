package de.galaxushd.mpsqcamera;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public final class ScreenCreationManager {
	private static boolean wasAttackPressedLastTick = false;

	private ScreenCreationManager() {}

	public static void initialize() {
		ClientTickEvents.END_CLIENT_TICK.register(ScreenCreationManager::onEndTick);
	}

	private static void onEndTick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.options == null) return;

		boolean attackPressed = client.options.attackKey.isPressed();
		boolean sneakPressed = client.player.isSneaking();

		// Trigger once per click (edge detection), not every tick.
		if (attackPressed && !wasAttackPressedLastTick && sneakPressed) {
			tryCreateScreen(client, client.player);
		}

		wasAttackPressedLastTick = attackPressed;
	}

	private static void tryCreateScreen(MinecraftClient client, ClientPlayerEntity player) {
		// Requirement: hold a regular arrow.
		boolean holdingArrow =
			player.getMainHandStack().isOf(Items.ARROW) ||
			player.getOffHandStack().isOf(Items.ARROW);

		if (!holdingArrow) return;

		if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
			MpsqCameraClient.LOGGER.info("[MPSQ Kameras] No block targeted.");
			return;
		}

		BlockHitResult hit = (BlockHitResult) client.crosshairTarget;
		BlockPos targetPos = hit.getBlockPos();
		BlockState state = client.world.getBlockState(targetPos);

		// Just basic validity check.
		if (state.isAir()) {
			MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Target block is air.");
			return;
		}

		LocalScreenStore.addScreen(targetPos, player.getPos());
		MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Screen created at {}", targetPos);
	}
}
