package de.galaxushd.mpsqcamera;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/** Prevents stationary cameras from rendering from inside solid terrain. */
public final class CameraSafety {
    private CameraSafety() { }

    /**
     * Returns true only if this client can verify that a stationary camera is
     * inside a block with a collision shape. Unloaded chunks remain OFFLINE,
     * not falsely marked as blocked.
     */
    public static boolean isStaticCameraBlocked(MinecraftClient client, LocalCameraStore.CameraData camera) {
        if (client == null || client.world == null || camera == null
                || camera.kind() != LocalCameraStore.CameraKind.STATIC
                || camera.position() == null
                || !client.world.getRegistryKey().getValue().toString().equals(camera.dimension())) {
            return false;
        }
        BlockPos pos = BlockPos.ofFloored(camera.position());
        if (!client.world.isChunkLoaded(pos)) return false;
        BlockState state = client.world.getBlockState(pos);
        return !state.getCollisionShape(client.world, pos).isEmpty();
    }
}
