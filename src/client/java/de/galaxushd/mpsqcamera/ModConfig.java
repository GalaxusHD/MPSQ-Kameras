package de.galaxushd.mpsqcamera;

/**
 * Einfacher Laufzeit-Konfigurationsspeicher (nicht persistiert – wird bei jedem Start zurückgesetzt).
 * TODO: Persistenz via JSON/Toml ergänzen, sobald benötigt.
 */
public final class ModConfig {

    private ModConfig() {}

    /** Item-ID des Erstellungs-Werkzeugs (Standard: minecraft:blaze_rod). */
    public static String toolItemId = "minecraft:blaze_rod";

    /** Globale Wiedergabe-Lautstärke (0.0 – 1.0). */
    public static float volume = 1.0f;
}
