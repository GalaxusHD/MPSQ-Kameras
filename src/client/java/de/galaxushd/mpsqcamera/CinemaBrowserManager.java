package de.galaxushd.mpsqcamera;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFBrowser;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Owns the off-screen MCEF browsers used as cinema-screen textures.
 * A browser exists only while a cinema screen is playing, keeping CPU and RAM use bounded.
 */
public final class CinemaBrowserManager {
    private static final int BROWSER_WIDTH = 1280;
    private static final int BROWSER_HEIGHT = 720;
    private static final int REFRESH_INTERVAL_TICKS = 20;
    private static final int STATE_POLL_INTERVAL_TICKS = 100;
    private static final Map<UUID, BrowserSession> BROWSERS = new HashMap<>();
    private static final Set<UUID> FAILED_BROWSERS = new HashSet<>();
    private static int ticks;
    private static volatile boolean refreshInProgress;

    private CinemaBrowserManager() { }

    public static void initialize() {
        try {
            if (!MCEF.isInitialized() && !MCEF.initialize()) {
                MpsqCameraClient.LOGGER.warn("MCEF konnte nicht initialisiert werden; Kino-Bildschirme bleiben offline.");
            }
        } catch (RuntimeException exception) {
            MpsqCameraClient.LOGGER.warn("MCEF konnte nicht initialisiert werden; Kino-Bildschirme bleiben offline.", exception);
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) {
                clear();
                return;
            }
            ticks++;
            if (ticks % REFRESH_INTERVAL_TICKS == 0) synchronize();
            if (ticks % STATE_POLL_INTERVAL_TICKS == 0 && MpsqApiClient.isReady() && !refreshInProgress) {
                refreshInProgress = true;
                ScreenSyncManager.refresh().whenComplete((ignored, error) -> refreshInProgress = false);
            }
        });
    }

    /**
     * Returns MCEF's Minecraft texture identifier. MCEF changed the method name
     * between releases, so this deliberately supports both public variants.
     */
    public static Identifier texture(UUID screenId) {
        BrowserSession session = BROWSERS.get(screenId);
        if (session == null || !session.browser().isTextureReady()) return null;
        return McefTextureCompat.texture(session.browser());
    }

    /** Human-readable state used by the screen renderer while no browser image is available. */
    public static ScreenStatus status(LocalScreenStore.LocalScreenData screen) {
        if (screen.inputType() != LocalScreenStore.ScreenInputType.LINK) return ScreenStatus.NONE;
        if (screen.url().isBlank()) return ScreenStatus.NO_LINK;
        if (normalizeHttpUrl(screen.url()) == null || FAILED_BROWSERS.contains(screen.id())) return ScreenStatus.ERROR;
        if (!MCEF.isInitialized()) return ScreenStatus.LOADING;
        if (!CinemaPlaybackStore.get(screen.id()).playing()) return ScreenStatus.OFFLINE;
        return texture(screen.id()) == null ? ScreenStatus.LOADING : ScreenStatus.NONE;
    }

    public static void synchronize() {
        if (!MCEF.isInitialized()) return;

        Set<UUID> wanted = new HashSet<>();
        for (LocalScreenStore.LocalScreenData screen : LocalScreenStore.getAllScreens()) {
            if (screen.inputType() != LocalScreenStore.ScreenInputType.LINK || screen.url().isBlank()) continue;
            CinemaPlaybackStore.PlaybackState playback = CinemaPlaybackStore.get(screen.id());
            if (!playback.playing()) continue;

            String url = playableUrl(screen.url(), currentPosition(playback));
            if (url == null) {
                FAILED_BROWSERS.add(screen.id());
                continue;
            }
            wanted.add(screen.id());
            BrowserSession current = BROWSERS.get(screen.id());
            if (current != null && current.url().equals(url)) continue;

            close(screen.id());
            try {
                MCEFBrowser browser = MCEF.createBrowser(url, false);
                browser.resize(BROWSER_WIDTH, BROWSER_HEIGHT);
                BROWSERS.put(screen.id(), new BrowserSession(url, browser));
                FAILED_BROWSERS.remove(screen.id());
            } catch (RuntimeException error) {
                FAILED_BROWSERS.add(screen.id());
                MpsqCameraClient.LOGGER.warn("Kino-Browser für Bildschirm {} konnte nicht erstellt werden", screen.id(), error);
            }
        }

        for (UUID id : new HashSet<>(BROWSERS.keySet())) {
            if (!wanted.contains(id)) close(id);
        }
    }

    public static void clear() {
        new HashSet<>(BROWSERS.keySet()).forEach(CinemaBrowserManager::close);
        FAILED_BROWSERS.clear();
    }

    private static void close(UUID screenId) {
        BrowserSession session = BROWSERS.remove(screenId);
        if (session != null) session.browser().close();
    }

    /** Converts common YouTube links to their player URL, including a synchronized start point. */
    private static String playableUrl(String originalUrl, long positionMs) {
        try {
            String normalized = normalizeHttpUrl(originalUrl);
            if (normalized == null) return null;
            URI uri = new URI(normalized);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            String videoId = null;
            if (host.endsWith("youtu.be")) {
                videoId = uri.getPath().replaceFirst("^/", "");
            } else if (host.endsWith("youtube.com") && "/watch".equals(uri.getPath())) {
                for (String part : uri.getQuery() == null ? new String[0] : uri.getQuery().split("&")) {
                    if (part.startsWith("v=")) videoId = part.substring(2);
                }
            }
            if (videoId != null && !videoId.isBlank()) {
                long seconds = Math.max(0L, positionMs / 1000L);
                return "https://www.youtube.com/embed/" + videoId
                        + "?autoplay=1&mute=1&controls=0&rel=0&start=" + seconds;
            }
        } catch (URISyntaxException ignored) {
            // The browser will show the normal error page for an invalid URL.
        }
        return normalizeHttpUrl(originalUrl);
    }

    /** Only web links are allowed; local file/data URLs must never be opened by the client browser. */
    public static String normalizeHttpUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) return null;
        String candidate = rawUrl.trim();
        if (!candidate.contains("://")) candidate = "https://" + candidate;
        try {
            URI uri = new URI(candidate);
            String scheme = uri.getScheme();
            if (("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && uri.getHost() != null) {
                return uri.toString();
            }
        } catch (URISyntaxException ignored) { }
        return null;
    }

    private static long currentPosition(CinemaPlaybackStore.PlaybackState state) {
        if (!state.playing() || state.updatedAtMs() <= 0L) return state.positionMs();
        return Math.max(0L, state.positionMs() + System.currentTimeMillis() - state.updatedAtMs());
    }

    private record BrowserSession(String url, MCEFBrowser browser) { }

    /** Mirrors WatchParty's MCEF compatibility check without relying on raw OpenGL texture ids. */
    private static final class McefTextureCompat {
        private static final Method TEXTURE_METHOD = findTextureMethod();
        private static boolean warningLogged;

        private McefTextureCompat() { }

        private static Identifier texture(MCEFBrowser browser) {
            if (browser == null || TEXTURE_METHOD == null) return null;
            try {
                Object value = TEXTURE_METHOD.invoke(browser);
                return value instanceof Identifier identifier ? identifier : null;
            } catch (IllegalAccessException | InvocationTargetException exception) {
                if (!warningLogged) {
                    warningLogged = true;
                    MpsqCameraClient.LOGGER.warn("MCEF-Texturkennung konnte nicht gelesen werden", exception);
                }
                return null;
            }
        }

        private static Method findTextureMethod() {
            for (String name : new String[]{"getTextureIdentifier", "getTextureLocation"}) {
                try {
                    return MCEFBrowser.class.getMethod(name);
                } catch (NoSuchMethodException ignored) {
                    // Try the next MCEF API variant.
                }
            }
            MpsqCameraClient.LOGGER.warn("Diese MCEF-Version stellt keine Minecraft-Texturkennung bereit.");
            return null;
        }
    }

    public enum ScreenStatus {
        NONE("", 0, 0, 0),
        NO_LINK("KEIN LINK", 140, 140, 140),
        OFFLINE("OFFLINE", 155, 155, 155),
        LOADING("LAEDT", 225, 180, 55),
        ERROR("FEHLER", 210, 60, 55);

        private final String label;
        private final int red;
        private final int green;
        private final int blue;

        ScreenStatus(String label, int red, int green, int blue) {
            this.label = label;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public String label() { return label; }
        public int red() { return red; }
        public int green() { return green; }
        public int blue() { return blue; }
    }
}
