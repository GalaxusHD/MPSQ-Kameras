package de.galaxushd.mpsqcamera;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MpsqCameraClient implements ClientModInitializer {
    public static final String MOD_ID = "mpsqcamera";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[MPSQ Kameras] Client mod initialized.");
        ScreenCreationManager.initialize();
        CameraCreationManager.initialize();
        BodycamRequestManager.initialize();
        CameraHologramManager.initialize();
        SelectionRenderer.initialize();
        ScreenRenderer.initialize();
        CinemaBrowserManager.initialize();
        MpsqApiClient.initialize().thenCompose(ignored -> MpsqApiClient.refreshCameras())
        .thenCompose(ignored -> ScreenSyncManager.refresh()).exceptionally(error -> {
            LOGGER.warn("MPSQ-API ist momentan nicht erreichbar", error);
            return null;
        });
    }
}
