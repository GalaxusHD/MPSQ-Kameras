package de.galaxushd.mpsqcamera;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Persistente Mod-Konfiguration (gespeichert in config/mpsqcamera.properties).
 * Enthält: Werkzeug-Item-ID und globale Lautstärke.
 */
public final class ModConfig {

	public static final String DEFAULT_TOOL_ITEM_ID = "minecraft:arrow";
	public static final int    DEFAULT_GLOBAL_VOLUME = 100;

	private static final String CONFIG_FILE = "mpsqcamera.properties";

	private static String toolItemId    = DEFAULT_TOOL_ITEM_ID;
	private static int    globalVolume  = DEFAULT_GLOBAL_VOLUME;

	private ModConfig() {}

	// ── Getters ──────────────────────────────────────────────────────────────

	public static String getToolItemId()   { return toolItemId; }
	public static int    getGlobalVolume() { return globalVolume; }

	// ── Setters (validated) ───────────────────────────────────────────────────

	public static void setToolItemId(String id) {
		toolItemId = (id == null || id.isBlank()) ? DEFAULT_TOOL_ITEM_ID : id.trim();
	}

	public static void setGlobalVolume(int volume) {
		globalVolume = Math.max(0, Math.min(100, volume));
	}

	// ── Persistence ───────────────────────────────────────────────────────────

	public static void load() {
		Path path = configPath();
		if (!Files.exists(path)) return;

		Properties props = new Properties();
		try (InputStream in = Files.newInputStream(path)) {
			props.load(in);
		} catch (IOException e) {
			MpsqCameraClient.LOGGER.warn("[MPSQ Kameras] Konfiguration konnte nicht geladen werden: {}", e.getMessage());
			return;
		}

		toolItemId = props.getProperty("toolItemId", DEFAULT_TOOL_ITEM_ID);

		try {
			globalVolume = Math.max(0, Math.min(100,
					Integer.parseInt(props.getProperty("globalVolume", String.valueOf(DEFAULT_GLOBAL_VOLUME)))));
		} catch (NumberFormatException ignored) {
			globalVolume = DEFAULT_GLOBAL_VOLUME;
		}
	}

	public static void save() {
		Path path = configPath();
		Properties props = new Properties();
		props.setProperty("toolItemId",   toolItemId);
		props.setProperty("globalVolume", String.valueOf(globalVolume));

		try {
			Files.createDirectories(path.getParent());
			try (OutputStream out = Files.newOutputStream(path)) {
				props.store(out, "MPSQ Kameras Konfiguration");
			}
		} catch (IOException e) {
			MpsqCameraClient.LOGGER.warn("[MPSQ Kameras] Konfiguration konnte nicht gespeichert werden: {}", e.getMessage());
		}
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
	}
}
