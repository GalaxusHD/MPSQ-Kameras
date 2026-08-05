package de.galaxushd.mpsqcamera;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.UUID;

public final class BildschirmDetailScreen extends Screen {
    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_GAP = 26;
    private static final long SEEK_AMOUNT_MS = 10_000L;

    private final Screen parent;
    private final UUID screenId;
    private final boolean isCreator;

    private boolean cameraMode;
    private String activationCode = "------";
    private String streamUrl = "";

    private TextFieldWidget streamUrlField;
    private ButtonWidget saveLinkButton;

    public BildschirmDetailScreen(Screen parent, String screenId, String name, boolean isCreator) {
        super(Text.literal(name));
        this.parent = parent;
        this.screenId = UUID.fromString(screenId);
        this.isCreator = isCreator;
        loadCachedScreenData();
    }

    @Override
    protected void init() {
        loadCachedScreenData();
        activationCode = ScreenAccessStore.code(screenId);

        int x = width / 2 - BUTTON_WIDTH / 2;
        int y = 54;

        if (isCreator) {
            addDrawableChild(ButtonWidget.builder(
                            Text.literal("Modus: " + (cameraMode ? "Kamera" : "Kino")),
                            button -> toggleMode()
                    )
                    .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
            y += ROW_GAP;

            if (cameraMode) {
                addDrawableChild(ButtonWidget.builder(
                                Text.literal("Kameras verwalten..."),
                                button -> client.setScreen(new CameraAssignmentScreen(this, screenId))
                        )
                        .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build());
                y += ROW_GAP;
            } else {
                y = addCinemaControls(x, y);
            }

            addDrawableChild(ButtonWidget.builder(
                            Text.literal("Code: " + activationCode),
                            button -> copyActivationCode()
                    )
                    .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
            y += ROW_GAP;

            addDrawableChild(ButtonWidget.builder(
                            Text.literal("Gruppieren..."),
                            button -> client.setScreen(new BildschirmGroupingScreen(this))
                    )
                    .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
            y += ROW_GAP;

            if (ScreenAccessStore.inGroup(screenId)) {
                addDrawableChild(ButtonWidget.builder(
                                Text.literal("Aus Gruppe entfernen"),
                                button -> removeFromGroup()
                        )
                        .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build());
                y += ROW_GAP;
            }

            addDrawableChild(ButtonWidget.builder(
                            Text.literal("Bildschirm löschen"),
                            button -> confirmDelete()
                    )
                    .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
        } else if (!cameraMode) {
            CinemaPlaybackStore.PlaybackState state = CinemaPlaybackStore.get(screenId);

            addDrawableChild(ButtonWidget.builder(
                            Text.literal(state.playing() ? "Kino läuft" : "Kino angehalten"),
                            button -> {
                            }
                    )
                    .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
        }

        addDrawableChild(ButtonWidget.builder(
                        Text.literal("Zurück zur Liste"),
                        button -> client.setScreen(parent)
                )
                .dimensions(width / 2 - 75, height - 36, 150, BUTTON_HEIGHT)
                .build());
    }

    private int addCinemaControls(int x, int y) {
        streamUrlField = new TextFieldWidget(
                textRenderer,
                x,
                y,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Text.literal("Video- oder Stream-Link")
        );
        streamUrlField.setMaxLength(2048);
        streamUrlField.setPlaceholder(Text.literal("https://youtube.com/... oder https://twitch.tv/..."));
        streamUrlField.setText(streamUrl);
        addDrawableChild(streamUrlField);
        y += ROW_GAP;

        saveLinkButton = addDrawableChild(ButtonWidget.builder(
                        Text.literal("Link speichern"),
                        button -> saveCinemaLink()
                )
                .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        y += ROW_GAP;

        addDrawableChild(ButtonWidget.builder(
                        Text.literal("Link zurücksetzen"),
                        button -> resetCinemaLink()
                )
                .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        y += ROW_GAP;

        openBrowserButton = addDrawableChild(ButtonWidget.builder(
                        Text.literal("Browser öffnen"),
                        button -> openBrowser()
                )
                .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        y += ROW_GAP;

        updateLinkButtons(streamUrlField.getText());
        streamUrlField.setChangedListener(this::updateLinkButtons);

        CinemaPlaybackStore.PlaybackState state = CinemaPlaybackStore.get(screenId);

        addDrawableChild(ButtonWidget.builder(
                        Text.literal(state.playing() ? "Stop" : "Start"),
                        button -> setPlaying(!state.playing())
                )
                .dimensions(x, y, 88, BUTTON_HEIGHT)
                .build());

        addDrawableChild(ButtonWidget.builder(
                        Text.literal("-10 Sekunden"),
                        button -> seek(-SEEK_AMOUNT_MS)
                )
                .dimensions(x + 92, y, 88, BUTTON_HEIGHT)
                .build());
        y += ROW_GAP;

        addDrawableChild(ButtonWidget.builder(
                        Text.literal("+10 Sekunden"),
                        button -> seek(SEEK_AMOUNT_MS)
                )
                .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        return y + ROW_GAP;
    }

    private void updateLinkButtons(String value) {
        boolean valid = CinemaBrowserManager.normalizeHttpUrl(value) != null;

        if (saveLinkButton != null) {
            saveLinkButton.active = valid;
        }

        if (openBrowserButton != null) {
            openBrowserButton.active = valid;
        }
    }

    private void openBrowser() {
        String url = CinemaBrowserManager.normalizeHttpUrl(streamUrlField.getText());

        if (url != null) {
            client.setScreen(new CinemaBrowserScreen(this, url));
        }
    }

    private void loadCachedScreenData() {
        LocalScreenStore.LocalScreenData screen = LocalScreenStore.findById(screenId).orElse(null);

        if (screen != null) {
            cameraMode = screen.inputType() == LocalScreenStore.ScreenInputType.CAMERA;
            streamUrl = screen.url() == null ? "" : screen.url();
        }
    }

    private void toggleMode() {
        if (cameraMode && ScreenCameraStore.hasCameras(screenId)) {
            showStatus("Entferne zuerst alle Kameras von diesem Bildschirm.");
            return;
        }

        if (!cameraMode && !streamUrl.isBlank()) {
            showStatus("Setze zuerst den Kino-Link zurück.");
            return;
        }

        boolean newCameraMode = !cameraMode;

        JsonObject body = new JsonObject();
        body.addProperty("mode", newCameraMode ? "CAMERA" : "KINO");

        MpsqApiClient.patch("/screens/" + screenId, body)
                .thenCompose(ignored -> ScreenSyncManager.refresh())
                .whenComplete((ignored, error) -> client.execute(() -> {
                    if (error != null) {
                        showStatus("Modus konnte nicht gespeichert werden.");
                    } else {
                        clearAndInit();
                    }
                }));
    }

    private void saveCinemaLink() {
        String url = CinemaBrowserManager.normalizeHttpUrl(streamUrlField.getText());

        if (url == null) {
            showStatus("Bitte gib einen gültigen http(s)-Link ein.");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("mode", "KINO");
        body.addProperty("cinemaUrl", url);

        saveScreenPatch(body, "Link gespeichert.");
    }

    private void resetCinemaLink() {
        JsonObject body = new JsonObject();
        body.addProperty("cinemaUrl", "");
        body.add("playbackState", playbackJson(false, 0L));

        saveScreenPatch(body, "Link zurückgesetzt.");
    }

    private void setPlaying(boolean playing) {
        CinemaPlaybackStore.PlaybackState state = CinemaPlaybackStore.get(screenId);

        JsonObject body = new JsonObject();
        body.add("playbackState", playbackJson(
                playing,
                currentPlaybackPosition(state)
        ));

        saveScreenPatch(body, playing ? "Kino gestartet." : "Kino gestoppt.");
    }

    private void seek(long delta) {
        CinemaPlaybackStore.PlaybackState state = CinemaPlaybackStore.get(screenId);

        JsonObject body = new JsonObject();
        body.add("playbackState", playbackJson(
                state.playing(),
                Math.max(0L, currentPlaybackPosition(state) + delta)
        ));

        saveScreenPatch(body, "Position geändert.");
    }

    private JsonObject playbackJson(boolean playing, long positionMs) {
        CinemaPlaybackStore.PlaybackState previous = CinemaPlaybackStore.get(screenId);

        JsonObject playback = new JsonObject();
        playback.addProperty("playing", playing);
        playback.addProperty("positionMs", positionMs);
        playback.addProperty("revision", previous.revision() + 1L);

        return playback;
    }

    private long currentPlaybackPosition(CinemaPlaybackStore.PlaybackState state) {
        if (!state.playing() || state.updatedAtMs() <= 0L) {
            return state.positionMs();
        }

        return Math.max(
                0L,
                state.positionMs() + System.currentTimeMillis() - state.updatedAtMs()
        );
    }

    private void saveScreenPatch(JsonObject body, String successMessage) {
        MpsqApiClient.patch("/screens/" + screenId, body)
                .thenCompose(ignored -> ScreenSyncManager.refresh())
                .whenComplete((ignored, error) -> client.execute(() -> {
                    if (error != null) {
                        showStatus("Änderung konnte nicht gespeichert werden.");
                    } else {
                        showStatus(successMessage);
                        clearAndInit();
                    }
                }));
    }

    private void copyActivationCode() {
        client.keyboard.setClipboard(activationCode);
        showStatus("Code kopiert.");
    }

    private void removeFromGroup() {
        MpsqApiClient.post("/screens/" + screenId + "/remove-from-group", new JsonObject())
                .thenCompose(ignored -> ScreenSyncManager.refresh())
                .whenComplete((ignored, error) -> client.execute(() -> client.setScreen(parent)));
    }

    private void confirmDelete() {
        boolean inGroup = ScreenAccessStore.inGroup(screenId);

        client.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        MpsqApiClient.delete("/screens/" + screenId)
                                .thenCompose(ignored -> ScreenSyncManager.refresh())
                                .whenComplete((ignored, error) ->
                                        client.execute(() -> client.setScreen(parent))
                                );
                    } else {
                        client.setScreen(this);
                    }
                },
                Text.literal("Bildschirm löschen"),
                Text.literal(inGroup
                        ? "Die gesamte Gruppe wird gelöscht."
                        : "Bildschirm wirklich löschen?"),
                Text.literal("Löschen"),
                Text.literal("Abbrechen")
        ));
    }

    private void showStatus(String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message), true);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int centerX = width / 2;

        context.drawCenteredTextWithShadow(
                textRenderer,
                title,
                centerX,
                28,
                MpsqTheme.TEXT_TITEL
        );
        context.fill(centerX - 130, 44, centerX + 130, 45, 0x44FFFFFF);

        if (!cameraMode) {
            LocalScreenStore.LocalScreenData screen = LocalScreenStore.findById(screenId).orElse(null);

            if (screen != null) {
                CinemaBrowserManager.ScreenStatus status = CinemaBrowserManager.status(screen);
                String label = status == CinemaBrowserManager.ScreenStatus.NONE
                        ? "Kino bereit"
                        : status.label();

                int color = status == CinemaBrowserManager.ScreenStatus.NONE
                        ? MpsqTheme.TEXT_GEDAEMPT
                        : (status.red() << 16) | (status.green() << 8) | status.blue();

                context.drawCenteredTextWithShadow(
                        textRenderer,
                        Text.literal(label),
                        centerX,
                        46,
                        color
                );
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
