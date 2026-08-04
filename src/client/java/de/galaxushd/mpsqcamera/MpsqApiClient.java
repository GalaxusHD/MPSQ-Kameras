package de.galaxushd.mpsqcamera;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;

/** The client only keeps its own random token. No Supabase secret is ever stored here. */
public final class MpsqApiClient {
    public static final String API_URL = "https://hbikjzzkxsvjoqnedbmm.supabase.co/functions/v1/mpsq-api";
    private static final Path TOKEN_FILE = FabricLoader.getInstance().getConfigDir().resolve("mpsqcamera-token.txt");
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final Gson GSON = new Gson();
    private static String token;

    private MpsqApiClient() { }

    public static CompletableFuture<Void> initialize() {
        token = readToken();
        if (token != null && !token.isBlank()) return CompletableFuture.completedFuture(null);
        JsonObject body = new JsonObject();
        body.addProperty("displayName", "Minecraft Client");
        return request("POST", "/register", body, false).thenAccept(json -> {
            token = json.getAsJsonObject().get("token").getAsString();
            try {
                Files.createDirectories(TOKEN_FILE.getParent());
                Files.writeString(TOKEN_FILE, token, StandardCharsets.UTF_8);
            } catch (IOException exception) {
                throw new IllegalStateException("MPSQ-Zugang konnte nicht gespeichert werden", exception);
            }
        });
    }

    public static CompletableFuture<JsonElement> get(String path) { return request("GET", path, null, true); }
    public static CompletableFuture<JsonElement> post(String path, JsonObject body) { return request("POST", path, body, true); }
    public static CompletableFuture<JsonElement> patch(String path, JsonObject body) { return request("PATCH", path, body, true); }
    public static CompletableFuture<JsonElement> delete(String path) { return request("DELETE", path, null, true); }

    public static CompletableFuture<List<LocalCameraStore.CameraData>> loadCameras() {
        return get("/cameras").thenApply(json -> {
            JsonArray rows = json.getAsJsonArray();
            List<LocalCameraStore.CameraData> cameras = new ArrayList<>();
            for (JsonElement element : rows) {
                JsonObject row = element.getAsJsonObject();
                LocalCameraStore.CameraKind kind = "BODYCAM".equals(row.get("kind").getAsString())
                        ? LocalCameraStore.CameraKind.BODYCAM : LocalCameraStore.CameraKind.STATIC;
                Vec3d position = row.get("x").isJsonNull() ? null : new Vec3d(row.get("x").getAsDouble(), row.get("y").getAsDouble(), row.get("z").getAsDouble());
                UUID wearer = row.has("body_owner_id") && !row.get("body_owner_id").isJsonNull() ? UUID.fromString(row.get("body_owner_id").getAsString()) : null;
                cameras.add(new LocalCameraStore.CameraData(UUID.fromString(row.get("id").getAsString()), row.get("name").getAsString(), kind,
                        row.get("dimension").getAsString(), position, row.get("yaw").getAsFloat(), row.get("pitch").getAsFloat(), wearer));
            }
            return cameras;
        });
    }

    private static CompletableFuture<JsonElement> request(String method, String path, JsonObject body, boolean authenticated) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(API_URL + path))
                .timeout(Duration.ofSeconds(15)).header("Accept", "application/json");
        if (authenticated) {
            if (token == null || token.isBlank()) return CompletableFuture.failedFuture(new IllegalStateException("MPSQ ist nicht verbunden"));
            request.header("x-mpsq-token", token);
        }
        if (body == null) request.method(method, HttpRequest.BodyPublishers.noBody());
        else request.header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(GSON.toJson(body)));
        return HTTP.sendAsync(request.build(), HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            JsonElement json = JsonParser.parseString(response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String message = json.isJsonObject() && json.getAsJsonObject().has("error") ? json.getAsJsonObject().get("error").getAsString() : response.body();
                throw new IllegalStateException(message);
            }
            return json;
        });
    }

    private static String readToken() {
        try { return Files.exists(TOKEN_FILE) ? Files.readString(TOKEN_FILE, StandardCharsets.UTF_8).trim() : null; }
        catch (IOException exception) { MpsqCameraClient.LOGGER.warn("MPSQ-Zugang konnte nicht gelesen werden", exception); return null; }
    }
}

