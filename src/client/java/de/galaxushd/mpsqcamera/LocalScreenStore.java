package de.galaxushd.mpsqcamera;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public final class LocalScreenStore {
	private static final List<LocalScreenData> SCREENS = new ArrayList<>();
	private static final double LOAD_RANGE = 48.0; // blocks

	private LocalScreenStore() {}

	public static void addScreen(BlockPos anchor, Vec3d createdFrom) {
		SCREENS.add(new LocalScreenData(anchor, createdFrom));
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

	// No owner field on purpose -> everyone in range can load.
	public record LocalScreenData(BlockPos anchor, Vec3d createdFrom) {}
}
