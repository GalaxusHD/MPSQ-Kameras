package de.galaxushd.mpsqcamera;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Owner view for access granted through a screen's activation code. */
public final class BildschirmPermissionsScreen extends Screen {
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_GAP = 26;
    private static final int MEMBER_START_Y = 132;

    private final Screen parent;
    private final UUID screenId;
    private final String activationCode;
    private final List<Member> members = new ArrayList<>();
    private String status = "Lade Berechtigungen...";
    private boolean membersLoaded;
    private int memberScrollOffset;
    private int maxMemberScrollOffset;
    private TextFieldWidget playerNameField;
    private ButtonWidget grantButton;

    public BildschirmPermissionsScreen(Screen parent, UUID screenId, String activationCode) {
        super(Text.literal("Berechtigungen"));
        this.parent = parent;
        this.screenId = screenId;
        this.activationCode = activationCode;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int x = centerX - BUTTON_WIDTH / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Code kopieren: " + activationCode), button -> {
                    client.keyboard.setClipboard(activationCode);
                    status = "Code kopiert. Teile ihn nur mit berechtigten Spielern.";
                })
                .dimensions(x, 54, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        playerNameField = new TextFieldWidget(textRenderer, x, 84, 148, BUTTON_HEIGHT, Text.literal("Minecraft-Name"));
        playerNameField.setMaxLength(32);
        playerNameField.setPlaceholder(Text.literal("Spielername"));
        addDrawableChild(playerNameField);
        grantButton = addDrawableChild(ButtonWidget.builder(Text.literal("Freigeben"), button -> grantPlayer())
                .dimensions(x + 152, 84, 68, BUTTON_HEIGHT).build());
        grantButton.active = false;
        playerNameField.setChangedListener(value -> grantButton.active = !value.trim().isEmpty());
        addDrawableChild(ButtonWidget.builder(Text.literal("Aktualisieren"), button -> loadMembers())
                .dimensions(x, height - 54, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), button -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 28, 150, BUTTON_HEIGHT)
                .build());
        int memberY = MEMBER_START_Y - memberScrollOffset;
        for (Member member : members) {
            if (memberY >= MEMBER_START_Y && memberY <= height - 92) {
                addDrawableChild(ButtonWidget.builder(Text.literal("Entfernen"), button -> remove(member))
                        .dimensions(centerX + BUTTON_WIDTH / 2 - 74, memberY, 74, BUTTON_HEIGHT).build());
            }
            memberY += ROW_GAP;
        }
        maxMemberScrollOffset = Math.max(0, members.size() * ROW_GAP - (height - 92 - MEMBER_START_Y + ROW_GAP));
        memberScrollOffset = Math.min(memberScrollOffset, maxMemberScrollOffset);
        if (!membersLoaded) loadMembers();
    }

    private void loadMembers() {
        membersLoaded = false;
        status = "Lade Berechtigungen...";
        MpsqApiClient.get("/screens/" + screenId + "/members")
                .whenComplete((response, error) -> client.execute(() -> {
                    members.clear();
                    if (error != null) {
                        Throwable cause = error.getCause() == null ? error : error.getCause();
                        status = "Berechtigungen: " + (cause.getMessage() == null ? "Serverfehler" : cause.getMessage());
                    } else {
                        readMembers(response);
                        status = members.isEmpty() ? "Noch keine weiteren Spieler berechtigt." : "";
                    }
                    membersLoaded = true;
                    clearAndInit();
                }));
    }

    private void readMembers(JsonElement response) {
        if (response == null || !response.isJsonArray()) return;
        for (JsonElement element : response.getAsJsonArray()) {
            JsonObject row = element.getAsJsonObject();
            if (!row.has("client_id")) continue;
            String name = "Unbekannter Spieler";
            if (row.has("mpsq_clients") && row.get("mpsq_clients").isJsonObject()) {
                JsonObject client = row.getAsJsonObject("mpsq_clients");
                if (client.has("display_name") && !client.get("display_name").getAsString().isBlank()) {
                    name = client.get("display_name").getAsString();
                }
            }
            members.add(new Member(UUID.fromString(row.get("client_id").getAsString()), name));
        }
    }

    private void grantPlayer() {
        String playerName = playerNameField.getText().trim();
        if (playerName.isEmpty()) return;

        JsonObject body = new JsonObject();
        body.addProperty("displayName", playerName);
        grantButton.active = false;
        MpsqApiClient.post("/screens/" + screenId + "/members", body)
                .whenComplete((ignored, error) -> client.execute(() -> {
                    if (error != null) {
                        Throwable cause = error.getCause() == null ? error : error.getCause();
                        status = "Freigabe fehlgeschlagen: " + (cause.getMessage() == null ? "Spieler nicht gefunden" : cause.getMessage());
                        grantButton.active = !playerNameField.getText().trim().isEmpty();
                    } else {
                        status = playerName + " wurde freigegeben.";
                        playerNameField.setText("");
                        loadMembers();
                    }
                }));
    }

    private void remove(Member member) {
        client.setScreen(new ConfirmScreen(confirmed -> {
            if (!confirmed) {
                client.setScreen(this);
                return;
            }
            MpsqApiClient.delete("/screens/" + screenId + "/members/" + member.clientId)
                    .whenComplete((ignored, error) -> client.execute(() -> {
                        status = error == null ? member.name + " entfernt." : "Spieler konnte nicht entfernt werden.";
                        loadMembers();
                    }));
        }, Text.literal("Berechtigung entfernen"), Text.literal(member.name + " den Zugriff entziehen?"),
                Text.literal("Entfernen"), Text.literal("Abbrechen")));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = width / 2;
        context.drawCenteredTextWithShadow(textRenderer, title, centerX, 28, MpsqTheme.TEXT_TITEL);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Freigabe per Name oder Aktivierungscode."), centerX, 112, MpsqTheme.TEXT_GEDAEMPT);
        if (!status.isBlank()) context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), centerX, 122, MpsqTheme.TEXT_GEDAEMPT);

        int y = MEMBER_START_Y - memberScrollOffset;
        for (Member member : members) {
            if (y >= MEMBER_START_Y && y <= height - 92) {
                context.drawTextWithShadow(textRenderer, Text.literal(member.name), centerX - BUTTON_WIDTH / 2, y + 6, MpsqTheme.TEXT_NORMAL);
            }
            y += ROW_GAP;
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, width, height);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxMemberScrollOffset <= 0) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        int oldScroll = memberScrollOffset;
        memberScrollOffset = Math.max(0, Math.min(maxMemberScrollOffset,
                memberScrollOffset - (int) (verticalAmount * ROW_GAP)));
        if (oldScroll != memberScrollOffset) clearAndInit();
        return true;
    }

    private record Member(UUID clientId, String name) { }
}
