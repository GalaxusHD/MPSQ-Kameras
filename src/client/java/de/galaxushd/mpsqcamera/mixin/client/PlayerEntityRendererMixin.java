package de.galaxushd.mpsqcamera.mixin.client;

import de.galaxushd.mpsqcamera.TeamProfile;
import de.galaxushd.mpsqcamera.MpsqCameraClient;
import de.galaxushd.mpsqcamera.TeamStateStore;
import de.galaxushd.mpsqcamera.TeamVisibilitySettings;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Replaces a server-provided prefix locally for MPSQ Team users. */
@Mixin(net.minecraft.client.render.entity.PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    private static final Identifier MPSQ_RANK_FONT = Identifier.of(MpsqCameraClient.MOD_ID, "ranks");

    @ModifyVariable(method = "renderLabelIfPresent", at = @At("HEAD"), argsOnly = true)
    private Text mpsq$replaceServerRank(
            Text original,
            PlayerEntityRenderState state,
            Text renderedText,
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            int light
    ) {
        if (!TeamVisibilitySettings.visible()) return original;
        // Some servers pass the complete server label here (for example
        // "ULTRA MP_SquidGame") instead of only the game-profile name.  Do
        // not require an exact equality check: locate the known Minecraft
        // name at the end of either text, then build a fresh label so the
        // original server rank can never leak through.
        String stateName = state.name == null ? "" : state.name;
        String originalName = original.getString();
        TeamProfile profile = TeamStateStore.members().stream()
                .filter(value -> matchesProfileName(value.displayName(), stateName)
                        || matchesProfileName(value.displayName(), originalName))
                .findFirst().orElse(null);
        if (profile == null) return original;
        return Text.literal(rankGlyph(profile.displayedRank()))
                .setStyle(Style.EMPTY.withFont(MPSQ_RANK_FONT).withColor(Formatting.WHITE))
                .append(Text.literal(" "))
                .append(Text.literal(profile.displayName()).formatted(Formatting.WHITE));
    }

    /**
     * The glyphs are bitmap entries in assets/mpsqcamera/font/ranks.json.  Using
     * Minecraft's normal text renderer lets the icon appear in every 3D name
     * label without relying on the server's resource-pack rank characters.
     */
    private static String rankGlyph(de.galaxushd.mpsqcamera.TeamRank rank) {
        return switch (rank) {
            case VIP -> "\ue001";
            case PLAYER -> "\ue002";
            case UNDERCOVER_001 -> "\ue003";
            case SOLDIER -> "\ue004";
            case WORKER -> "\ue005";
            case OFFICER -> "\ue006";
            case FRONTMAN -> "\ue007";
            case SENIOR_OFFICER -> "\ue008";
        };
    }

    private static boolean matchesProfileName(String profileName, String renderedName) {
        if (profileName == null || profileName.isBlank() || renderedName == null) return false;
        return renderedName.equalsIgnoreCase(profileName)
                || renderedName.regionMatches(true, Math.max(0, renderedName.length() - profileName.length()),
                profileName, 0, profileName.length());
    }
}
