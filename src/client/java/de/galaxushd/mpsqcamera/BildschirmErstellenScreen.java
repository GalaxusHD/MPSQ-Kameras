package de.galaxushd.mpsqcamera;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Locale;

public class BildschirmErstellenScreen extends Screen {
    private final BlockPos pos1;
    private final BlockPos pos2;
    private final Direction clickedSide;
    private final int breite;
    private final int hoehe;
    private final int tiefe;
    private TextFieldWidget nameField;
    private CyclingButtonWidget<LocalScreenStore.ScreenInputType> modusButton;
    private TextFieldWidget urlField;
    private ButtonWidget kameraButton;
    private ButtonWidget createButton;

    public BildschirmErstellenScreen(BlockPos pos1, BlockPos pos2, Direction clickedSide) {
        super(Text.translatable("gui.mpsqcamera.erstellen.titel"));
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.clickedSide = clickedSide == null ? Direction.NORTH : clickedSide;
        breite = Math.abs(pos2.getX() - pos1.getX()) + 1;
        hoehe = Math.abs(pos2.getY() - pos1.getY()) + 1;
        tiefe = Math.abs(pos2.getZ() - pos1.getZ()) + 1;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int fieldWidth = 220;
        int y = height / 2 - 72;
        nameField = new TextFieldWidget(textRenderer, centerX - fieldWidth / 2, y, fieldWidth, 20, Text.translatable("gui.mpsqcamera.erstellen.name"));
        nameField.setPlaceholder(Text.translatable("gui.mpsqcamera.erstellen.name.hinweis"));
        nameField.setMaxLength(64);
        nameField.setChangedListener(ignored -> updateCreateButton());
        addDrawableChild(nameField);
        y += 28;
        addDrawableChild(CyclingButtonWidget.builder(Ausrichtung::getLabel).values(Ausrichtung.values()).initially(Ausrichtung.NORD)
                .build(centerX - fieldWidth / 2, y, fieldWidth, 20, Text.translatable("gui.mpsqcamera.erstellen.ausrichtung")));
        y += 28;
        modusButton = addDrawableChild(CyclingButtonWidget.builder(LocalScreenStore.ScreenInputType::text).values(LocalScreenStore.ScreenInputType.values())
                .initially(LocalScreenStore.ScreenInputType.LINK).build(centerX - fieldWidth / 2, y, fieldWidth, 20,
                        Text.translatable("gui.mpsqcamera.erstellen.modus"), (button, mode) -> updateMode(mode)));
        y += 28;
        urlField = new TextFieldWidget(textRenderer, centerX - fieldWidth / 2, y, fieldWidth, 20, Text.translatable("gui.mpsqcamera.erstellen.url"));
        urlField.setPlaceholder(Text.literal("https://…"));
        urlField.setMaxLength(2048);
        addDrawableChild(urlField);
        y += 28;
        kameraButton = addDrawableChild(ButtonWidget.builder(Text.literal("Kamera: Keine"), button -> { })
                .dimensions(centerX - fieldWidth / 2, y, fieldWidth, 20).build());
        y += 32;
        createButton = addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.erstellen.erstellen"), button -> createScreen())
                .dimensions(centerX - fieldWidth / 2, y, 106, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.erstellen.abbrechen"), button -> close())
                .dimensions(centerX + 4, y, 106, 20).build());
        updateMode(LocalScreenStore.ScreenInputType.LINK);
        updateCreateButton();
    }

    private void updateMode(LocalScreenStore.ScreenInputType mode) {
        boolean linkMode = mode == LocalScreenStore.ScreenInputType.LINK;
        urlField.setVisible(linkMode);
        urlField.setEditable(linkMode);
        kameraButton.visible = !linkMode;
        kameraButton.active = !linkMode;
    }

    private void createScreen() {
        if (client == null || client.player == null || client.world == null) return;
        String name = nameField.getText().trim();
        if (name.isBlank()) {
            updateCreateButton();
            return;
        }
        if (screenNameExists(name)) {
            showStatus("Dieser Bildschirmname ist bereits vergeben.");
            updateCreateButton();
            return;
        }
        LocalScreenStore.ScreenInputType mode = modusButton.getValue();
        String url = mode == LocalScreenStore.ScreenInputType.LINK ? urlField.getText().trim() : "";
        Vec3d createdFrom = client.player.getPos();
        LocalScreenStore.addScreenFromSelection(pos1, pos2, name, mode, url, createdFrom);

        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("mode", mode == LocalScreenStore.ScreenInputType.CAMERA ? "CAMERA" : "KINO");
        body.addProperty("dimension", client.world.getRegistryKey().getValue().toString());
        body.add("pos1", position(pos1));
        body.add("pos2", position(pos2));
        body.addProperty("front", clickedSide.asString().toUpperCase(java.util.Locale.ROOT));
        body.addProperty("cinemaUrl", url);
        MpsqApiClient.post("/screens", body).thenCompose(result -> ScreenSyncManager.refresh()).exceptionally(error -> {
            MpsqCameraClient.LOGGER.warn("Bildschirm konnte nicht synchronisiert werden", error);
            return null;
        });
        close();
    }

    private void updateCreateButton() {
        if (createButton == null || nameField == null) return;
        String name = nameField.getText().trim();
        createButton.active = !name.isBlank() && !screenNameExists(name);
    }

    private static boolean screenNameExists(String name) {
        return LocalScreenStore.getAllScreens().stream()
                .map(LocalScreenStore.LocalScreenData::name)
                .filter(existing -> existing != null)
                .anyMatch(existing -> existing.trim().equalsIgnoreCase(name.trim()));
    }

    private void showStatus(String message) {
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal(message), true);
        }
    }

    private static JsonObject position(BlockPos pos) {
        JsonObject result = new JsonObject();
        result.addProperty("x", pos.getX());
        result.addProperty("y", pos.getY());
        result.addProperty("z", pos.getZ());
        return result;
    }


    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, width, height);
        MpsqTheme.drawPanel(context, (width - 260) / 2, height / 2 - 100, 260, 290);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = width / 2;
        int titleY = height / 2 - 96;
        context.drawCenteredTextWithShadow(textRenderer, title, centerX, titleY, MpsqTheme.TEXT_TITEL);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(breite + " × " + hoehe + " × " + tiefe + " Blöcke"), centerX, titleY + 12, MpsqTheme.TEXT_GEDAEMPT);
        context.fill(centerX - 120, titleY + 25, centerX + 120, titleY + 26, 0x44FFFFFF);
        context.drawTextWithShadow(textRenderer, Text.translatable("gui.mpsqcamera.erstellen.name"), centerX - 110, height / 2 - 84, MpsqTheme.TEXT_NORMAL);
    }

    @Override public boolean shouldPause() { return false; }

    public enum Ausrichtung {
        NORD("Nord"), SUED("Süd"), OST("Ost"), WEST("West"), OBEN("Oben"), UNTEN("Unten");
        private final String label;
        Ausrichtung(String label) { this.label = label; }
        public Text getLabel() { return Text.literal(label); }
    }
}
