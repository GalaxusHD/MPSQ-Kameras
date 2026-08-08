package de.galaxushd.mpsqcamera;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Confirmation for a request received from another MPSQ user. */
public final class BodycamRequestScreen extends Screen {
    private final String requestId;
    private final String requesterName;

    public BodycamRequestScreen(String requestId, String requesterName) {
        super(Text.literal("Bodycam-Anfrage"));
        this.requestId = requestId;
        this.requesterName = requesterName;
    }

    @Override protected void init() {
        int x = width / 2 - 110;
        int y = height / 2 + 12;
        addDrawableChild(ButtonWidget.builder(Text.literal("Akzeptieren"), button -> respond(true)).dimensions(x, y, 106, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Ablehnen"), button -> respond(false)).dimensions(x + 114, y, 106, 20).build());
    }

    private void respond(boolean accepted) {
        JsonObject body = new JsonObject();
        body.addProperty("accepted", accepted);
        MpsqApiClient.post("/bodycam-requests/" + requestId + "/respond", body)
                .thenCompose(ignored -> MpsqApiClient.refreshCameras())
                .whenComplete((ignored, error) -> client.execute(this::close));
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 42, MpsqTheme.TEXT_TITEL);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(requesterName + " möchte deine Perspektive verwenden."), width / 2, height / 2 - 16, MpsqTheme.TEXT_NORMAL);
    }
    @Override public void renderBackground(DrawContext c, int x, int y, float d) { super.renderBackground(c, x, y, d); MpsqTheme.drawBackground(c, width, height); }
    @Override public boolean shouldPause() { return false; }
}
