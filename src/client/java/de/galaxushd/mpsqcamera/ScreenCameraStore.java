package de.galaxushd.mpsqcamera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Ordered camera assignments per screen. */
public final class ScreenCameraStore {
    private static final Map<UUID, List<UUID>> CAMERAS = new HashMap<>();
    private static final Map<UUID, Integer> ACTIVE = new HashMap<>();
    private ScreenCameraStore() { }
    public static void put(UUID screenId, List<UUID> cameras) { CAMERAS.put(screenId, List.copyOf(cameras)); ACTIVE.putIfAbsent(screenId, 0); }
    public static UUID active(UUID screenId) { List<UUID> list = CAMERAS.getOrDefault(screenId, List.of()); if (list.isEmpty()) return null; return list.get(Math.floorMod(ACTIVE.getOrDefault(screenId, 0), list.size())); }
    public static UUID next(UUID screenId, int direction) { List<UUID> list = CAMERAS.getOrDefault(screenId, List.of()); if (list.isEmpty()) return null; int index = Math.floorMod(ACTIVE.getOrDefault(screenId, 0) + direction, list.size()); ACTIVE.put(screenId, index); return list.get(index); }
    public static void clear() { CAMERAS.clear(); ACTIVE.clear(); }
}
