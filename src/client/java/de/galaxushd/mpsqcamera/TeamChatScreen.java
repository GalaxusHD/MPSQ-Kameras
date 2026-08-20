package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

/** Private MPSQ Team chat. /mpsq <message> sends to this same chat without opening the screen. */
public final class TeamChatScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget input;
    private List<TeamChatMessage> messages = List.of();

    public TeamChatScreen(Screen parent) { super(Text.translatable("gui.mpsqcamera.team.chat")); this.parent = parent; }

    @Override protected void init() {
        input = new TextFieldWidget(textRenderer, width / 2 - 145, height - 62, 220, 20, Text.translatable("gui.mpsqcamera.team.chat.placeholder"));
        input.setMaxLength(256);
        input.setPlaceholder(Text.translatable("gui.mpsqcamera.team.chat.placeholder"));
        addDrawableChild(input);
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.team.chat.send"), b -> send())
                .dimensions(width / 2 + 81, height - 62, 64, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), b -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 36, 150, 20).build());
        setInitialFocus(input);
        reload();
    }

    private void reload() { MpsqApiClient.loadTeamMessages().whenComplete((rows, error) -> { if (error == null) client.execute(() -> messages = rows); }); }
    private void send() {
        String message = TeamChatPolicy.prepare(input.getText());
        if (message.isEmpty()) return;
        if (TeamChatPolicy.containsForbiddenContent(message)) {
            if (client.player != null) client.player.sendMessage(Text.translatable("gui.mpsqcamera.team.command.filtered"), true);
            return;
        }
        MpsqApiClient.sendTeamMessage(message).whenComplete((v, error) -> client.execute(() -> {
            if (error == null) { input.setText(""); reload(); }
            else if (client.player != null) client.player.sendMessage(Text.translatable("gui.mpsqcamera.team.command.failed"), true);
        }));
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { send(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) { super.renderBackground(context, mouseX, mouseY, delta); MpsqTheme.drawBackground(context, width, height); }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 24, MpsqTheme.TEXT_TITEL);
        int y = 48;
        int start = Math.max(0, messages.size() - Math.max(1, (height - 122) / 18));
        for (int i = start; i < messages.size(); i++) {
            TeamChatMessage message = messages.get(i);
            message.senderRank().draw(context, width / 2 - 145, y + 1, 12);
            int x = width / 2 - 139 + message.senderRank().widthForHeight(12);
            context.drawTextWithShadow(textRenderer,
                    Text.literal(message.senderName() + ": ").append(TeamChatText.fromAmpersandCodes(message.message(), net.minecraft.util.Formatting.WHITE)),
                    x, y + 4, MpsqTheme.TEXT_NORMAL);
            y += 18;
        }
    }
    @Override public boolean shouldPause() { return false; }
}
