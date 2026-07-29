package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Öffnet sich automatisch nachdem zwei Positionen mit dem Werkzeug markiert wurden.
 * Deutsches Gegenstück zum "Create WatchParty Screen"-Menü.
 *
 * Layout:
 *  ┌──────────────────────────────────┐
 *  │  Bildschirm erstellen            │
 *  │  3 × 2 × 1 Blöcke               │
 *  │  ────────────────────────────    │
 *  │  Name: [__________________]      │
 *  │  Ausrichtung: [Nord ▶]           │
 *  │  Modus: [Link ▶]                 │
 *  │  URL: [https://…_____________]   │
 *  │  ─────────────────────────────   │
 *  │  [  Erstellen  ] [  Abbrechen ]  │
 *  └──────────────────────────────────┘
 */
public class BildschirmErstellenScreen extends Screen {

    private final BlockPos pos1;
    private final BlockPos pos2;

    private TextFieldWidget nameField;
    private CyclingButtonWidget<Ausrichtung> ausrichtungButton;
    private CyclingButtonWidget<LocalScreenStore.ScreenInputType> modusButton;
    private TextFieldWidget urlField;
    private ButtonWidget kameraButton;

    // Automatisch berechnete Größe aus den zwei Positionen
    private final int breite;
    private final int hoehe;
    private final int tiefe;

    public BildschirmErstellenScreen(BlockPos pos1, BlockPos pos2) {
        super(Text.translatable("gui.mpsqcamera.erstellen.titel"));
        this.pos1   = pos1;
        this.pos2   = pos2;
        this.breite = Math.abs(pos2.getX() - pos1.getX()) + 1;
        this.hoehe  = Math.abs(pos2.getY() - pos1.getY()) + 1;
        this.tiefe  = Math.abs(pos2.getZ() - pos1.getZ()) + 1;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int w  = 220;
        int y  = this.height / 2 - 72;

        // ── Name ──────────────────────────────────────────────────────────────
        nameField = new TextFieldWidget(
                this.textRenderer,
                cx - w / 2, y, w, 20,
                Text.translatable("gui.mpsqcamera.erstellen.name")
        );
        nameField.setPlaceholder(Text.translatable("gui.mpsqcamera.erstellen.name.hinweis"));
        nameField.setMaxLength(64);
        addDrawableChild(nameField);
        y += 28;

        // ── Ausrichtung ───────────────────────────────────────────────────────
        ausrichtungButton = CyclingButtonWidget.builder(Ausrichtung::getLabel)
                .values(Ausrichtung.values())
                .initially(Ausrichtung.NORD)
                .build(cx - w / 2, y, w, 20,
                        Text.translatable("gui.mpsqcamera.erstellen.ausrichtung"));
        addDrawableChild(ausrichtungButton);
        y += 28;

        // ── Eingabemodus ──────────────────────────────────────────────────────
        modusButton = CyclingButtonWidget.builder(LocalScreenStore.ScreenInputType::text)
                .values(LocalScreenStore.ScreenInputType.values())
                .initially(LocalScreenStore.ScreenInputType.LINK)
                .build(cx - w / 2, y, w, 20,
                        Text.translatable("gui.mpsqcamera.erstellen.modus"),
                        (btn, val) -> aktualisiereSichtbarkeit(val));
        addDrawableChild(modusButton);
        y += 28;

        // ── URL-Feld (Link-Modus) ─────────────────────────────────────────────
        urlField = new TextFieldWidget(
                this.textRenderer,
                cx - w / 2, y, w, 20,
                Text.translatable("gui.mpsqcamera.erstellen.url")
        );
        urlField.setPlaceholder(Text.literal("https://…"));
        urlField.setMaxLength(2048);
        addDrawableChild(urlField);
        y += 28;

        // ── Kamera-Auswahl (Kamera-Modus) – Stub ─────────────────────────────
        kameraButton = ButtonWidget.builder(
                Text.literal("Kamera: Keine"),
                b -> { /* TODO: Kamera-Auswahl implementieren */ }
        ).dimensions(cx - w / 2, y, w, 20).build();
        addDrawableChild(kameraButton);
        y += 32;

        // ── Erstellen / Abbrechen ─────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.mpsqcamera.erstellen.erstellen"),
                b -> onErstellen()
        ).dimensions(cx - w / 2, y, 106, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.mpsqcamera.erstellen.abbrechen"),
                b -> close()
        ).dimensions(cx + 4, y, 106, 20).build());

        aktualisiereSichtbarkeit(LocalScreenStore.ScreenInputType.LINK);
    }

    // ── Hilfsmethoden ────────────────────────────────────────────────────────

    private void aktualisiereSichtbarkeit(LocalScreenStore.ScreenInputType modus) {
        boolean istLink = modus == LocalScreenStore.ScreenInputType.LINK;
        urlField.setVisible(istLink);
        urlField.setEditable(istLink);
        kameraButton.visible = !istLink;
        kameraButton.active  = !istLink;
    }

    private void onErstellen() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "Bildschirm";
        LocalScreenStore.addScreenFromSelection(pos1, pos2, name);
        MpsqCameraClient.LOGGER.info(
                "[MPSQ Kameras] Bildschirm '{}' erstellt: {} → {}", name, pos1, pos2);
        close();
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, this.width, this.height);

        // Zentriertes Panel
        int panelW = 260;
        int panelH = 290;
        MpsqTheme.drawPanel(context,
                (this.width - panelW) / 2,
                this.height / 2 - 100,
                panelW, panelH);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int cx    = this.width / 2;
        int titleY = this.height / 2 - 96;

        // Titel
        context.drawCenteredTextWithShadow(
                this.textRenderer, this.title, cx, titleY, MpsqTheme.TEXT_TITEL);

        // Größen-Info
        String groesse = breite + " × " + hoehe + " × " + tiefe + " Blöcke";
        context.drawCenteredTextWithShadow(
                this.textRenderer, Text.literal(groesse),
                cx, titleY + 12, MpsqTheme.TEXT_GEDAEMPT);

        // Trennlinie
        context.fill(cx - 120, titleY + 25, cx + 120, titleY + 26, MpsqTheme.WEINROT);

        // Feld-Beschriftung "Name"
        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable("gui.mpsqcamera.erstellen.name"),
                cx - 110, this.height / 2 - 84,
                MpsqTheme.TEXT_NORMAL);
    }

    @Override
    public boolean shouldPause() { return false; }

    // ── Ausrichtung-Enum ─────────────────────────────────────────────────────

    public enum Ausrichtung {
        NORD("Nord"), SUED("Süd"), OST("Ost"), WEST("West"),
        OBEN("Oben"), UNTEN("Unten");

        private final String label;

        Ausrichtung(String label) { this.label = label; }

        public Text getLabel() { return Text.literal(label); }
    }
}
