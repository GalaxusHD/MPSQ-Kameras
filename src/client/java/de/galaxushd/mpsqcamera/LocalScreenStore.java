package de.galaxushd.mpsqcamera;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class LocalScreenStore {
	private static final List<LocalScreenData> SCREENS = new ArrayList<>();
	private static final List<LocalGroupData> GROUPS = new ArrayList<>();
	private static final double LOAD_RANGE = 48.0; // blocks
	private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final int GROUP_CODE_LENGTH = 6;
	private static final SecureRandom RANDOM = new SecureRandom();

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
				null,
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
				null,
				null
		));
	}

	/** Entfernt einen Bildschirm. Wenn er in einer Gruppe ist, wird die ganze Gruppe gelöscht. */
	public static void removeScreen(UUID id) {
		LocalScreenData screen = findById(id).orElse(null);
		if (screen == null) return;

		if (screen.groupId() != null) {
			removeGroup(screen.groupId());
		} else {
			SCREENS.removeIf(s -> s.id().equals(id));
		}
	}

	/** Erstellt eine neue Gruppe mit den angegebenen Bildschirmen und einem gemeinsamen Code. */
	public static LocalGroupData createGroup(List<UUID> screenIds) {
		String sharedCode = generateGroupCode();
		UUID groupId = UUID.randomUUID();
		LocalGroupData group = new LocalGroupData(groupId, sharedCode);
		GROUPS.add(group);

		for (int i = 0; i < SCREENS.size(); i++) {
			LocalScreenData s = SCREENS.get(i);
			if (screenIds.contains(s.id())) {
				SCREENS.set(i, new LocalScreenData(
						s.id(), s.pos1(), s.pos2(), s.name(), s.createdFrom(),
						s.inputType(), s.url(), s.cameraId(), groupId
				));
			}
		}
		return group;
	}

	/** Löscht eine Gruppe und alle Bildschirme, die zu ihr gehören. */
	public static void removeGroup(UUID groupId) {
		GROUPS.removeIf(g -> g.id().equals(groupId));
		SCREENS.removeIf(s -> groupId.equals(s.groupId()));
	}

	/** Gibt die Gruppe eines Bildschirms zurück (Optional.empty() wenn nicht in Gruppe). */
	public static Optional<LocalGroupData> getGroupForScreen(UUID screenId) {
		LocalScreenData screen = findById(screenId).orElse(null);
		if (screen == null || screen.groupId() == null) return Optional.empty();
		UUID groupId = screen.groupId();
		return GROUPS.stream().filter(g -> g.id().equals(groupId)).findFirst();
	}

	/** Gibt alle Gruppen zurück. */
	public static List<LocalGroupData> getAllGroups() {
		return List.copyOf(GROUPS);
	}

	/** Gibt alle Bildschirme einer Gruppe zurück. */
	public static List<LocalScreenData> getScreensInGroup(UUID groupId) {
		List<LocalScreenData> result = new ArrayList<>();
		for (LocalScreenData s : SCREENS) {
			if (groupId.equals(s.groupId())) result.add(s);
		}
		return result;
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
						cameraId,
						s.groupId()
				));
				return;
			}
		}
	}

	private static String generateGroupCode() {
		StringBuilder sb = new StringBuilder(GROUP_CODE_LENGTH);
		for (int i = 0; i < GROUP_CODE_LENGTH; i++) {
			sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
		}
		return sb.toString();
	}

	// ── Enums ────────────────────────────────────────────────────────────────

	public enum ScreenInputType {
		LINK("Link"),
		CAMERA("Kamera");

		private final String label;

		ScreenInputType(String label) { this.label = label; }

		public Text text() { return Text.literal(label); }
	}

	public enum DeleteBehavior {
		NIE("Nie"),
		CREATOR_OFFLINE("Wenn Creator off"),
		NICHT_GELADEN("Wenn nicht geladen");

		private final String label;

		DeleteBehavior(String label) { this.label = label; }

		public String getLabel() { return label; }

		public DeleteBehavior next() {
			DeleteBehavior[] values = values();
			return values[(this.ordinal() + 1) % values.length];
		}
	}

	// ── Records ───────────────────────────────────────────────────────────────

	public record LocalScreenData(
			UUID id,
			BlockPos pos1,
			BlockPos pos2,
			String name,
			Vec3d createdFrom,
			ScreenInputType inputType,
			String url,
			UUID cameraId,
			UUID groupId
	) {}

	public record LocalGroupData(
			UUID id,
			String sharedCode
	) {}
}
