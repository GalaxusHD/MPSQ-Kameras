package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Hauptmenü des Mods – wird per Tastenkürzel (Standard: M) geöffnet.
 *
 * Layout:
 *  ┌────────────────────────────────────┐  ← Rote Akzentlinie oben
 *  │         [LOGO oben-mitte]          │  ← TODO: echtes Logo-Texture
 *  │         MPSQ Kameras               │
 *  │      [Code eingeben...]            │  ← Code-Input oben
 *  │  ╔══════════════════════════╗      │
 *  │  ║  [ Bildschirme        ] ║      │  ← Buttons (mittig)
 *  │  ║  [ Einstellungen      ] ║      │
 *  │  ╚══════════════════════════╝      │
 *  │ [Lizenz]                           │  ← Unten-links
 *  └────────────────────────────────────┘  ← Weinrote Akzentlinie unten
 */
public class ModConfigScreen extends Screen {

	// Logo-Platzhalter-Abmessungen
	private static final int LOGO_W = 200;
	private static final int LOGO_H = 60;
	private static final int CODE_INPUT_W = 180;
	private static final int CODE_INPUT_H = 20;

	private TextFieldWidget codeInputField;

	public ModConfigScreen() {
		super(Text.translatable("gui.mpsqcamera.hauptmenu.titel"));
	}

	@Override
	protected void init() {
		int cx   = this.width / 2;
		int btnW = 180;
		
		// ── Code-Input-Feld (über den Buttons) ──────────────────────────────────
		codeInputField = new TextFieldWidget(this.textRenderer, cx - CODE_INPUT_W / 2, this.height / 2 - 50, CODE_INPUT_W, CODE_INPUT_H, Text.literal("Code"));
		codeInputField.setPlaceholder(Text.literal("Aktivierungscode eingeben..."));
		codeInputField.setMaxLength(20);
		addDrawableChild(codeInputField);
		
		// Code-Submit-Button (neben Input)
		addDrawableChild(ButtonWidget.builder(
				Text.literal("Beitreten"),
				b -> submitCode()
		).dimensions(cx + CODE_INPUT_W / 2 + 5, this.height / 2 - 50, 70, CODE_INPUT_H).build());

		// Buttons in der unteren Hälfte des Bildschirms
		int btnY = this.height / 2 + 8;

		// "Bildschirme" – Haupt-Button (öffnet BildschirmListScreen)
		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.hauptmenu.bildschirme"),
				b -> onBildschirme()
		).dimensions(cx - btnW / 2, btnY, btnW, 20).build());

		// "Einstellungen"
		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.hauptmenu.einstellungen"),
				b -> onEinstellungen()
		).dimensions(cx - btnW / 2, btnY + 26, btnW, 20).build());

		// "Lizenz" – unten-links
		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.hauptmenu.lizenz"),
				b -> onLizenz()
		).dimensions(6, this.height - 26, 60, 20).build());
	}

	// ── Aktionen ──────────────────────────────────────────────────────────────

	private void submitCode() {
		String code = codeInputField.getText().trim();
		if (code.isEmpty()) {
			MpsqCameraClient.LOGGER.warn("[MPSQ] Code-Feld leer.");
			return;
		}
		
		// Code-Validierung & Join-Anfrage an Backend
		MpsqCameraClient.LOGGER.info("[MPSQ] Trete Bildschirm mit Code bei: " + code);
		// TODO: Backend-Anfrage senden, Bildschirm zur Liste hinzufügen
		
		// Input-Feld leeren
		codeInputField.setText("");
	}

	private void onBildschirme() {
		// Öffne die Bildschirm-Liste
		this.client.setScreen(new BildschirmListScreen(this));
	}

	private void onEinstellungen() {
		this.client.setScreen(new ModSettingsScreen(this));
	}

	private void onLizenz() {
		this.client.setScreen(new LizenzScreen(this));
	}

	// ── Rendering ──────────────────────────────────────────────────────────────

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		MpsqTheme.drawBackground(context, this.width, this.height);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		int cx      = this.width / 2;
		int logoTop = this.height / 4 - LOGO_H / 2;
		int logoX   = cx - LOGO_W / 2;

		// ── Logo-Bereich ─────────────────────────────────────────────────────
		// TODO: Echtes Logo-Texture verwenden, sobald verfügbar:
		//   Identifier logoId = Identifier.of("mpsqcamera", "textures/gui/logo.png");
		//   context.drawTexture(logoId, logoX, logoTop, 0, 0, LOGO_W, LOGO_H, LOGO_W, LOGO_H);
		context.fill(logoX, logoTop, logoX + LOGO_W, logoTop + LOGO_H, 0x55150505);
		context.drawBorder(logoX, logoTop, LOGO_W, LOGO_H, MpsqTheme.WEINROT);
		context.drawCenteredTextWithShadow(
				this.textRenderer,
				Text.translatable("gui.mpsqcamera.hauptmenu.logo_platzhalter"),
				cx, logoTop + LOGO_H / 2 - 4,
				MpsqTheme.TEXT_GEDAEMPT);

		// ── Mod-Titel unterhalb Logo ──────────────────────────────────────────
		context.drawCenteredTextWithShadow(
				this.textRenderer,
				Text.translatable("gui.mpsqcamera.hauptmenu.titel"),
				cx, logoTop + LOGO_H + 8,
				MpsqTheme.TEXT_TITEL);

		// ── Panel hinter den Buttons ──────────────────────────────────────────
		int panelW = 220;
		int panelH = 76;
		MpsqTheme.drawPanel(context, cx - panelW / 2, this.height / 2, panelW, panelH);

		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
