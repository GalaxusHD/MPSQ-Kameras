package de.galaxushd.mpsqcamera;

import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Stable camera-frame transport.
 *
 * <p>Only Minecraft's already completed normal render is captured. This is
 * intentional: rendering the world a second time while Minecraft is drawing
 * caused camera jumps, hand artefacts and broken freecam movement.</p>
 */
public final class RemoteCameraFrameManager {
    private static final long FRAME_INTERVAL_MS = 100L;
    private static final int STREAM_WIDTH = 480;
    private static final int STREAM_HEIGHT = 270;
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final Map<UUID, Identifier> TEXTURE_IDS = new HashMap<>();
    private static final Map<UUID, NativeImageBackedTexture> TEXTURES = new HashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST_MS = new HashMap<>();
    private static final Set<UUID> OWN_BODYCAMS = new HashSet<>();

    private static UUID providerCamera;
    private static long lastPublishMs;
    private static long lastBodycamRefreshMs;
    private static boolean publishing;
    private static boolean bodycamRefreshInFlight;

    private RemoteCameraFrameManager() { }

    public static void initialize() {
        WorldRenderEvents.END.register(context -> captureProviderFrame());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> clear());
    }

    /** Starts transmission from the camera view the player explicitly entered. */
    public static void startPublishing(UUID cameraId) {
        providerCamera = cameraId;
        lastPublishMs = 0L;
    }

    /** Stops static-camera transmission immediately after leaving the view. */
    public static void stopPublishing() {
        providerCamera = null;
    }

    public static Identifier texture(UUID cameraId) {
        if (cameraId == null) return null;
        requestLatestFrame(cameraId);
        return TEXTURE_IDS.get(cameraId);
    }

    private static void captureProviderFrame() {
        MinecraftClient client = MinecraftClient.getInstance();
        refreshMyBodycams();
        if ((providerCamera == null && OWN_BODYCAMS.isEmpty()) || publishing || client.getFramebuffer() == null) return;
        long now = System.currentTimeMillis();
        if (now - lastPublishMs < FRAME_INTERVAL_MS) return;
        lastPublishMs = now;
        publishing = true;

        ScreenshotRecorder.takeScreenshot(client.getFramebuffer(), 1, image -> {
            byte[] png;
            try {
                NativeImage streamImage = resizeForStream(image);
                try {
                    png = encode(streamImage);
                } finally {
                    streamImage.close();
                }
            } catch (IOException exception) {
                image.close();
                publishing = false;
                MpsqCameraClient.LOGGER.warn("Kamera-Bild konnte nicht erzeugt werden", exception);
                return;
            }
            image.close();
            Set<UUID> targets;
            synchronized (OWN_BODYCAMS) {
                targets = new HashSet<>(OWN_BODYCAMS);
            }
            if (providerCamera != null) targets.add(providerCamera);
            if (targets.isEmpty()) {
                publishing = false;
                return;
            }
            CompletableFuture<?>[] uploads = targets.stream()
                    .map(cameraId -> MpsqApiClient.postCameraFrame(cameraId, png))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(uploads).whenComplete((ignored, error) -> {
                publishing = false;
                if (error != null) MpsqCameraClient.LOGGER.debug("Kamera-Bild konnte nicht hochgeladen werden", error);
            });
        });
    }

    private static void refreshMyBodycams() {
        if (!MpsqApiClient.isReady() || bodycamRefreshInFlight) return;
        long now = System.currentTimeMillis();
        if (now - lastBodycamRefreshMs < 10_000L) return;
        lastBodycamRefreshMs = now;
        bodycamRefreshInFlight = true;
        MpsqApiClient.loadMyBodycamIds().whenComplete((ids, error) -> {
            bodycamRefreshInFlight = false;
            if (error == null && ids != null) {
                synchronized (OWN_BODYCAMS) {
                    OWN_BODYCAMS.clear();
                    OWN_BODYCAMS.addAll(ids);
                }
            }
        });
    }

    private static byte[] encode(NativeImage image) throws IOException {
        Path file = Files.createTempFile("mpsq-camera-frame-", ".png");
        try {
            image.writeTo(file);
            return Files.readAllBytes(file);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static NativeImage resizeForStream(NativeImage source) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        double targetRatio = (double) STREAM_WIDTH / STREAM_HEIGHT;
        double sourceRatio = (double) sourceWidth / sourceHeight;
        int cropX = 0;
        int cropY = 0;
        int cropWidth = sourceWidth;
        int cropHeight = sourceHeight;
        if (sourceRatio > targetRatio) {
            cropWidth = (int) Math.round(sourceHeight * targetRatio);
            cropX = (sourceWidth - cropWidth) / 2;
        } else if (sourceRatio < targetRatio) {
            cropHeight = (int) Math.round(sourceWidth / targetRatio);
            cropY = (sourceHeight - cropHeight) / 2;
        }
        NativeImage target = new NativeImage(STREAM_WIDTH, STREAM_HEIGHT, false);
        source.resizeSubRectTo(cropX, cropY, cropWidth, cropHeight, target);
        return target;
    }

    private static void requestLatestFrame(UUID cameraId) {
        long now = System.currentTimeMillis();
        if (now - LAST_REQUEST_MS.getOrDefault(cameraId, 0L) < FRAME_INTERVAL_MS) return;
        LAST_REQUEST_MS.put(cameraId, now);
        MpsqApiClient.get("/cameras/" + cameraId + "/frame")
                .thenCompose(RemoteCameraFrameManager::downloadFrame)
                .thenAccept(image -> MinecraftClient.getInstance().execute(() -> install(cameraId, image)))
                .exceptionally(error -> {
                    MinecraftClient.getInstance().execute(() -> removeTexture(cameraId));
                    return null;
                });
    }

    private static CompletableFuture<NativeImage> downloadFrame(JsonElement result) {
        if (result == null || !result.isJsonObject() || !result.getAsJsonObject().has("url")) {
            return CompletableFuture.failedFuture(new IOException("Kamera ist offline"));
        }
        String url = result.getAsJsonObject().get("url").getAsString();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        return HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(HttpResponse::body)
                .thenApply(bytes -> {
                    try {
                        return NativeImage.read(new ByteArrayInputStream(bytes));
                    } catch (IOException exception) {
                        throw new IllegalStateException(exception);
                    }
                });
    }

    private static void install(UUID cameraId, NativeImage image) {
        NativeImageBackedTexture texture = TEXTURES.get(cameraId);
        if (texture == null) {
            texture = new NativeImageBackedTexture(() -> "mpsq-camera-" + cameraId, image);
            Identifier id = Identifier.of(MpsqCameraClient.MOD_ID, "remote_camera/" + cameraId);
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
            texture.upload();
            TEXTURES.put(cameraId, texture);
            TEXTURE_IDS.put(cameraId, id);
        } else {
            texture.setImage(image);
            texture.upload();
        }
    }

    private static void removeTexture(UUID cameraId) {
        NativeImageBackedTexture texture = TEXTURES.remove(cameraId);
        if (texture != null) texture.close();
        TEXTURE_IDS.remove(cameraId);
    }

    private static void clear() {
        stopPublishing();
        TEXTURES.values().forEach(NativeImageBackedTexture::close);
        TEXTURES.clear();
        TEXTURE_IDS.clear();
        LAST_REQUEST_MS.clear();
        OWN_BODYCAMS.clear();
    }
}
