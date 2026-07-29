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

	public static void addScreen(BlockPos anchor, Vec3d createdFrom) {
		SCREENS.add(new LocalScreenData(
				UUID.randomUUID(),
				anchor.toImmutable(),
				createdFrom,
				ScreenInputType.LINK,
				"",
				null
		));
	}

	public static List<LocalScreenData> getInRange(Vec3d playerPos) {
		List<LocalScreenData> result = new ArrayList<>();
		for (LocalScreenData s : SCREENS) {
			if (s.anchor().getSquaredDistance(playerPos.x, playerPos.y, playerPos.z) <= LOAD_RANGE * LOAD_RANGE) {
				result.add(s);
			}
		}
		return result;
	}

	public static Optional<LocalScreenData> findByAnchor(BlockPos anchor) {
		for (LocalScreenData s : SCREENS) {
			if (s.anchor().equals(anchor)) return Optional.of(s);
		}
		return Optional.empty();
	}

	public static Optional<LocalScreenData> findNearest(Vec3d pos, double maxDistance) {
		double best = maxDistance * maxDistance;
		LocalScreenData found = null;
		for (LocalScreenData s : SCREENS) {
			double d = s.anchor().getSquaredDistance(pos.x, pos.y, pos.z);
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
						s.anchor(),
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

	// No owner field on purpose -> everyone in range can load.
	public record LocalScreenData(
			UUID id,
			BlockPos anchor,
			Vec3d createdFrom,
			ScreenInputType inputType,
			String url,
			UUID cameraId
	) {}
}
