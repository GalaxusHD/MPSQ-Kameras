package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Role list and image-based promotion controls. Server-side checks remain authoritative. */
public final class TeamMembersScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private final Screen parent;
    private TeamProfile selected;
    private String messageKey = "gui.mpsqcamera.team.members.loading";

    public TeamMembersScreen(Screen parent) {
        super(Text.translatable("gui.mpsqcamera.team.members"));
        this.parent = parent;
    }

    @Override protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), b -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 36, 150, 20).build());
        reload();
    }

    private void reload() {
        MpsqApiClient.refreshTeamProfile().thenCompose(profile -> MpsqApiClient.refreshTeamMembers())
                .whenComplete((members, error) -> client.execute(() -> {
                    messageKey = error == null ? "gui.mpsqcamera.team.members.help" : "gui.mpsqcamera.team.unavailable";
                    if (selected != null) selected = TeamStateStore.members().stream().filter(p -> p.id().equals(selected.id())).findFirst().orElse(null);
                }));
    }

    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, width, height);
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int left = width / 2 - 154;
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 24, MpsqTheme.TEXT_TITEL);
        context.drawTextWithShadow(textRenderer, Text.translatable(messageKey), left, 44, MpsqTheme.TEXT_GEDAEMPT);
        int y = 62;
        for (TeamProfile member : TeamStateStore.members()) {
            boolean isSelected = member.equals(selected);
            context.fill(left, y, left + 145, y + ROW_HEIGHT - 2, isSelected ? 0x88557A9B : 0x66000000);
            member.displayedRank().draw(context, left + 4, y + 5, 12);
            int nameX = left + 10 + member.displayedRank().widthForHeight(12);
            context.drawTextWithShadow(textRenderer, member.displayName(), nameX, y + 8, MpsqTheme.TEXT_NORMAL);
            y += ROW_HEIGHT;
        }
        renderRankChoices(context, left + 162, 62);
    }

    private void renderRankChoices(DrawContext context, int x, int y) {
        if (selected == null) return;
        context.drawTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.team.members.choose", selected.displayName()), x, y - 18, MpsqTheme.TEXT_NORMAL);
        for (TeamRank rank : allowedRanks()) {
            int tagHeight = 14;
            int w = rank.widthForHeight(tagHeight);
            context.fill(x - 3, y - 3, x + w + 3, y + tagHeight + 3, 0x66000000);
            rank.draw(context, x, y, tagHeight);
            y += tagHeight + 7;
        }
        TeamStateStore.self().ifPresent(self -> {
            if (selected.id().equals(self.id()) && self.displayedRank() == TeamRank.UNDERCOVER_001) {
                context.drawTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.team.members.leave001"), x, y + 4, MpsqTheme.TEXT_GEDAEMPT);
            }
        });
    }

    private List<TeamRank> allowedRanks() {
        if (selected == null) return List.of();
        TeamProfile self = TeamStateStore.self().orElse(null);
        if (self == null) return List.of();
        if (self.permissionRank() == TeamRank.SENIOR_OFFICER) {
            return List.of(TeamRank.VIP, TeamRank.PLAYER, TeamRank.UNDERCOVER_001, TeamRank.SOLDIER, TeamRank.WORKER, TeamRank.OFFICER, TeamRank.FRONTMAN);
        }
        if (self.permissionRank() == TeamRank.OFFICER || self.permissionRank() == TeamRank.FRONTMAN) {
            return selected.displayedRank().level() <= TeamRank.WORKER.level()
                    ? List.of(TeamRank.VIP, TeamRank.PLAYER, TeamRank.UNDERCOVER_001, TeamRank.SOLDIER, TeamRank.WORKER) : List.of();
        }
        if ((self.baseRank() == TeamRank.SOLDIER || self.baseRank() == TeamRank.WORKER)
                && selected.id().equals(self.id())) return List.of(TeamRank.UNDERCOVER_001);
        return List.of();
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = width / 2 - 154;
        int y = 62;
        for (TeamProfile member : TeamStateStore.members()) {
            if (mouseX >= left && mouseX <= left + 145 && mouseY >= y && mouseY < y + ROW_HEIGHT - 2) { selected = member; return true; }
            y += ROW_HEIGHT;
        }
        if (selected != null) {
            int x = left + 162;
            y = 62;
            for (TeamRank rank : allowedRanks()) {
                int h = 14, w = rank.widthForHeight(h);
                if (mouseX >= x - 3 && mouseX <= x + w + 3 && mouseY >= y - 3 && mouseY <= y + h + 3) {
                    MpsqApiClient.changeTeamRank(selected.id(), rank).whenComplete((v, error) -> client.execute(this::reload));
                    return true;
                }
                y += h + 7;
            }
            TeamStateStore.self().ifPresent(self -> {
                if (selected.id().equals(self.id()) && self.displayedRank() == TeamRank.UNDERCOVER_001 && mouseY >= y && mouseY < y + 18) {
                    MpsqApiClient.clearOwnUndercoverRank().whenComplete((v, error) -> client.execute(this::reload));
                }
            });
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean shouldPause() { return false; }
}
