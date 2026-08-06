package de.galaxushd.mpsqcamera;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Owner view for access granted through a screen's activation code. */
public final class BildschirmPermissionsScreen extends Screen {
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_GAP = 26;

    private final Screen parent;
    private final UUID screenId;
    private final String activationCode;
    private final List<Member> members = new ArrayList<>();
    private String status = "Lade Berechtigungen...";
    private boolean membersLoaded;

    public BildschirmPermissionsScreen(Screen parent, UUID screenId, String activationCode) {
        super(Text.literal("Berechtigungen"));
        this.parent = parent;
        this.screenId = screenId;
        this.activationCode = activationCode;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Code kopieren: " + activationCode), button -> {
                    client.keyboard.setClipboard(activationCode);
                    status = "Code kopiert. Teile ihn nur mit berechtigten Spielern.";
                })
                .dimensions(width / 2 - BUTTON_WIDTH / 2, 54, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Aktualisieren"), button -> loadMembers())
                .dimensions(width / 2 - BUTTON_WIDTH / 2, height - 54, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), button -> client.setScreen(parent))
                .dimensions(width / 2 - 75, height - 28, 150, BUTTON_HEIGHT)
                .build());
        int memberY = 120;
        for (Member member : members) {
            if (memberY > height - 92) break;
            addDrawableChild(ButtonWidget.builder(Text.literal("Entfernen"), button -> remove(member))
                    .dimensions(width / 2 + BUTTON_WIDTH / 2 - 74, memberY, 74, BUTTON_HEIGHT).build());
            memberY += ROW_GAP;
        }
        if (!membersLoaded) loadMembers();
    }

    private void loadMembers() {
        membersLoaded = false;
        status = "Lade Berechtigungen...";
        MpsqApiClient.get("/screens/" + screenId + "/members")
                .whenComplete((response, error) -> client.execute(() -> {
                    members.clear();
                    if (error != null) {
                        status = "Berechtigungen konnten nicht geladen werden.";
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
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Freigabe erfolgt über den Aktivierungscode."), centerX, 82, MpsqTheme.TEXT_GEDAEMPT);
        if (!status.isBlank()) context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), centerX, 102, MpsqTheme.TEXT_GEDAEMPT);

        int y = 120;
        for (Member member : members) {
            if (y > height - 92) break;
            context.drawTextWithShadow(textRenderer, Text.literal(member.name), centerX - BUTTON_WIDTH / 2, y + 6, MpsqTheme.TEXT_NORMAL);
            y += ROW_GAP;
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, width, height);
    }

    private record Member(UUID clientId, String name) { }
}
