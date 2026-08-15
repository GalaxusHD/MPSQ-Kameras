package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;

/**
 * The role page deliberately has four views.  The server remains the authority
 * for every click; this screen only exposes the controls appropriate to the
 * current base role.
 */
public final class TeamMembersScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int LIST_WIDTH = 260;
    private static final int LIST_TOP = 78;
    private static final int RANK_X_GAP = 28;
    private static final int TAG_HEIGHT = 9;
    private static final int TAG_SPACING = 7;

    private final Screen parent;
    private TeamProfile selected;
    private String messageKey = "gui.mpsqcamera.team.members.loading";

    public TeamMembersScreen(Screen parent) {
        super(Text.translatable("gui.mpsqcamera.team.members"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), b -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 36, 150, 20).build());
        reload();
    }

    private void reload() {
        MpsqApiClient.refreshTeamProfile().thenCompose(profile -> MpsqApiClient.refreshTeamMembers())
                .whenComplete((members, error) -> client.execute(() -> {
                    messageKey = error == null ? "gui.mpsqcamera.team.members.help" : "gui.mpsqcamera.team.unavailable";
                    TeamProfile self = TeamStateStore.self().orElse(null);
                    if (selfView() && self != null) {
                        selected = self;
                    } else if (selected != null) {
                        selected = TeamStateStore.members().stream()
                                .filter(profile -> profile.id().equals(selected.id()))
                                .findFirst().orElse(null);
                    }
                }));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int left = width / 2 - 360;
        int right = left + LIST_WIDTH + RANK_X_GAP;
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 24, MpsqTheme.TEXT_TITEL);
        context.fill(left, 45, width - left, 47, MpsqTheme.TEXT_GEDAEMPT);

        if (showsSelectionHelp()) {
            context.drawTextWithShadow(textRenderer, Text.translatable(messageKey), left, 54, MpsqTheme.TEXT_GEDAEMPT);
        }

        int y = LIST_TOP;
        for (TeamProfile member : sortedMembers()) {
            boolean isSelected = member.equals(selected);
            context.fill(left, y, left + LIST_WIDTH, y + ROW_HEIGHT - 2,
                    isSelected ? 0x88557A9B : 0x66000000);
            member.displayedRank().draw(context, left + 4, y + 8, 8);
            int nameX = left + 10 + member.displayedRank().widthForHeight(8);
            context.drawTextWithShadow(textRenderer, member.displayName(), nameX, y + 8, MpsqTheme.TEXT_NORMAL);
            y += ROW_HEIGHT;
        }

        renderRightColumn(context, right, LIST_TOP);
    }

    private void renderRightColumn(DrawContext context, int x, int y) {
        if (publicView()) {
            context.drawTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.team.members.overview"), x, y - 18, MpsqTheme.TEXT_NORMAL);
            drawRanks(context, x, y, publicRanks());
            return;
        }

        if (selected == null) return;
        if (usesTemporary001()) {
            context.drawTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.team.members.temporary"), x, y - 18, MpsqTheme.TEXT_NORMAL);
            // The event rank remains visible for every selected person.  It is
            // intentionally clickable only for oneself, except for the Sr
            // Offizier who may manage it for other members.
            TeamRank.UNDERCOVER_001.draw(context, x, y, TAG_HEIGHT);
            y += TAG_HEIGHT + TAG_SPACING + 8;
        }

        List<TeamRank> permanent = permanentRanks();
        if (!permanent.isEmpty()) {
            context.drawTextWithShadow(textRenderer,
                    Text.translatable("gui.mpsqcamera.team.members.choose", selected.displayName()),
                    x, y - 18, MpsqTheme.TEXT_NORMAL);
            drawRanks(context, x, y, permanent);
        }
    }

    private void drawRanks(DrawContext context, int x, int y, List<TeamRank> ranks) {
        for (TeamRank rank : ranks) {
            rank.draw(context, x, y, TAG_HEIGHT);
            y += TAG_HEIGHT + TAG_SPACING;
        }
    }

    private boolean publicView() {
        TeamProfile self = TeamStateStore.self().orElse(null);
        return self == null || self.baseRank() == TeamRank.PLAYER || self.baseRank() == TeamRank.VIP || self.baseRank() == TeamRank.UNDERCOVER_001;
    }

    private boolean selfView() {
        TeamProfile self = TeamStateStore.self().orElse(null);
        return self != null && (self.baseRank() == TeamRank.SOLDIER || self.baseRank() == TeamRank.WORKER);
    }

    private boolean showsSelectionHelp() {
        return !publicView() && !selfView();
    }

    private boolean usesTemporary001() {
        TeamProfile self = TeamStateStore.self().orElse(null);
        return self != null && self.baseRank().level() >= TeamRank.SOLDIER.level();
    }

    private boolean mayToggle001() {
        TeamProfile self = TeamStateStore.self().orElse(null);
        if (self == null || selected == null) return false;
        if (self.baseRank() == TeamRank.SENIOR_OFFICER) {
            return selected.baseRank() != TeamRank.SENIOR_OFFICER;
        }
        return selected.id().equals(self.id());
    }

    private List<TeamRank> permanentRanks() {
        TeamProfile self = TeamStateStore.self().orElse(null);
        if (self == null || selected == null || selected.baseRank() == TeamRank.SENIOR_OFFICER) return List.of();
        return switch (self.baseRank()) {
            case OFFICER, FRONTMAN -> List.of(TeamRank.SOLDIER, TeamRank.WORKER, TeamRank.VIP, TeamRank.PLAYER);
            case SENIOR_OFFICER -> List.of(TeamRank.SOLDIER, TeamRank.WORKER, TeamRank.VIP, TeamRank.PLAYER,
                    TeamRank.OFFICER, TeamRank.FRONTMAN);
            default -> List.of();
        };
    }

    private List<TeamRank> publicRanks() {
        return List.of(TeamRank.FRONTMAN, TeamRank.SENIOR_OFFICER, TeamRank.OFFICER, TeamRank.SOLDIER,
                TeamRank.WORKER, TeamRank.UNDERCOVER_001, TeamRank.VIP, TeamRank.PLAYER);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = width / 2 - 360;
        if (!publicView() && !selfView()) {
            int y = LIST_TOP;
            for (TeamProfile member : sortedMembers()) {
                if (mouseX >= left && mouseX <= left + LIST_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT - 2) {
                    selected = member;
                    return true;
                }
                y += ROW_HEIGHT;
            }
        }

        if (selected != null && !publicView()) {
            int x = left + LIST_WIDTH + RANK_X_GAP;
            int y = LIST_TOP;
            if (usesTemporary001()) {
                if (mayToggle001() && withinRank(mouseX, mouseY, x, y, TeamRank.UNDERCOVER_001)) {
                    toggle001();
                    return true;
                }
                y += TAG_HEIGHT + TAG_SPACING + 8;
            }
            for (TeamRank rank : permanentRanks()) {
                if (withinRank(mouseX, mouseY, x, y, rank)) {
                    MpsqApiClient.changeTeamRank(selected.id(), rank).whenComplete((ignored, error) -> client.execute(this::reload));
                    return true;
                }
                y += TAG_HEIGHT + TAG_SPACING;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean withinRank(double mouseX, double mouseY, int x, int y, TeamRank rank) {
        int rankWidth = rank.widthForHeight(TAG_HEIGHT);
        return mouseX >= x - 3 && mouseX <= x + rankWidth + 3 && mouseY >= y - 3 && mouseY <= y + TAG_HEIGHT + 3;
    }

    private void toggle001() {
        if (selected.displayedRank() == TeamRank.UNDERCOVER_001) {
            MpsqApiClient.clearUndercoverRank(selected.id()).whenComplete((ignored, error) -> client.execute(this::reload));
        } else {
            MpsqApiClient.changeTeamRank(selected.id(), TeamRank.UNDERCOVER_001)
                    .whenComplete((ignored, error) -> client.execute(this::reload));
        }
    }

    @Override public boolean shouldPause() { return false; }

    private List<TeamProfile> sortedMembers() {
        return TeamStateStore.members().stream()
                .sorted(Comparator.comparingInt((TeamProfile value) -> value.displayedRank().level()).reversed()
                        .thenComparing(TeamProfile::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
