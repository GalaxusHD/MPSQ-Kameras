package de.galaxushd.mpsqcamera;

/** One local switch for all optional MPSQ Team overlays and messages. */
public final class TeamVisibilitySettings {
    private static boolean visible = true;
    private TeamVisibilitySettings() { }
    public static boolean visible() { return visible; }
    public static void toggle() { visible = !visible; }
}
