package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Detail-Menü für einen einzelnen Bildschirm.
 * Unterscheidung zwischen Ersteller und Besucher/Zuschauer.
 */
public class BildschirmDetailScreen extends Screen {

    private final Screen parent;
    private final String bildschirmId;
    private final String bildschirmName;
    private final boolean isCreator;

    // Creator-only
    private boolean isCameraMode = true; // default = Kamera
    private String activationCode = "ABC123XYZ"; // TODO: Aus Daten laden
    private String streamUrl = "";
    private String deleteBehavior = "nie"; // "verlassen", "alle", "nie"
    private TextFieldWidget streamUrlField;

    public BildschirmDetailScreen(Screen parent, String bildschirmId, String name, boolean isCreator) {
        super(Text.literal(name));
        this.parent = parent;
        this.bildschirmId = bildschirmId;
        this.bildschirmName = name;
        this.isCreator = isCreator;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int buttonY = this.height / 2 - 30;

        if (isCreator) {
            // ── CREATOR MODE ──────────────────────────────────────────────
            // Kamera/Kino Toggle
            addDrawableChild(ButtonWidget.builder(
                    Text.literal(isCameraMode ? "[Kamera]" : "[Kino]"),
                    b -> toggleCameraKino()
            ).dimensions(cx - 60, buttonY, 120, 20).build());
            buttonY += 30;

            // Stream-URL Input (nur im Kino-Modus sichtbar)
            if (!isCameraMode) {
                streamUrlField = new TextFieldWidget(this.textRenderer, cx - 100, buttonY, 200, 20, Text.literal("URL"));
                streamUrlField.setText(streamUrl);
                addDrawableChild(streamUrlField);
                buttonY += 30;
            }

            // Aktivierungscode (klickbar zum Kopieren)
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Code: " + activationCode),
                    b -> copyActivationCode()
            ).dimensions(cx - 80, buttonY, 160, 20).build());
            buttonY += 30;

            // Gruppierung
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Gruppieren..."),
                    b -> openGroupingMenu()
            ).dimensions(cx - 80, buttonY, 160, 20).build());
            buttonY += 30;

            // Lösch-Verhalten Toggle (3 Modi)
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Löschen: " + getDeleteBehaviorLabel()),
                    b -> cycleDeleteBehavior()
            ).dimensions(cx - 80, buttonY, 160, 20).build());
            buttonY += 30;

            // Bildschirm/Gruppe löschen
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Bildschirm löschen"),
                    b -> deleteScreen()
            ).dimensions(cx - 80, buttonY, 160, 20).build());
        } else {
            // ── GUEST MODE ────────────────────────────────────────────────
            // Nur Anzeige + Buttons zum Zurück/Verlassen
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Bildschirm verlassen"),
                    b -> leaveScreen()
            ).dimensions(cx - 80, buttonY, 160, 20).build());
        }

        // Zurück-Button (beide Modi)
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Zurück zur Liste"),
                b -> this.client.setScreen(parent)
        ).dimensions(this.width / 2 - 75, this.height - 36, 150, 20).build());
    }

    private void toggleCameraKino() {
        isCameraMode = !isCameraMode;
        this.init(); // Neurender mit aktualisierten Buttons
    }

    private void cycleDeleteBehavior() {
        if (deleteBehavior.equals("nie")) {
            deleteBehavior = "verlassen";
        } else if (deleteBehavior.equals("verlassen")) {
            deleteBehavior = "alle";
        } else {
            deleteBehavior = "nie";
        }
    }

    private String getDeleteBehaviorLabel() {
        return switch (deleteBehavior) {
            case "verlassen" -> "Nach Verlassen";
            case "alle" -> "Wenn alle weg";
            default -> "Nie";
        };
    }

    private void copyActivationCode() {
        this.client.keyboard.setClipboard(activationCode);
        MpsqCameraClient.LOGGER.info("[MPSQ] Code " + activationCode + " kopiert.");
    }

    private void openGroupingMenu() {
        MpsqCameraClient.LOGGER.info("[MPSQ] Gruppieren für " + bildschirmName);
        // TODO: Gruppierungs-Screen öffnen
    }

    private void deleteScreen() {
        MpsqCameraClient.LOGGER.info("[MPSQ] Bildschirm " + bildschirmName + " gelöscht.");
        // TODO: Bildschirm löschen & zur Liste zurück
        this.client.setScreen(parent);
    }

    private void leaveScreen() {
        MpsqCameraClient.LOGGER.info("[MPSQ] Besucher verlässt Bildschirm " + bildschirmName);
        // TODO: Bildschirm als nicht mehr sichtbar markieren
        this.client.setScreen(parent);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, this.width, this.height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int startY = 28;

        // Titel
        context.drawCenteredTextWithShadow(
                this.textRenderer, this.title, cx, startY, MpsqTheme.TEXT_TITEL);
        startY += 16;

        // Trennlinie
        context.fill(cx - 130, startY, cx + 130, startY + 1, MpsqTheme.WEINROT);
    }

    @Override
    public boolean shouldPause() { return false; }
}
