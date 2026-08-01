package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;

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
    private String activationCode;
    private String streamUrl = "";
    private LocalScreenStore.DeleteBehavior deleteBehavior = LocalScreenStore.DeleteBehavior.NIE;
    private TextFieldWidget streamUrlField;

    public BildschirmDetailScreen(Screen parent, String bildschirmId, String name, boolean isCreator) {
        super(Text.literal(name));
        this.parent = parent;
        this.bildschirmId = bildschirmId;
        this.bildschirmName = name;
        this.isCreator = isCreator;
        // Zeige den gemeinsamen Gruppencode, falls vorhanden
        this.activationCode = resolveActivationCode(bildschirmId);
    }

    /** Gibt den Code zurück: Gruppencode wenn in Gruppe, sonst Screen-eigenen Code. */
    private static String resolveActivationCode(String screenIdStr) {
        try {
            UUID id = UUID.fromString(screenIdStr);
            Optional<LocalScreenStore.LocalGroupData> group = LocalScreenStore.getGroupForScreen(id);
            return group.map(LocalScreenStore.LocalGroupData::sharedCode).orElse("-----");
        } catch (IllegalArgumentException e) {
            return "-----";
        }
    }

    @Override
    protected void init() {
        // Aktivierungscode bei jedem init() aktualisieren (z.B. nach Gruppierung)
        activationCode = resolveActivationCode(bildschirmId);

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

            // Lösch-Verhalten Toggle (3 Modi) – aktualisiert sich nach Klick
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Löschen: " + deleteBehavior.getLabel()),
                    b -> cycleDeleteBehavior()
            ).dimensions(buttonX, buttonY, buttonW, 20).build());
            buttonY += gapY;

            // Bildschirm löschen (in beiden Modi: Kamera + Kino)
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Bildschirm löschen"),
                    b -> confirmDeleteScreen()
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

    /** Wechselt den Löschmodus und aktualisiert sofort die Anzeige. */
    private void cycleDeleteBehavior() {
        deleteBehavior = deleteBehavior.next();
        clearAndInit();
    }

    private void copyActivationCode() {
        this.client.keyboard.setClipboard(activationCode);
        MpsqCameraClient.LOGGER.info("[MPSQ] Code " + activationCode + " kopiert.");
    }

    private void openGroupingMenu() {
        MpsqCameraClient.LOGGER.info("[MPSQ] Gruppieren für " + bildschirmName);
        this.client.setScreen(new BildschirmGroupingScreen(this));
    }

    /** Öffnet einen Bestätigungs-Dialog bevor der Bildschirm gelöscht wird. */
    private void confirmDeleteScreen() {
        boolean isInGroup = isScreenInGroup();
        String confirmText = isInGroup
                ? "Bildschirm '" + bildschirmName + "' ist Teil einer Gruppe.\nDie gesamte Gruppe wird gelöscht!"
                : "Bildschirm '" + bildschirmName + "' wirklich löschen?";

        this.client.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        deleteScreen();
                    } else {
                        this.client.setScreen(this);
                    }
                },
                Text.literal("Bildschirm löschen"),
                Text.literal(confirmText),
                Text.literal("Ja, löschen"),
                Text.literal("Abbrechen")
        ));
    }

    /** Löscht den Bildschirm (oder die ganze Gruppe falls in Gruppe). */
    private void deleteScreen() {
        try {
            UUID id = UUID.fromString(bildschirmId);
            boolean wasInGroup = isScreenInGroup();
            LocalScreenStore.removeScreen(id);
            if (wasInGroup) {
                MpsqCameraClient.LOGGER.info("[MPSQ] Gruppe von Bildschirm " + bildschirmName + " gelöscht.");
            } else {
                MpsqCameraClient.LOGGER.info("[MPSQ] Bildschirm " + bildschirmName + " gelöscht.");
            }
        } catch (IllegalArgumentException e) {
            MpsqCameraClient.LOGGER.warn("[MPSQ] Ungültige Bildschirm-ID: " + bildschirmId);
        }
        this.client.setScreen(parent);
    }

    private boolean isScreenInGroup() {
        try {
            UUID id = UUID.fromString(bildschirmId);
            return LocalScreenStore.getGroupForScreen(id).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void leaveScreen() {
        MpsqCameraClient.LOGGER.info("[MPSQ] Besucher verlässt Bildschirm " + bildschirmName);
        this.client.setScreen(parent);
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
