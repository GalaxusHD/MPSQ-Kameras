package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Hauptmenü des Mods – wird per Tastenkürzel (Standard: M) geöffnet.
 *
 * Layout:
 *  ┌────────────────────────────────────┐  ← Rote Akzentlinie oben
 *  │         [LOGO oben-mitte]          │  ← TODO: echtes Logo-Texture
 *  │         MPSQ Kameras               │
 *  │  ╔══════════════════════════╗      │
 *  │  ║  [ Bildschirme        ] ║      │  ← Haupt-Button (funktionsfähig)
 *  │  ║  [ Einstellungen      ] ║      │
 *  │  ╚══════════════════════════╝      │
 *  │ [Lizenz]                           │  ← Unten-links
 *  └────────────────────────────────────┘  ← Weinrote Akzentlinie unten
 */
public class ModConfigScreen extends Screen {

	// Logo-Platzhalter-Abmessungen
	private static final int LOGO_W = 200;
	private static final int LOGO_H = 60;

	public ModConfigScreen() {
		super(Text.translatable("gui.mpsqcamera.hauptmenu.titel"));
	}

	@Override
	protected void init() {
		int cx   = this.width / 2;
		int btnW = 180;
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
		renderBackground(context, mouseX, mouseY, delta);

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
