package de.galaxushd.mpsqcamera;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ScreenCameraStore {
    private static final Map<UUID, List<UUID>> CAMERAS = new HashMap<>();
    private static final Map<UUID, Integer> ACTIVE = new HashMap<>();

    private ScreenCameraStore() {
    }

    public static void put(UUID screenId, List<UUID> cameras) {
        CAMERAS.put(screenId, List.copyOf(cameras));
        ACTIVE.putIfAbsent(screenId, 0);
    }

    public static boolean hasCameras(UUID screenId) {
        return !CAMERAS.getOrDefault(screenId, List.of()).isEmpty();
    }

    public static UUID active(UUID screenId) {
        List<UUID> cameras = CAMERAS.getOrDefault(screenId, List.of());

        if (cameras.isEmpty()) {
            return null;
        }

        int index = Math.floorMod(
                ACTIVE.getOrDefault(screenId, 0),
                cameras.size()
        );

        return cameras.get(index);
    }

    public static UUID next(UUID screenId, int direction) {
        List<UUID> cameras = CAMERAS.getOrDefault(screenId, List.of());

        if (cameras.isEmpty()) {
            return null;
        }

        int index = Math.floorMod(
                ACTIVE.getOrDefault(screenId, 0) + direction,
                cameras.size()
        );

        ACTIVE.put(screenId, index);
        return cameras.get(index);
    }

    public static void clear() {
        CAMERAS.clear();
        ACTIVE.clear();
    }
}
