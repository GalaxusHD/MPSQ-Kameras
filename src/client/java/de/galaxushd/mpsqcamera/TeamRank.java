package de.galaxushd.mpsqcamera;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/** Shared MPSQ Team roles. The order is the permission order, not a public leaderboard. */
public enum TeamRank {
    VIP("vip", "VIP", 0, 864),
    PLAYER("spieler", "Spieler", 1, 1440),
    UNDERCOVER_001("001", "001", 2, 672),
    SOLDIER("soldat", "Soldat", 3, 1248),
    WORKER("arbeiter", "Arbeiter", 4, 1632),
    OFFICER("offizier", "Offizier", 5, 1632),
    FRONTMAN("frontman", "Frontman", 6, 1632),
    SENIOR_OFFICER("sr_offizier", "Sr Offizier", 7, 2208);

    private static final int TEXTURE_HEIGHT = 224;
    private final String id;
    private final String label;
    private final int level;
    private final int textureWidth;

    TeamRank(String id, String label, int level, int textureWidth) {
        this.id = id;
        this.label = label;
        this.level = level;
        this.textureWidth = textureWidth;
    }

    public String id() { return id; }
    public String label() { return label; }
    public int level() { return level; }
    public Identifier texture() { return Identifier.of(MpsqCameraClient.MOD_ID, "textures/gui/ranks/" + id + ".png"); }
    public int widthForHeight(int height) { return Math.max(1, textureWidth * height / TEXTURE_HEIGHT); }

    public void draw(DrawContext context, int x, int y, int height) {
        int width = widthForHeight(height);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture(), x, y, 0, 0, width, height,
                textureWidth, TEXTURE_HEIGHT, textureWidth, TEXTURE_HEIGHT);
    }

    public static TeamRank fromId(String value) {
        if (value == null) return PLAYER;
        for (TeamRank rank : values()) if (rank.id.equalsIgnoreCase(value)) return rank;
        return PLAYER;
    }
}
