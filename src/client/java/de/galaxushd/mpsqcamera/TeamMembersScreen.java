package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Role list and image-based promotion controls. Server-side checks remain authoritative. */
public final class TeamMembersScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int LIST_WIDTH = 210;
    private static final int LIST_TOP = 78;
    private static final int RANK_X_GAP = 15;
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
        int left = width / 2 - 230;
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 24, MpsqTheme.TEXT_TITEL);
        context.fill(left, 45, left + LIST_WIDTH + 260, 47, MpsqTheme.TEXT_GEDAEMPT);
        context.drawTextWithShadow(textRenderer, Text.translatable(messageKey), left, 54, MpsqTheme.TEXT_GEDAEMPT);
        int y = LIST_TOP;
        for (TeamProfile member : sortedMembers()) {
            boolean isSelected = member.equals(selected);
            context.fill(left, y, left + LIST_WIDTH, y + ROW_HEIGHT - 2, isSelected ? 0x88557A9B : 0x66000000);
            int tagHeight = 8;
            member.displayedRank().draw(context, left + 4, y + 8, tagHeight);
            int nameX = left + 10 + member.displayedRank().widthForHeight(tagHeight);
            context.drawTextWithShadow(textRenderer, member.displayName(), nameX, y + 8, MpsqTheme.TEXT_NORMAL);
            y += ROW_HEIGHT;
        }
        renderRankChoices(context, left + LIST_WIDTH + RANK_X_GAP, LIST_TOP + 30);
    }

    private void renderRankChoices(DrawContext context, int x, int y) {
        if (selected == null) return;
        context.drawTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.team.members.choose", selected.displayName()), x, y - 18, MpsqTheme.TEXT_NORMAL);
        for (TeamRank rank : allowedRanks()) {
            int tagHeight = 9;
            // The rank artwork already contains its complete visual design.
            // Keep the hit area in mouseClicked, but do not paint a dark button
            // or shadow behind the image.
            rank.draw(context, x, y, tagHeight);
            y += tagHeight + 7;
        }
        TeamProfile self = TeamStateStore.self().orElse(null);
        if (self != null && selected.id().equals(self.id()) && self.displayedRank() == TeamRank.UNDERCOVER_001) {
            context.drawTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.team.members.leave001"), x, y + 4, MpsqTheme.TEXT_GEDAEMPT);
        }
    }

    private List<TeamRank> allowedRanks() {
        if (selected == null) return List.of();
        TeamProfile self = TeamStateStore.self().orElse(null);
        if (self == null) return List.of();
        if (self.permissionRank() == TeamRank.SENIOR_OFFICER) {
            return List.of(TeamRank.PLAYER, TeamRank.VIP, TeamRank.UNDERCOVER_001, TeamRank.WORKER, TeamRank.SOLDIER, TeamRank.OFFICER, TeamRank.FRONTMAN);
        }
        if (self.permissionRank() == TeamRank.OFFICER || self.permissionRank() == TeamRank.FRONTMAN) {
            return selected.displayedRank().level() <= TeamRank.WORKER.level()
                    ? List.of(TeamRank.PLAYER, TeamRank.VIP, TeamRank.UNDERCOVER_001, TeamRank.WORKER, TeamRank.SOLDIER) : List.of();
        }
        // The 001 rank is a reversible event disguise. Every actual staff
        // rank (Arbeiter, Soldat, Offizier, Frontman and Sr Offizier) may
        // enable it for itself; it can never be assigned to another member.
        boolean mayUseOwn001 = self.baseRank() == TeamRank.WORKER
                || self.baseRank() == TeamRank.SOLDIER
                || self.baseRank() == TeamRank.OFFICER
                || self.baseRank() == TeamRank.FRONTMAN
                || self.baseRank() == TeamRank.SENIOR_OFFICER;
        if (mayUseOwn001 && selected.id().equals(self.id())) return List.of(TeamRank.UNDERCOVER_001);
        return List.of();
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = width / 2 - 230;
        int y = LIST_TOP;
        for (TeamProfile member : sortedMembers()) {
            if (mouseX >= left && mouseX <= left + LIST_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT - 2) { selected = member; return true; }
            y += ROW_HEIGHT;
        }
        if (selected != null) {
            int x = left + LIST_WIDTH + RANK_X_GAP;
            y = LIST_TOP + 30;
            for (TeamRank rank : allowedRanks()) {
                int h = 9, w = rank.widthForHeight(h);
                if (mouseX >= x - 3 && mouseX <= x + w + 3 && mouseY >= y - 3 && mouseY <= y + h + 3) {
                    MpsqApiClient.changeTeamRank(selected.id(), rank).whenComplete((v, error) -> client.execute(this::reload));
                    return true;
                }
                y += h + 7;
            }
            TeamProfile self = TeamStateStore.self().orElse(null);
            if (self != null && selected.id().equals(self.id()) && self.displayedRank() == TeamRank.UNDERCOVER_001 && mouseY >= y && mouseY < y + 18) {
                MpsqApiClient.clearOwnUndercoverRank().whenComplete((v, error) -> client.execute(this::reload));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean shouldPause() { return false; }

    private List<TeamProfile> sortedMembers() {
        return TeamStateStore.members().stream()
                .sorted(Comparator.comparingInt((TeamProfile value) -> value.displayedRank().level()).reversed()
                        .thenComparing(TeamProfile::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
