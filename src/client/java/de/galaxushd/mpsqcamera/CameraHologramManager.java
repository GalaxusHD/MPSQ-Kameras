package de.galaxushd.mpsqcamera;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Client-only entities. They are never sent to a Minecraft server. */
public final class CameraHologramManager {
    private static final String CAMERA_HEAD_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGZiZWQzNzI1YzA1Mjc0OGI2NWJlODQ3ZTE5NDJmNzU5YzNhOGRhMDY0OWY4MDUwODdiMDM2Nzk2NDE2MWI0ZCJ9fX0=";
    private static final Map<UUID, ArmorStandEntity> HOLOGRAMS = new HashMap<>();
    private static int nextEntityId = -2_000_000_000;
	private static boolean hiddenForCameraView;

    private CameraHologramManager() { }

    public static void initialize() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> clear());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                client.execute(() -> LocalCameraStore.getAll().forEach(CameraHologramManager::show)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
    }

    public static void show(LocalCameraStore.CameraData camera) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || camera.position() == null || HOLOGRAMS.containsKey(camera.id())) return;
        ArmorStandEntity stand = new ArmorStandEntity(client.world, camera.position().x, camera.position().y - 1.55, camera.position().z);
        stand.setId(nextEntityId++);
        stand.setInvisible(true);
        stand.setNoGravity(true);
        applyTransform(stand, camera);
        stand.equipStack(EquipmentSlot.HEAD, createCameraHead());
        client.world.addEntity(stand);
        HOLOGRAMS.put(camera.id(), stand);
    }

    /** Applies a saved move/rotation immediately to an already visible client-only hologram. */
    public static void update(LocalCameraStore.CameraData camera) {
        ArmorStandEntity stand = HOLOGRAMS.get(camera.id());
        if (stand == null) { show(camera); return; }
        if (camera.position() == null) { remove(camera.id()); return; }
        applyTransform(stand, camera);
    }

    private static void applyTransform(ArmorStandEntity stand, LocalCameraStore.CameraData camera) {
        stand.setPosition(camera.position().x, camera.position().y - 1.55, camera.position().z);
        stand.setYaw(camera.yaw());
        stand.setPitch(camera.pitch());
        // The armor stand already owns the horizontal direction through setYaw.
        // Applying the same yaw a second time to its head rotates the camera
        // marker twice (most visible when it faces along the Z axis).
        stand.setHeadRotation(new EulerAngle(camera.pitch(), 0.0F, 0.0F));
    }

    public static void remove(UUID cameraId) {
        ArmorStandEntity stand = HOLOGRAMS.remove(cameraId);
        if (stand != null) stand.discard();
    }

    public static void clear() {
        for (ArmorStandEntity stand : HOLOGRAMS.values()) stand.discard();
        HOLOGRAMS.clear();
		hiddenForCameraView = false;
    }

	/** Hides local camera markers while their world is used as a camera view. */
	public static void hideForCameraView() {
		if (hiddenForCameraView) return;
		hiddenForCameraView = true;
		for (ArmorStandEntity stand : HOLOGRAMS.values()) {
			stand.setInvisible(true);
			stand.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
		}
	}

	/** Restores all local camera markers after leaving the camera view. */
	public static void showAfterCameraView() {
		if (!hiddenForCameraView) return;
		hiddenForCameraView = false;
		for (LocalCameraStore.CameraData camera : LocalCameraStore.getAll()) {
			ArmorStandEntity stand = HOLOGRAMS.get(camera.id());
			if (stand == null || camera.position() == null) continue;
			applyTransform(stand, camera);
			stand.equipStack(EquipmentSlot.HEAD, createCameraHead());
		}
	}

    private static ItemStack createCameraHead() {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        GameProfile profile = new GameProfile(UUID.randomUUID(), "MPSQ Camera");
        profile.getProperties().put("textures", new Property("textures", CAMERA_HEAD_TEXTURE));
        head.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
        return head;
    }
}
