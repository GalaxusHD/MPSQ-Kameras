package de.galaxushd.mpsqcamera;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Client-only entities. They are never sent to a Minecraft server. */
public final class CameraHologramManager {
    private static final Map<UUID, ArmorStandEntity> HOLOGRAMS = new HashMap<>();
    private static int nextEntityId = -2_000_000_000;

    private CameraHologramManager() { }

    public static void initialize() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> clear());
    }

    public static void show(LocalCameraStore.CameraData camera) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || camera.position() == null || HOLOGRAMS.containsKey(camera.id())) return;
        ArmorStandEntity stand = new ArmorStandEntity(client.world, camera.position().x, camera.position().y - 1.55, camera.position().z);
        stand.setId(nextEntityId++);
        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setSmall(true);
        stand.setYaw(camera.yaw());
        stand.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.PLAYER_HEAD));
        client.world.addEntity(stand);
        HOLOGRAMS.put(camera.id(), stand);
    }

    public static void remove(UUID cameraId) {
        ArmorStandEntity stand = HOLOGRAMS.remove(cameraId);
        if (stand != null) stand.discard();
    }

    public static void clear() {
        for (ArmorStandEntity stand : HOLOGRAMS.values()) stand.discard();
        HOLOGRAMS.clear();
    }
}
