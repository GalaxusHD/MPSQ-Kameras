package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Detail-Menü für einen einzelnen Bildschirm.
 * Unterscheidung zwischen Ersteller und Besucher/Zuschauer.
 */
public class BildschirmDetailScreen extends Screen {
    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ACTIVATION_CODE_LENGTH = 5;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> GENERATED_CODES = new HashSet<>();

    private final Screen parent;
    private final String bildschirmId;
    private final String bildschirmName;
    private final boolean isCreator;

    // Creator-only
    private boolean isCameraMode = true; // default = Kamera
    private String activationCode = generateUniqueActivationCode();
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
        int buttonY = this.height / 2 - 56;
        int buttonX = cx - 90;
        int buttonW = 180;
        int gapY = 26;

        if (isCreator) {
            // ── CREATOR MODE ──────────────────────────────────────────────
            // Kamera/Kino Toggle
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Modus: " + (isCameraMode ? "Kamera" : "Kino")),
                    b -> toggleCameraKino()
            ).dimensions(buttonX, buttonY, buttonW, 20).build());
            buttonY += gapY;

            // Stream-URL Input (nur im Kino-Modus sichtbar)
            if (!isCameraMode) {
                streamUrlField = new TextFieldWidget(this.textRenderer, buttonX, buttonY, buttonW, 20, Text.literal("URL"));
                streamUrlField.setText(streamUrl);
                addDrawableChild(streamUrlField);
                buttonY += gapY;
            }

            // Aktivierungscode (klickbar zum Kopieren)
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Code: " + activationCode),
                    b -> copyActivationCode()
            ).dimensions(buttonX, buttonY, buttonW, 20).build());
            buttonY += gapY;

            // Gruppierung
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Gruppieren..."),
                    b -> openGroupingMenu()
            ).dimensions(buttonX, buttonY, buttonW, 20).build());
            buttonY += gapY;

            // Lösch-Verhalten Toggle (3 Modi)
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Löschen: " + getDeleteBehaviorLabel()),
                    b -> cycleDeleteBehavior()
            ).dimensions(buttonX, buttonY, buttonW, 20).build());
            buttonY += gapY;

            // Bildschirm löschen (in beiden Modi: Kamera + Kino)
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Bildschirm löschen"),
                    b -> deleteScreen()
            ).dimensions(buttonX, buttonY, buttonW, 20).build());
        } else {
            // ── GUEST MODE ────────────────────────────────────────────────
            // Nur Anzeige + Buttons zum Zurück/Verlassen
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Bildschirm verlassen"),
                    b -> leaveScreen()
            ).dimensions(buttonX, buttonY, buttonW, 20).build());
        }

        // Zurück-Button (beide Modi)
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Zurück zur Liste"),
                b -> this.client.setScreen(parent)
        ).dimensions(this.width / 2 - 75, this.height - 36, 150, 20).build());
    }

    private void toggleCameraKino() {
        if (streamUrlField != null) {
            streamUrl = streamUrlField.getText();
        }
        isCameraMode = !isCameraMode;
        clearAndInit();
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
        this.client.setScreen(new BildschirmGroupingScreen(this));
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

    private static synchronized String generateUniqueActivationCode() {
        String code;
        do {
            StringBuilder builder = new StringBuilder(ACTIVATION_CODE_LENGTH);
            for (int i = 0; i < ACTIVATION_CODE_LENGTH; i++) {
                builder.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            code = builder.toString();
        } while (GENERATED_CODES.contains(code));
        GENERATED_CODES.add(code);
        return code;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, this.width, this.height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int startY = 28;

        // Titel
        context.drawCenteredTextWithShadow(
                this.textRenderer, this.title, cx, startY, MpsqTheme.TEXT_TITEL);
        startY += 16;

        // Trennlinie
        context.fill(cx - 130, startY, cx + 130, startY + 1, 0x44FFFFFF);
    }

    @Override
    public boolean shouldPause() { return false; }
}
