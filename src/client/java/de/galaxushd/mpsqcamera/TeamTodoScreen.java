package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Four shared task lists. Completion ticks are intentionally only local UI state. */
public final class TeamTodoScreen extends Screen {
    private static final int PAGE_MARGIN = 18;
    private static final int COLUMN_GAP = 16;
    private static final int ROW_HEIGHT = 18;
    private final Screen parent;
    private List<TeamTodo> todos = List.of();
    private final Set<UUID> checked = new HashSet<>();
    private String status = "";

    public TeamTodoScreen(Screen parent) {
        super(Text.translatable("gui.mpsqcamera.team.todo"));
        this.parent = parent;
    }

    @Override protected void init() {
        if (canManage()) {
            addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.team.todo.manage"),
                    button -> client.setScreen(new TeamTodoManageScreen(this)))
                    .dimensions(width / 2 - 75, 52, 150, 20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), button -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 36, 150, 20).build());
        reload();
    }

    private boolean canManage() {
        return TeamStateStore.self().map(TeamProfile::baseRank)
                .map(rank -> rank.level() >= TeamRank.OFFICER.level()).orElse(false);
    }

    private void reload() {
        MpsqApiClient.loadTeamTodos().whenComplete((rows, error) -> client.execute(() -> {
            if (error == null) todos = rows; else status = "!";
        }));
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (TeamTodoList list : TeamTodoList.values()) {
            int x = columnX(list);
            int y = listY(list) + 17;
            int columnWidth = columnWidth();
            for (TeamTodo todo : todos) {
                if (todo.list() != list) continue;
                if (mouseX >= x && mouseX < x + columnWidth && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                    if (!checked.add(todo.id())) checked.remove(todo.id());
                    return true;
                }
                y += ROW_HEIGHT;
                if (y > height - 48) break;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, width, height);
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 24, MpsqTheme.TEXT_TITEL);
        context.fill(PAGE_MARGIN, 45, width - PAGE_MARGIN, 47, MpsqTheme.TEXT_GEDAEMPT);
        for (TeamTodoList list : TeamTodoList.values()) drawList(context, list);
        if (!status.isEmpty()) context.drawCenteredTextWithShadow(textRenderer, status, width / 2, height - 52, 0xFF5555);
    }

    private void drawList(DrawContext context, TeamTodoList list) {
        int x = columnX(list);
        int y = listY(list);
        int columnWidth = columnWidth();
        context.drawTextWithShadow(textRenderer, list.title(), x, y, MpsqTheme.TEXT_NORMAL);
        y += 17;
        for (TeamTodo todo : todos) {
            if (todo.list() != list) continue;
            context.fill(x, y, x + columnWidth, y + ROW_HEIGHT - 2, 0x66000000);
            boolean isChecked = checked.contains(todo.id());
            context.fill(x + 4, y + 4, x + 13, y + 13, 0xFFDDDDDD);
            if (isChecked) {
                context.fill(x + 6, y + 6, x + 11, y + 11, 0xFF55FF55);
                context.drawTextWithShadow(textRenderer, "✓", x + 5, y + 3, 0xFF006600);
            }
            context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(todo.text(), columnWidth - 23),
                    x + 18, y + 5, isChecked ? MpsqTheme.TEXT_GEDAEMPT : MpsqTheme.TEXT_NORMAL);
            y += ROW_HEIGHT;
            if (y > height - 48) break;
        }
    }

    private int columnWidth() { return Math.max(120, (width - PAGE_MARGIN * 2 - COLUMN_GAP) / 2); }
    private int columnX(TeamTodoList list) { return (list == TeamTodoList.WORKER || list == TeamTodoList.OFFICER) ? PAGE_MARGIN : PAGE_MARGIN + columnWidth() + COLUMN_GAP; }
    private int listY(TeamTodoList list) { return (list == TeamTodoList.WORKER || list == TeamTodoList.SOLDIER) ? 86 : height / 2 + 8; }
    @Override public boolean shouldPause() { return false; }
}
