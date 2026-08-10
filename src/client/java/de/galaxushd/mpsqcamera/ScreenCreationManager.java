package de.galaxushd.mpsqcamera;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.UUID;

public final class ScreenCreationManager {
	private static final double CAMERA_LOAD_RANGE = 48.0;
	private static final long VIEW_ENTER_COOLDOWN_MS = 400L;
	private static final long OFFLINE_HINT_INTERVAL_MS = 1000L;
	private static final double CAMERA_EYE_HEIGHT = 1.62;

	private static boolean wasUsePressedLastTick = false;
	private static boolean wasEscPressedLastTick = false;
	private static boolean wasPreviousCameraPressed = false;
	private static boolean wasNextCameraPressed = false;
	private static BlockPos selectionPos1;
	private static Direction selectionSide;
	private static long lastViewEnterAttemptMs = 0L;
	private static long lastOfflineHintMs = 0L;

	/** K – Hauptmenü des Mods */
	private static KeyBinding hauptMenuKey;

	private static ViewSession activeViewSession;

	private ScreenCreationManager() {}

	public static void initialize() {
		hauptMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.mpsqcamera.hauptmenu",
				GLFW.GLFW_KEY_K,
				"category.mpsqcamera.main"
		));

		ClientTickEvents.START_CLIENT_TICK.register(ScreenCreationManager::onStartTick);
		ClientTickEvents.END_CLIENT_TICK.register(ScreenCreationManager::onEndTick);
	}

	private static void onStartTick(MinecraftClient client) {
		if (activeViewSession == null || client.player == null || client.world == null || client.options == null) {
			return;
		}

		suppressMovementAndInteraction(client);
		lockPlayerPosition(client.player, activeViewSession.originPos());
		activeViewSession.cameraEntity().setYaw(client.player.getYaw());
		activeViewSession.cameraEntity().setPitch(client.player.getPitch());
	}

	private static void onEndTick(MinecraftClient client) {
		if (client.options == null) return;

		boolean usePressed = client.options.useKey.isPressed();
		boolean escPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_ESCAPE);
		boolean previousCameraPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_COMMA);
		boolean nextCameraPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_PERIOD);

		if (client.player == null || client.world == null) {
			if (activeViewSession != null) {
				exitViewMode(client, false);
			}
			wasUsePressedLastTick = usePressed;
			wasEscPressedLastTick = escPressed;
			return;
		}

		ClientPlayerEntity player = client.player;

		if (activeViewSession != null) {
			LocalScreenStore.findByAnchor(activeViewSession.sourceAnchor()).ifPresent(screen -> {
				if (previousCameraPressed && !wasPreviousCameraPressed) switchCamera(screen.id(), -1);
				if (nextCameraPressed && !wasNextCameraPressed) switchCamera(screen.id(), 1);
			});
			handleActiveViewSession(client, player, usePressed, escPressed);
		} else {
			LocalScreenStore.LocalScreenData lookedAt = getScreenAtCrosshair(client).orElse(null);
			if (lookedAt != null && previousCameraPressed && !wasPreviousCameraPressed) switchCamera(lookedAt.id(), -1);
			if (lookedAt != null && nextCameraPressed && !wasNextCameraPressed) switchCamera(lookedAt.id(), 1);
			handlePassiveScreenLook(client, player);

			if (usePressed && !wasUsePressedLastTick) {
				if (isHoldingToolItem(player)) {
					tryCreateScreen(client, player);
				} else {
					tryEnterViewMode(client, player);
				}
			}
		}

		// K => Hauptmenü öffnen
		while (hauptMenuKey.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new ModConfigScreen());
			}
		}

		wasUsePressedLastTick = usePressed;
		wasEscPressedLastTick = escPressed;
		wasPreviousCameraPressed = previousCameraPressed;
		wasNextCameraPressed = nextCameraPressed;
	}

	private static void switchCamera(UUID screenId, int direction) {
		UUID camera = ScreenCameraStore.next(screenId, direction);
		if (camera != null) MpsqCameraClient.LOGGER.info("[MPSQ] Aktive Kamera gewechselt: {}", camera);
	}

	private static UUID activeCameraId(LocalScreenStore.LocalScreenData screen) {
		UUID active = ScreenCameraStore.active(screen.id());
		return active != null ? active : screen.cameraId();
	}

	private static void handlePassiveScreenLook(MinecraftClient client, ClientPlayerEntity player) {
		LocalScreenStore.LocalScreenData screen = getScreenAtCrosshair(client).orElse(null);
		UUID cameraId = screen == null ? null : activeCameraId(screen);
		if (screen == null || screen.inputType() != LocalScreenStore.ScreenInputType.CAMERA || cameraId == null) {
			return;
		}

		Optional<LocalCameraStore.CameraData> cameraScreen = LocalCameraStore.find(cameraId);
		if (cameraScreen.isEmpty() || cameraScreen.get().position() == null) {
			sendOfflineHint(player);
			return;
		}

		Vec3d cameraPos = cameraScreen.get().position();
		if (!isCameraAreaLoadedByAnyPlayer(client, cameraPos)) {
			sendOfflineHint(player);
		}
	}

	private static void sendOfflineHint(ClientPlayerEntity player) {
		long now = System.currentTimeMillis();
		if (now - lastOfflineHintMs >= OFFLINE_HINT_INTERVAL_MS) {
			player.sendMessage(Text.translatable("status.mpsqcamera.camera_offline"), true);
			lastOfflineHintMs = now;
		}
	}

	private static void tryEnterViewMode(MinecraftClient client, ClientPlayerEntity player) {
		long now = System.currentTimeMillis();
		if (now - lastViewEnterAttemptMs < VIEW_ENTER_COOLDOWN_MS) {
			return;
		}
		lastViewEnterAttemptMs = now;

		LocalScreenStore.LocalScreenData screen = getScreenAtCrosshair(client).orElse(null);
		UUID cameraId = screen == null ? null : activeCameraId(screen);
		if (screen == null || screen.inputType() != LocalScreenStore.ScreenInputType.CAMERA || cameraId == null) {
			return;
		}

		LocalCameraStore.CameraData cameraScreen = LocalCameraStore.find(cameraId).orElse(null);
		if (cameraScreen == null || cameraScreen.position() == null) {
			sendOfflineHint(player);
			return;
		}

		Vec3d cameraPos = cameraScreen.position();
		if (!isCameraAreaLoadedByAnyPlayer(client, cameraPos)) {
			sendOfflineHint(player);
			return;
		}

		ArmorStandEntity cameraEntity = new ArmorStandEntity(client.world, cameraPos.x, cameraPos.y, cameraPos.z);
		cameraEntity.setNoGravity(true);
		cameraEntity.setInvisible(true);
        // Enter a camera from its saved viewing direction. The player can
        // still look around after entering the anchored view mode.
        player.setYaw(cameraScreen.yaw());
        player.setPitch(cameraScreen.pitch());
        cameraEntity.setYaw(cameraScreen.yaw());
        cameraEntity.setPitch(cameraScreen.pitch());

		Entity previousCamera = client.getCameraEntity();
		Perspective previousPerspective = client.options.getPerspective();

		activeViewSession = new ViewSession(
				screen.pos1().toImmutable(),
				cameraId,
				player.getPos(),
				client.world.getRegistryKey(),
				previousCamera,
				previousPerspective,
				cameraEntity
		);

		client.setCameraEntity(cameraEntity);
		client.options.setPerspective(Perspective.FIRST_PERSON);
		player.sendMessage(Text.translatable("status.mpsqcamera.view_enter"), true);
	}

	private static void handleActiveViewSession(
			MinecraftClient client,
			ClientPlayerEntity player,
			boolean usePressed,
			boolean escPressed
	) {
		if (!isSessionStillValid(client, player, activeViewSession)) {
			exitViewMode(client, true);
			return;
		}

		if (escPressed && !wasEscPressedLastTick) {
			exitViewMode(client, true);
			return;
		}

		if (usePressed && !wasUsePressedLastTick) {
			exitViewMode(client, true);
		}
	}

	private static boolean isSessionStillValid(MinecraftClient client, ClientPlayerEntity player, ViewSession session) {
		if (player.isRemoved() || !player.isAlive()) return false;
		if (client.world == null) return false;
		if (!client.world.getRegistryKey().equals(session.originDimension())) return false;
		if (client.currentScreen != null) return false;

		LocalScreenStore.LocalScreenData sourceScreen = LocalScreenStore.findByAnchor(session.sourceAnchor()).orElse(null);
		if (sourceScreen == null) return false;
		UUID cameraId = activeCameraId(sourceScreen);
		if (sourceScreen.inputType() != LocalScreenStore.ScreenInputType.CAMERA || cameraId == null) return false;

		LocalCameraStore.CameraData cameraScreen = LocalCameraStore.find(cameraId).orElse(null);
		if (cameraScreen == null || cameraScreen.position() == null) return false;

		Vec3d cameraPos = cameraScreen.position();
		session.cameraEntity().setPosition(cameraPos);
		return true;
	}

	private static void suppressMovementAndInteraction(MinecraftClient client) {
		client.options.forwardKey.setPressed(false);
		client.options.backKey.setPressed(false);
		client.options.leftKey.setPressed(false);
		client.options.rightKey.setPressed(false);
		client.options.jumpKey.setPressed(false);
		client.options.sneakKey.setPressed(false);
		client.options.sprintKey.setPressed(false);
		client.options.attackKey.setPressed(false);
		client.options.useKey.setPressed(false);
	}

	private static void lockPlayerPosition(ClientPlayerEntity player, Vec3d originPos) {
		player.setVelocity(Vec3d.ZERO);
		player.setPosition(originPos.x, originPos.y, originPos.z);
	}

	private static void exitViewMode(MinecraftClient client, boolean notify) {
		if (activeViewSession == null) return;
		ViewSession session = activeViewSession;
		activeViewSession = null;

		if (client.options != null) {
			client.options.setPerspective(session.previousPerspective());
		}

		Entity fallbackCamera = client.player == null ? null : client.player;
		client.setCameraEntity(session.previousCameraEntity() != null ? session.previousCameraEntity() : fallbackCamera);

		if (client.player != null) {
			lockPlayerPosition(client.player, session.originPos());
			if (notify) {
				client.player.sendMessage(Text.translatable("status.mpsqcamera.view_exit"), true);
			}
		}
	}

	private static Optional<LocalScreenStore.LocalScreenData> getScreenAtCrosshair(MinecraftClient client) {
		if (client.player == null) return Optional.empty();

		double reach = client.player.getBlockInteractionRange();
		Vec3d eye = client.player.getCameraPosVec(1.0f);
		Vec3d direction = client.player.getRotationVec(1.0f);

		// Prüfe alle Bildschirme via Ray-AABB-Test (keine Block-Abhängigkeit mehr)
		LocalScreenStore.LocalScreenData closest = null;
		double closestDist = Double.MAX_VALUE;

		for (LocalScreenStore.LocalScreenData screen : LocalScreenStore.getAllScreens()) {
			if (testRayBox(eye, direction, reach, screen.pos1(), screen.pos2())) {
				double dist = screen.pos1().getSquaredDistance(eye.x, eye.y, eye.z);
				if (dist < closestDist) {
					closestDist = dist;
					closest = screen;
				}
			}
		}

		if (closest != null) return Optional.of(closest);

		// Fallback: Legacy-Prüfung via Block-Treffer
		if (client.crosshairTarget instanceof BlockHitResult hit
				&& client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
			return LocalScreenStore.findByAnchor(hit.getBlockPos());
		}

		return Optional.empty();
	}

	/**
	 * Prüft ob ein Strahl (Ursprung + Richtung, max. Länge reach) die Bounding-Box
	 * eines Bildschirms (pos1..pos2) schneidet (Slab-Methode / AABB-Ray-Test).
	 */
	private static boolean testRayBox(Vec3d eye, Vec3d dir, double reach,
			BlockPos p1, BlockPos p2) {
		double minX = Math.min(p1.getX(), p2.getX());
		double minY = Math.min(p1.getY(), p2.getY());
		double minZ = Math.min(p1.getZ(), p2.getZ());
		double maxX = Math.max(p1.getX(), p2.getX()) + 1.0;
		double maxY = Math.max(p1.getY(), p2.getY()) + 1.0;
		double maxZ = Math.max(p1.getZ(), p2.getZ()) + 1.0;

		double tNear = 0.0;
		double tFar  = reach;

		// X-Achse
		if (Math.abs(dir.x) < 1e-9) {
			if (eye.x < minX || eye.x > maxX) return false;
		} else {
			double t1 = (minX - eye.x) / dir.x;
			double t2 = (maxX - eye.x) / dir.x;
			if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
			tNear = Math.max(tNear, t1);
			tFar  = Math.min(tFar, t2);
			if (tNear > tFar) return false;
		}

		// Y-Achse
		if (Math.abs(dir.y) < 1e-9) {
			if (eye.y < minY || eye.y > maxY) return false;
		} else {
			double t1 = (minY - eye.y) / dir.y;
			double t2 = (maxY - eye.y) / dir.y;
			if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
			tNear = Math.max(tNear, t1);
			tFar  = Math.min(tFar, t2);
			if (tNear > tFar) return false;
		}

		// Z-Achse
		if (Math.abs(dir.z) < 1e-9) {
			if (eye.z < minZ || eye.z > maxZ) return false;
		} else {
			double t1 = (minZ - eye.z) / dir.z;
			double t2 = (maxZ - eye.z) / dir.z;
			if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
			tNear = Math.max(tNear, t1);
			tFar  = Math.min(tFar, t2);
			if (tNear > tFar) return false;
		}

		return tFar > 0.0;
	}

	private static boolean isCameraAreaLoadedByAnyPlayer(MinecraftClient client, Vec3d cameraPos) {
		double maxDistSq = CAMERA_LOAD_RANGE * CAMERA_LOAD_RANGE;
		for (PlayerEntity worldPlayer : client.world.getPlayers()) {
			if (worldPlayer.squaredDistanceTo(cameraPos) <= maxDistSq) {
				return true;
			}
		}
		return false;
	}

	private static Vec3d toCameraPos(Vec3d basePos) {
		return basePos.add(0.0, CAMERA_EYE_HEIGHT, 0.0);
	}

	static BlockPos getSelectionPos1() {
		return selectionPos1;
	}

	static BlockPos getSelectionPos2Preview() {
		if (selectionPos1 == null) {
			return null;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
			return selectionPos1;
		}
		return ((BlockHitResult) client.crosshairTarget).getBlockPos();
	}

	private static void tryCreateScreen(MinecraftClient client, ClientPlayerEntity player) {
		if (!isHoldingToolItem(player)) return;

		if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
			MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Kein Block anvisiert.");
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
			selectionSide = hit.getSide();
			player.sendMessage(
					Text.translatable("gui.mpsqcamera.auswahl.pos1_gesetzt"), true);
			MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Pos 1 markiert: {}", selectionPos1);
		} else {
			// ── Zweiter Klick: Endpunkt → automatisch bestätigen & Menü öffnen
			BlockPos pos1 = selectionPos1;
			BlockPos pos2 = target.toImmutable();
			Direction side = selectionSide;
			selectionPos1 = null; // Auswahl-Modus beenden
			selectionSide = null;

			MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Pos 2 markiert: {} → Erstell-Menü öffnen", pos2);
			client.setScreen(new BildschirmErstellenScreen(pos1, pos2, side));
		}
	}

	private static boolean isHoldingToolItem(ClientPlayerEntity player) {
		Item toolItem = getConfiguredToolItem();
		return player.getMainHandStack().isOf(toolItem) || player.getOffHandStack().isOf(toolItem);
	}

	private static Item getConfiguredToolItem() {
		Identifier identifier = Identifier.tryParse(ModConfig.toolItemId);
		if (identifier != null && Registries.ITEM.containsId(identifier)) {
			return Registries.ITEM.get(identifier);
		}
		return Items.INK_SAC;
	}

	private record ViewSession(
			BlockPos sourceAnchor,
			UUID cameraId,
			Vec3d originPos,
			RegistryKey<World> originDimension,
			Entity previousCameraEntity,
			Perspective previousPerspective,
			ArmorStandEntity cameraEntity
	) {}
}
