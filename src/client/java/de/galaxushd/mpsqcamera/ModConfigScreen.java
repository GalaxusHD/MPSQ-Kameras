package de.galaxushd.mpsqcamera;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Hauptmenü des Mods – wird per Tastenkürzel (Standard: K) geöffnet.
 *
 * Layout:
 *  ┌────────────────────────────────────┐  ← Rote Akzentlinie oben
 *  │         [LOGO oben-mitte]          │
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
	private static final Identifier LOGO_TEXTURE = Identifier.of(MpsqCameraClient.MOD_ID, "textures/gui/logo.png");

	private static final int LOGO_TEXTURE_W = 512;
	private static final int LOGO_TEXTURE_H = 128;
	private static final int LOGO_W = 256;
	private static final int LOGO_H = 64;
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
		int panelY = this.height / 2 - 14;
		
		// ── Code-Input-Feld (über den Buttons) ──────────────────────────────────
		codeInputField = new TextFieldWidget(this.textRenderer, cx - CODE_INPUT_W / 2, panelY - 38, CODE_INPUT_W, CODE_INPUT_H, Text.literal("Code"));
		codeInputField.setPlaceholder(Text.literal("Aktivierungscode eingeben..."));
		codeInputField.setMaxLength(20);
		addDrawableChild(codeInputField);
		setInitialFocus(codeInputField);
		
		// Code-Submit-Button (neben Input)
		addDrawableChild(ButtonWidget.builder(
				Text.literal("Beitreten"),
				b -> submitCode()
		).dimensions(cx + CODE_INPUT_W / 2 + 5, panelY - 38, 70, CODE_INPUT_H).build());

		// Buttons in der unteren Hälfte des Bildschirms
		int btnY = panelY + 20;

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
		int logoTop = Math.max(18, this.height / 4 - LOGO_H / 2);
		int logoX   = cx - LOGO_W / 2;
		int panelW = 286;
		int panelH = 118;
		int panelX = cx - panelW / 2;
		int panelY = this.height / 2 - 14;

		// ── Hauptmenü-Rahmen / Panel ──────────────────────────────────────────
		MpsqTheme.drawPanel(context, panelX, panelY, panelW, panelH);
		context.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 4, MpsqTheme.ROT);
		context.fill(panelX + 1, panelY + panelH - 4, panelX + panelW - 1, panelY + panelH - 1, MpsqTheme.WEINROT);

		// ── Logo-Bereich ─────────────────────────────────────────────────────
		context.fill(logoX - 12, logoTop - 10, logoX + LOGO_W + 12, logoTop + LOGO_H + 10, 0x55150505);
		context.drawBorder(logoX - 12, logoTop - 10, LOGO_W + 24, LOGO_H + 20, MpsqTheme.WEINROT);
		context.drawTexture(RenderPipelines.GUI_TEXTURED, LOGO_TEXTURE, logoX, logoTop, 0.0F, 0.0F, LOGO_W, LOGO_H, LOGO_TEXTURE_W, LOGO_TEXTURE_H);

		// ── Mod-Titel unterhalb Logo ──────────────────────────────────────────
		context.drawCenteredTextWithShadow(
				this.textRenderer,
				Text.translatable("gui.mpsqcamera.hauptmenu.titel"),
				cx, logoTop + LOGO_H + 14,
				MpsqTheme.TEXT_TITEL);
		context.drawCenteredTextWithShadow(
				this.textRenderer,
				Text.literal("Professionelles Kameramenü"),
				cx, logoTop + LOGO_H + 26,
				MpsqTheme.TEXT_GEDAEMPT);

		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
