package de.galaxushd.mpsqcamera.mixin.client;

import de.galaxushd.mpsqcamera.TeamProfile;
import de.galaxushd.mpsqcamera.TeamStateStore;
import de.galaxushd.mpsqcamera.TeamVisibilitySettings;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Replaces a server-provided prefix locally for MPSQ Team users. */
@Mixin(net.minecraft.client.render.entity.PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    /**
     * PlayerEntityRenderer receives the final server nameplate as its only Text
     * argument. Replacing that argument at method entry removes every server
     * prefix (for example "ULTRA") in one place before Minecraft renders it.
     */
    @ModifyVariable(
            method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private Text mpsq$replaceServerRank(
            Text original,
            PlayerEntityRenderState state
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
        // Deliberately build an entirely new label.  Appending to "original"
        // would preserve the server's rank prefix and causes tags such as
        // "ULTRA" to remain visible.
        return Text.literal("[" + profile.displayedRank().label() + "] ")
                .formatted(profile.displayedRank().chatColor())
                .append(Text.literal(profile.displayName()).formatted(net.minecraft.util.Formatting.WHITE));
    }

    private static boolean matchesProfileName(String profileName, String renderedName) {
        if (profileName == null || profileName.isBlank() || renderedName == null) return false;
        return renderedName.equalsIgnoreCase(profileName)
                || renderedName.regionMatches(true, Math.max(0, renderedName.length() - profileName.length()),
                profileName, 0, profileName.length());
    }
}
