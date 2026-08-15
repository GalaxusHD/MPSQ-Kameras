package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.List;

/** Shared persisted event boards: to-dos, timer and reusable announcements. */
public final class TeamBoardScreen extends Screen {
    public enum Mode { TODO, TIMER, TEMPLATES }
    private static final int WIDTH = 270;
    private final Screen parent;
    private final Mode mode;
    private TextFieldWidget input;
    private List<TeamTodo> todos = List.of();
    private List<TeamTemplate> templates = List.of();
    private TeamTimerState timer = new TeamTimerState(false, null, "");
    private String status = "";

    public TeamBoardScreen(Screen parent, Mode mode) {
        super(Text.translatable(key(mode)));
        this.parent = parent;
        this.mode = mode;
    }

    private static String key(Mode mode) {
        return switch (mode) {
            case TODO -> "gui.mpsqcamera.team.todo";
            case TIMER -> "gui.mpsqcamera.team.timer";
            case TEMPLATES -> "gui.mpsqcamera.team.templates";
        };
    }

    @Override protected void init() {
        int left = width / 2 - WIDTH / 2;
        input = new TextFieldWidget(textRenderer, left, height - 62, WIDTH - 70, 20, Text.translatable("gui.mpsqcamera.team.input"));
        input.setMaxLength(mode == Mode.TIMER ? 120 : 256);
        input.setPlaceholder(Text.translatable(mode == Mode.TIMER ? "gui.mpsqcamera.team.timer.input" : "gui.mpsqcamera.team.input"));
        addDrawableChild(input);
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.team.add"), b -> submit())
                .dimensions(left + WIDTH - 64, height - 62, 64, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), b -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 36, 150, 20).build());
        reload();
    }

    private TeamRank rank() { return TeamStateStore.self().map(TeamProfile::permissionRank).orElse(TeamRank.VIP); }
    private boolean canEdit() {
        return switch (mode) {
            case TODO -> rank().level() >= TeamRank.WORKER.level();
            case TIMER, TEMPLATES -> rank().level() >= TeamRank.OFFICER.level();
        };
    }

    private void reload() {
        switch (mode) {
            case TODO -> MpsqApiClient.loadTeamTodos().whenComplete((rows, error) -> client.execute(() -> {
                if (error == null) todos = rows; else status = "!";
            }));
            case TIMER -> MpsqApiClient.loadTeamTimer().whenComplete((row, error) -> client.execute(() -> {
                if (error == null) timer = row; else status = "!";
            }));
            case TEMPLATES -> MpsqApiClient.loadTeamTemplates().whenComplete((rows, error) -> client.execute(() -> {
                if (error == null) templates = rows; else status = "!";
            }));
        }
    }

    private void submit() {
        if (!canEdit()) { status = "!"; return; }
        String value = input.getText().trim();
        if (mode == Mode.TIMER) {
            long seconds;
            try { seconds = Math.max(0, Long.parseLong(value.split(" ", 2)[0])); }
            catch (NumberFormatException ignored) { status = "!"; return; }
            String label = value.contains(" ") ? value.substring(value.indexOf(' ') + 1).trim() : "";
            MpsqApiClient.updateTeamTimer(seconds > 0, seconds, label).whenComplete((v, error) -> client.execute(() -> { status = error == null ? "OK" : "!"; reload(); }));
        } else if (!value.isEmpty()) {
            (mode == Mode.TODO ? MpsqApiClient.addTeamTodo(value) : MpsqApiClient.addTeamTemplate(value))
                    .whenComplete((v, error) -> client.execute(() -> { if (error == null) input.setText(""); status = error == null ? "OK" : "!"; reload(); }));
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mode == Mode.TODO && canEdit()) {
            int y = 70;
            for (TeamTodo todo : todos) {
                if (mouseY >= y && mouseY < y + 18 && mouseX >= width / 2 - WIDTH / 2 && mouseX <= width / 2 + WIDTH / 2) {
                    MpsqApiClient.toggleTeamTodo(todo.id(), !todo.done()).whenComplete((v, error) -> client.execute(this::reload));
                    return true;
                }
                y += 19;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta); MpsqTheme.drawBackground(context, width, height);
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int left = width / 2 - WIDTH / 2;
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 24, MpsqTheme.TEXT_TITEL);
        context.fill(left, 45, left + WIDTH, 47, MpsqTheme.TEXT_GEDAEMPT);
        context.drawTextWithShadow(textRenderer, canEdit() ? Text.translatable("gui.mpsqcamera.team.editable") : Text.translatable("gui.mpsqcamera.team.readonly"), left, 52, MpsqTheme.TEXT_GEDAEMPT);
        if (mode == Mode.TODO) drawTodos(context, left);
        else if (mode == Mode.TIMER) drawTimer(context, left);
        else drawTemplates(context, left);
        if (!status.isEmpty()) context.drawTextWithShadow(textRenderer, status, left + WIDTH - 18, 52, status.equals("OK") ? 0x55FF55 : 0xFF5555);
    }

    private void drawTodos(DrawContext context, int left) {
        int y = 70;
        for (TeamTodo todo : todos) {
            context.fill(left, y, left + WIDTH, y + 17, todo.done() ? 0x44335533 : 0x66000000);
            String prefix = todo.done() ? "[x] " : "[ ] ";
            context.drawTextWithShadow(textRenderer, prefix + todo.text(), left + 5, y + 5, todo.done() ? MpsqTheme.TEXT_GEDAEMPT : MpsqTheme.TEXT_NORMAL);
            y += 19;
            if (y > height - 88) break;
        }
    }

    private void drawTimer(DrawContext context, int left) {
        String text = timer.label();
        if (timer.running() && timer.endsAt() != null) {
            try { text = text + "  " + Math.max(0, Instant.parse(timer.endsAt()).getEpochSecond() - Instant.now().getEpochSecond()) + " s"; }
            catch (Exception ignored) { }
        }
        context.drawCenteredTextWithShadow(textRenderer, text.isBlank() ? Text.translatable("gui.mpsqcamera.team.timer.idle") : Text.literal(text), width / 2, height / 2 - 12, MpsqTheme.TEXT_NORMAL);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable(timer.running() ? "gui.mpsqcamera.team.timer.running" : "gui.mpsqcamera.team.timer.idle"), width / 2, height / 2 + 8, MpsqTheme.TEXT_GEDAEMPT);
    }

    private void drawTemplates(DrawContext context, int left) {
        int y = 70;
        for (TeamTemplate template : templates) {
            context.fill(left, y, left + WIDTH, y + 17, 0x66000000);
            context.drawTextWithShadow(textRenderer, template.text(), left + 5, y + 5, MpsqTheme.TEXT_NORMAL);
            y += 19;
            if (y > height - 88) break;
        }
    }

    @Override public boolean shouldPause() { return false; }
}
