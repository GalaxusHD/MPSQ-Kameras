package de.galaxushd.mpsqcamera;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class LocalScreenStore {
	private static final List<LocalScreenData> SCREENS = new ArrayList<>();
	private static final double LOAD_RANGE = 48.0; // blocks

	private LocalScreenStore() {}

	/** Legt einen Bildschirm mit einer einzelnen Anker-Position an (Legacy-Pfad). */
	public static void addScreen(BlockPos anchor, Vec3d createdFrom) {
		SCREENS.add(new LocalScreenData(
				UUID.randomUUID(),
				anchor.toImmutable(),
				anchor.toImmutable(),
				"Bildschirm",
				createdFrom,
				ScreenInputType.LINK,
				"",
				null
		));
	}

	/** Legt einen Bildschirm aus der Zweipunkt-Auswahl an. */
	public static void addScreenFromSelection(BlockPos pos1, BlockPos pos2, String name) {
		SCREENS.add(new LocalScreenData(
				UUID.randomUUID(),
				pos1.toImmutable(),
				pos2.toImmutable(),
				name.isBlank() ? "Bildschirm" : name,
				new Vec3d(pos1.getX(), pos1.getY(), pos1.getZ()),
				ScreenInputType.LINK,
				"",
				null
		));
	}

	public static List<LocalScreenData> getInRange(Vec3d playerPos) {
		List<LocalScreenData> result = new ArrayList<>();
		for (LocalScreenData s : SCREENS) {
			if (s.pos1().getSquaredDistance(playerPos.x, playerPos.y, playerPos.z)
					<= LOAD_RANGE * LOAD_RANGE) {
				result.add(s);
			}
		}
		return result;
	}

	public static List<LocalScreenData> getAllScreens() {
		return List.copyOf(SCREENS);
	}

	public static Optional<LocalScreenData> findByAnchor(BlockPos anchor) {
		for (LocalScreenData s : SCREENS) {
			if (s.pos1().equals(anchor)) return Optional.of(s);
		}
		return Optional.empty();
	}

	public static Optional<LocalScreenData> findById(UUID id) {
		for (LocalScreenData s : SCREENS) {
			if (s.id().equals(id)) return Optional.of(s);
		}
		return Optional.empty();
	}

	public static Optional<LocalScreenData> findNearest(Vec3d pos, double maxDistance) {
		double best = maxDistance * maxDistance;
		LocalScreenData found = null;
		for (LocalScreenData s : SCREENS) {
			double d = s.pos1().getSquaredDistance(pos.x, pos.y, pos.z);
			if (d <= best) {
				best = d;
				found = s;
			}
		}
		return Optional.ofNullable(found);
	}

	public static void updateConfig(UUID id, ScreenInputType mode, String url, UUID cameraId) {
		for (int i = 0; i < SCREENS.size(); i++) {
			LocalScreenData s = SCREENS.get(i);
			if (s.id().equals(id)) {
				SCREENS.set(i, new LocalScreenData(
						s.id(),
						s.pos1(),
						s.pos2(),
						s.name(),
						s.createdFrom(),
						mode,
						url == null ? "" : url,
						cameraId
				));
				return;
			}
		}
	}

	// ── Enum ────────────────────────────────────────────────────────────────

	public enum ScreenInputType {
		LINK("Link"),
		CAMERA("Kamera");

		private final String label;

		ScreenInputType(String label) { this.label = label; }

		public Text text() { return Text.literal(label); }
	}

	// ── Record ───────────────────────────────────────────────────────────────

	public record LocalScreenData(
			UUID id,
			BlockPos pos1,
			BlockPos pos2,
			String name,
			Vec3d createdFrom,
			ScreenInputType inputType,
			String url,
			UUID cameraId
	) {}
}
