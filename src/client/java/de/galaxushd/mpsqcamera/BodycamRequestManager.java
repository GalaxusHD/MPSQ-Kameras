package de.galaxushd.mpsqcamera;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

/** Polls the small shared request inbox while a player is in a world. */
public final class BodycamRequestManager {
    private static int ticksUntilPoll;
    private static boolean requestInFlight;

    private BodycamRequestManager() { }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(BodycamRequestManager::tick);
    }

    /** Sends a bodycam request for the player currently looked at by the requester. */
    public static void request(MinecraftClient client, PlayerEntity target) {
        if (client.player == null || target == client.player) return;
        // The visible name can contain a server rank such as "ULTRA". The
        // game profile name is the actual Minecraft name saved by MPSQ Team.
        String playerName = target.getGameProfile().getName().trim();
        if (playerName.isBlank()) return;

        JsonObject body = new JsonObject();
        body.addProperty("targetDisplayName", playerName);
        MpsqApiClient.post("/bodycam-requests", body).whenComplete((ignored, error) -> client.execute(() -> {
            if (client.player == null) return;
            if (error == null) {
                client.player.sendMessage(Text.translatable("gui.mpsqcamera.bodycam.sent", playerName), true);
                return;
            }
            // Never expose an internal API path or translation key in the HUD.
            // The user only needs the clear, localised failure message.
            client.player.sendMessage(Text.translatable("gui.mpsqcamera.bodycam.failed"), true);
        }));
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.currentScreen != null || !MpsqApiClient.isReady() || requestInFlight) return;
        if (--ticksUntilPoll > 0) return;
        ticksUntilPoll = 60;
        requestInFlight = true;
        MpsqApiClient.get("/bodycam-requests").whenComplete((json, error) -> client.execute(() -> {
            requestInFlight = false;
            if (error != null || json == null || !json.isJsonArray() || json.getAsJsonArray().isEmpty()) return;
            JsonElement first = json.getAsJsonArray().get(0);
            if (!first.isJsonObject()) return;
            JsonObject request = first.getAsJsonObject();
            client.setScreen(new BodycamRequestScreen(request.get("id").getAsString(), request.get("requesterName").getAsString()));
        }));
    }
}
