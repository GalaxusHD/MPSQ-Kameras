package de.galaxushd.mpsqcamera;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/** Creates a client-only camera marker in front of the player's head. */
public final class CameraCreationManager {
    private static KeyBinding createCameraKey;

    private CameraCreationManager() { }

    public static void initialize() {
        createCameraKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mpsqcamera.create_camera", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C,
                "category.mpsqcamera.main"));
        ClientTickEvents.END_CLIENT_TICK.register(CameraCreationManager::tick);
    }

    private static void tick(MinecraftClient client) {
        while (createCameraKey.wasPressed()) {
            if (client.player == null || client.world == null || client.currentScreen != null) return;
            Vec3d position = client.player.getCameraPosVec(1.0F).add(client.player.getRotationVec(1.0F).multiply(0.65));
            String name = "Kamera " + (LocalCameraStore.getAll().size() + 1);
            LocalCameraStore.CameraData local = LocalCameraStore.createStatic(name, client.world.getRegistryKey().getValue().toString(), position, client.player.getYaw(), client.player.getPitch());
            publish(local);
            client.player.sendMessage(Text.translatable("status.mpsqcamera.camera_created"), true);
        }
    }

    private static void publish(LocalCameraStore.CameraData camera) {
        JsonObject body = new JsonObject();
        body.addProperty("name", camera.name()); body.addProperty("kind", "STATIC"); body.addProperty("dimension", camera.dimension());
        body.addProperty("x", camera.position().x); body.addProperty("y", camera.position().y); body.addProperty("z", camera.position().z);
        body.addProperty("yaw", camera.yaw()); body.addProperty("pitch", camera.pitch());
        MpsqApiClient.post("/cameras", body).exceptionally(error -> { MpsqCameraClient.LOGGER.warn("Kamera konnte nicht synchronisiert werden", error); return null; });
    }
}
