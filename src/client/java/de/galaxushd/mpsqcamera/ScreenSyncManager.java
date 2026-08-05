package de.galaxushd.mpsqcamera;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Fetches screens and their server-only metadata in one request. */
public final class ScreenSyncManager {
    private ScreenSyncManager() { }
    public static CompletableFuture<Void> refresh() {
        return MpsqApiClient.get("/screens").thenAccept(json -> {
            List<LocalScreenStore.LocalScreenData> screens = new ArrayList<>(); Map<UUID, String> codes = new HashMap<>(); Map<UUID, LocalScreenStore.LocalGroupData> groups = new HashMap<>(); Set<UUID> owned = new HashSet<>();
            for (JsonElement item : json.getAsJsonArray()) {
                JsonObject row = item.getAsJsonObject(); UUID id = UUID.fromString(row.get("id").getAsString());
                BlockPos p1 = new BlockPos(row.get("pos1_x").getAsInt(), row.get("pos1_y").getAsInt(), row.get("pos1_z").getAsInt()); BlockPos p2 = new BlockPos(row.get("pos2_x").getAsInt(), row.get("pos2_y").getAsInt(), row.get("pos2_z").getAsInt());
                UUID groupId = row.has("group_id") && !row.get("group_id").isJsonNull() ? UUID.fromString(row.get("group_id").getAsString()) : null;
                codes.put(id, row.get("activation_code").getAsString()); if (row.has("is_owner") && row.get("is_owner").getAsBoolean()) owned.add(id);
                if (row.has("front") && !row.get("front").isJsonNull()) ScreenAccessStore.setFront(id, row.get("front").getAsString());
                if (groupId != null && row.has("mpsq_screen_groups") && row.get("mpsq_screen_groups").isJsonObject()) { JsonObject group = row.getAsJsonObject("mpsq_screen_groups"); groups.put(id, new LocalScreenStore.LocalGroupData(groupId, group.get("activation_code").getAsString())); }
                LocalScreenStore.ScreenInputType mode = "CAMERA".equals(row.get("mode").getAsString()) ? LocalScreenStore.ScreenInputType.CAMERA : LocalScreenStore.ScreenInputType.LINK;
                screens.add(new LocalScreenStore.LocalScreenData(id, p1, p2, row.get("name").getAsString(), new Vec3d(p1.getX(), p1.getY(), p1.getZ()), mode, row.get("cinema_url").getAsString(), null, groupId));
            }
            MinecraftClient.getInstance().execute(() -> { LocalScreenStore.replaceAll(screens); ScreenAccessStore.replace(codes, groups, owned); });
        });
    }
}
