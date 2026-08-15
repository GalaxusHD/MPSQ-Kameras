package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

/** Officer-and-higher editor for creating, moving, changing and deleting tasks. */
public final class TeamTodoManageScreen extends Screen {
    private static final int WIDTH = 360;
    private final Screen parent;
    private TextFieldWidget input;
    private ButtonWidget listButton;
    private ButtonWidget saveButton;
    private ButtonWidget deleteButton;
    private List<TeamTodo> todos = List.of();
    private TeamTodo selected;
    private TeamTodoList selectedList;
    private String status = "";

    public TeamTodoManageScreen(Screen parent) {
        super(Text.translatable("gui.mpsqcamera.team.todo.manage"));
        this.parent = parent;
        this.selectedList = TeamStateStore.self().map(TeamProfile::baseRank).map(TeamTodoList::forRank).orElse(TeamTodoList.WORKER);
    }

    @Override protected void init() {
        int left = width / 2 - WIDTH / 2;
        input = new TextFieldWidget(textRenderer, left, 66, WIDTH - 126, 20, Text.translatable("gui.mpsqcamera.team.input"));
        input.setMaxLength(256);
        input.setPlaceholder(Text.translatable("gui.mpsqcamera.team.input"));
        addDrawableChild(input);
        listButton = addDrawableChild(ButtonWidget.builder(selectedList.title(), button -> nextList())
                .dimensions(left + WIDTH - 120, 66, 120, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.team.add"), button -> addTodo())
                .dimensions(left, 92, 112, 20).build());
        saveButton = addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.team.todo.save"), button -> saveTodo())
                .dimensions(left + 124, 92, 112, 20).build());
        deleteButton = addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.team.todo.delete"), button -> deleteTodo())
                .dimensions(left + 248, 92, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), button -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 36, 150, 20).build());
        updateButtons();
        reload();
    }

    private void nextList() {
        TeamTodoList[] lists = TeamTodoList.values();
        selectedList = lists[(selectedList.ordinal() + 1) % lists.length];
        listButton.setMessage(selectedList.title());
    }

    private void addTodo() {
        String text = input.getText().trim();
        if (text.isEmpty()) return;
        MpsqApiClient.addTeamTodo(text, selectedList).whenComplete((ignored, error) -> client.execute(() -> {
            status = error == null ? "OK" : "!";
            if (error == null) input.setText("");
            reload();
        }));
    }

    private void saveTodo() {
        if (selected == null || input.getText().trim().isEmpty()) return;
        MpsqApiClient.updateTeamTodo(selected.id(), input.getText().trim(), selectedList).whenComplete((ignored, error) -> client.execute(() -> {
            status = error == null ? "OK" : "!";
            reload();
        }));
    }

    private void deleteTodo() {
        if (selected == null) return;
        MpsqApiClient.deleteTeamTodo(selected.id()).whenComplete((ignored, error) -> client.execute(() -> {
            status = error == null ? "OK" : "!";
            if (error == null) { selected = null; input.setText(""); updateButtons(); }
            reload();
        }));
    }

    private void reload() {
        MpsqApiClient.loadTeamTodos().whenComplete((rows, error) -> client.execute(() -> {
            if (error == null) todos = rows; else status = "!";
        }));
    }

    private void updateButtons() { saveButton.active = selected != null; deleteButton.active = selected != null; }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = width / 2 - WIDTH / 2;
        int y = 124;
        for (TeamTodo todo : todos) {
            if (mouseX >= left && mouseX < left + WIDTH && mouseY >= y && mouseY < y + 18) {
                selected = todo;
                selectedList = todo.list();
                input.setText(todo.text());
                listButton.setMessage(selectedList.title());
                updateButtons();
                return true;
            }
            y += 19;
            if (y > height - 48) break;
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
        int y = 124;
        for (TeamTodo todo : todos) {
            context.fill(left, y, left + WIDTH, y + 17, todo.equals(selected) ? 0x88557A9B : 0x66000000);
            context.drawTextWithShadow(textRenderer, todo.list().title(), left + 5, y + 5, MpsqTheme.TEXT_GEDAEMPT);
            int prefixWidth = textRenderer.getWidth(todo.list().title());
            context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(todo.text(), WIDTH - prefixWidth - 18), left + 10 + prefixWidth, y + 5, MpsqTheme.TEXT_NORMAL);
            y += 19;
            if (y > height - 48) break;
        }
        if (!status.isEmpty()) context.drawTextWithShadow(textRenderer, status, left + WIDTH - 14, 52, status.equals("OK") ? 0x55FF55 : 0xFF5555);
    }

    @Override public boolean shouldPause() { return false; }
}
