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
        TeamProfile profile = TeamStateStore.members().stream()
                .filter(value -> value.displayName().equalsIgnoreCase(state.name))
                .findFirst().orElse(null);
        if (profile == null) return original;
        return Text.literal("[" + profile.displayedRank().label() + "] ")
                .formatted(profile.displayedRank().chatColor())
                .append(Text.literal(state.name));
    }
}
