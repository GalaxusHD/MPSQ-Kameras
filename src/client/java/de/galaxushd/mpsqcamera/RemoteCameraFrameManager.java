package de.galaxushd.mpsqcamera;

import com.google.gson.JsonElement;
import de.galaxushd.mpsqcamera.mixin.client.GameRendererInvoker;
import de.galaxushd.mpsqcamera.mixin.client.MinecraftClientAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Sends a separate, fixed view for every locally loaded static-camera chunk.
 *
 * <p>The pass deliberately renders into its own framebuffer; it never steals
 * or screenshots the player's regular view. It is limited to 480x270 and one
 * pass per 100 ms, so the normal game render remains the priority.</p>
 */
public final class RemoteCameraFrameManager {
    public static final long FRAME_INTERVAL_MS = 100L;
    public static final int STREAM_WIDTH = 480;
    public static final int STREAM_HEIGHT = 270;

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final Map<UUID, Identifier> TEXTURE_IDS = new HashMap<>();
    private static final Map<UUID, NativeImageBackedTexture> TEXTURES = new HashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST_MS = new HashMap<>();
    private static final Set<UUID> OWN_BODYCAMS = new HashSet<>();

    private static long lastRenderMs;
    private static long lastBodycamRefreshMs;
    private static boolean bodycamRefreshInFlight;
    private static boolean renderingCameraPass;
    private static boolean uploadInFlight;
    private static int nextCameraIndex;
    private static SimpleFramebuffer cameraFramebuffer;

    private RemoteCameraFrameManager() { }

    public static void initialize() {
        WorldRenderEvents.END.register(context -> renderLoadedCamera());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> clear());
    }

    /** Kept for existing view-mode callers; streaming is now automatic. */
    public static void startPublishing(UUID ignoredCameraId) { }

    /** Kept for existing view-mode callers; leaving a view never disables a locally loaded camera. */
    public static void stopPublishing() { }

    /** Returns the newest remote texture, or null while the camera is offline. */
    public static Identifier texture(UUID cameraId) {
        if (cameraId == null) return null;
        requestLatestFrame(cameraId);
        return TEXTURE_IDS.get(cameraId);
    }

    private static void renderLoadedCamera() {
        if (renderingCameraPass || uploadInFlight) return;
        MinecraftClient client = MinecraftClient.getInstance();
        refreshMyBodycams();
        if (client.world == null || client.player == null || client.getCameraEntity() == null || !MpsqApiClient.isReady()) return;

        long now = System.currentTimeMillis();
        if (now - lastRenderMs < FRAME_INTERVAL_MS) return;

        List<LocalCameraStore.CameraData> candidates = loadedStaticCameras(client);
        if (candidates.isEmpty()) return;
        LocalCameraStore.CameraData camera = candidates.get(Math.floorMod(nextCameraIndex++, candidates.size()));
        lastRenderMs = now;
        renderCameraPass(client, camera);
    }

    private static List<LocalCameraStore.CameraData> loadedStaticCameras(MinecraftClient client) {
        String dimension = client.world.getRegistryKey().getValue().toString();
        List<LocalCameraStore.CameraData> cameras = new ArrayList<>();
        for (LocalCameraStore.CameraData camera : LocalCameraStore.getAll()) {
            if (camera.kind() != LocalCameraStore.CameraKind.STATIC || camera.position() == null || !dimension.equals(camera.dimension())) continue;
            int chunkX = ((int) Math.floor(camera.position().x)) >> 4;
            int chunkZ = ((int) Math.floor(camera.position().z)) >> 4;
            if (client.world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) cameras.add(camera);
        }
        return cameras;
    }

    private static void renderCameraPass(MinecraftClient client, LocalCameraStore.CameraData camera) {
        Framebuffer playerFramebuffer = client.getFramebuffer();
        Entity playerCamera = client.getCameraEntity();
        Perspective playerPerspective = client.options.getPerspective();
        ArmorStandEntity cameraEntity = new ArmorStandEntity(client.world, camera.position().x, camera.position().y, camera.position().z);
        cameraEntity.setNoGravity(true);
        cameraEntity.setInvisible(true);
        cameraEntity.setYaw(camera.yaw());
        cameraEntity.setPitch(camera.pitch());

        try {
            ensureCameraFramebuffer();
            renderingCameraPass = true;
            client.setCameraEntity(cameraEntity);
            client.options.setPerspective(Perspective.FIRST_PERSON);
            ((MinecraftClientAccessor) client).mpsq$setFramebuffer(cameraFramebuffer);
            ((GameRendererInvoker) ((MinecraftClientAccessor) client).mpsq$getGameRenderer())
                    .mpsq$renderWorld(client.getRenderTickCounter());

            uploadInFlight = true;
            ScreenshotRecorder.takeScreenshot(cameraFramebuffer, 1, image -> uploadRenderedFrame(camera.id(), image));
        } catch (Throwable error) {
            MpsqCameraClient.LOGGER.warn("[MPSQ] Separater Kamera-Renderdurchlauf fehlgeschlagen", error);
            uploadInFlight = false;
        } finally {
            ((MinecraftClientAccessor) client).mpsq$setFramebuffer(playerFramebuffer);
            client.setCameraEntity(playerCamera);
            client.options.setPerspective(playerPerspective);
            renderingCameraPass = false;
        }
    }

    private static void ensureCameraFramebuffer() {
        if (cameraFramebuffer == null) {
            cameraFramebuffer = new SimpleFramebuffer("mpsq_camera", STREAM_WIDTH, STREAM_HEIGHT, true);
        }
    }

    private static void uploadRenderedFrame(UUID cameraId, NativeImage image) {
        byte[] png;
        try {
            png = encode(image);
        } catch (IOException error) {
            MpsqCameraClient.LOGGER.warn("[MPSQ] Kamera-Bild konnte nicht erzeugt werden", error);
            image.close();
            uploadInFlight = false;
            return;
        }
        image.close();
        MpsqApiClient.postCameraFrame(cameraId, png).whenComplete((ignored, error) -> {
            uploadInFlight = false;
            if (error != null) MpsqCameraClient.LOGGER.debug("[MPSQ] Kamera-Bild konnte nicht hochgeladen werden", error);
        });
    }

    /** A wearer who accepted a bodycam continues to publish their own first-person view. */
    private static void refreshMyBodycams() {
        if (!MpsqApiClient.isReady() || bodycamRefreshInFlight) return;
        long now = System.currentTimeMillis();
        if (now - lastBodycamRefreshMs < 10_000L) return;
        lastBodycamRefreshMs = now;
        bodycamRefreshInFlight = true;
        MpsqApiClient.loadMyBodycamIds().whenComplete((ids, error) -> {
            bodycamRefreshInFlight = false;
            if (error == null && ids != null) synchronized (OWN_BODYCAMS) {
                OWN_BODYCAMS.clear();
                OWN_BODYCAMS.addAll(ids);
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
                    try { return NativeImage.read(new ByteArrayInputStream(bytes)); }
                    catch (IOException exception) { throw new IllegalStateException(exception); }
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
        TEXTURES.values().forEach(NativeImageBackedTexture::close);
        TEXTURES.clear();
        TEXTURE_IDS.clear();
        LAST_REQUEST_MS.clear();
        OWN_BODYCAMS.clear();
        if (cameraFramebuffer != null) cameraFramebuffer.delete();
        cameraFramebuffer = null;
    }
}
