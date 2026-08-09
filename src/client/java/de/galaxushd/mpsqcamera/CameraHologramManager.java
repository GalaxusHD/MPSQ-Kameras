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
        // Body yaw is the world direction. Giving the head the same yaw again
        // rotates it a second time and makes the marker point the wrong way.
        stand.setHeadRotation(new EulerAngle(camera.pitch(), 0.0F, 0.0F));
    }

    public static void remove(UUID cameraId) {
        ArmorStandEntity stand = HOLOGRAMS.remove(cameraId);
        if (stand != null) stand.discard();
    }

    public static void clear() {
        for (ArmorStandEntity stand : HOLOGRAMS.values()) stand.discard();
        HOLOGRAMS.clear();
    }

    private static ItemStack createCameraHead() {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        GameProfile profile = new GameProfile(UUID.randomUUID(), "MPSQ Camera");
        profile.getProperties().put("textures", new Property("textures", CAMERA_HEAD_TEXTURE));
        head.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
        return head;
    }
}
