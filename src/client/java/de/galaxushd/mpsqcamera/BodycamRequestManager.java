package de.galaxushd.mpsqcamera;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

/** Polls the small shared request inbox while a player is in a world. */
public final class BodycamRequestManager {
    private static int ticksUntilPoll;
    private static boolean requestInFlight;

    private BodycamRequestManager() { }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(BodycamRequestManager::tick);
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
