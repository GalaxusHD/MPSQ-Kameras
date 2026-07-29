package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Hauptmenü des Mods – wird per Tastenkürzel geöffnet.
 *
 * Layout:
 *   ┌──────────────────────────────┐
 *   │        [LOGO-BEREICH]        │
 *   │   ________________________   │
 *   │  | Zugangscode eingeben…  |  │
 *   │  |________________________|  │
 *   │  [Bildschirm erstellen]      │
 *   │  [Einstellungen]             │
 *   │                              │
 *   │ [Lizenz]                     │
 *   └──────────────────────────────┘
 */
public class ModConfigScreen extends Screen {

	// Dimensions of the logo placeholder box
	private static final int LOGO_W = 160;
	private static final int LOGO_H = 50;

	private TextFieldWidget zugangscodeField;

	public ModConfigScreen() {
		super(Text.translatable("gui.mpsqcamera.hauptmenu.titel"));
	}

	@Override
	protected void init() {
		int cx = this.width / 2;

		// Logo box sits in the upper third of the screen
		int logoTop = this.height / 4 - LOGO_H / 2;

		// Zugangscode input – centered, just below logo
		int fieldY = logoTop + LOGO_H + 14;
		int fieldW = LOGO_W + 40;

		zugangscodeField = new TextFieldWidget(
				this.textRenderer,
				cx - fieldW / 2, fieldY,
				fieldW, 20,
				Text.translatable("gui.mpsqcamera.hauptmenu.zugangscode")
		);
		zugangscodeField.setPlaceholder(
				Text.translatable("gui.mpsqcamera.hauptmenu.zugangscode.hinweis")
		);
		zugangscodeField.setMaxLength(64);
		addDrawableChild(zugangscodeField);

		// "Bildschirm erstellen" button
		int btnW = fieldW;
		int btnY = fieldY + 28;
		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.hauptmenu.bildschirm_erstellen"),
				b -> onBildschirmErstellen()
		).dimensions(cx - btnW / 2, btnY, btnW, 20).build());

		// "Einstellungen" button
		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.hauptmenu.einstellungen"),
				b -> onEinstellungen()
		).dimensions(cx - btnW / 2, btnY + 26, btnW, 20).build());

		// "Lizenz" – bottom-left corner
		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.hauptmenu.lizenz"),
				b -> onLizenz()
		).dimensions(6, this.height - 26, 60, 20).build());
	}

	// ── Actions ─────────────────────────────────────────────────────────────

	private void onBildschirmErstellen() {
		String code = zugangscodeField.getText().trim();
		if (!code.isEmpty()) {
			MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Zugangscode eingegeben: {}", code);
		}
		// TODO: Bildschirm-Erstellungslogik einbauen (Shift+Klick weiterhin möglich)
		this.close();
	}

	private void onEinstellungen() {
		// TODO: Einstellungen-Screen öffnen
		MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Einstellungen (noch nicht implementiert).");
	}

	private void onLizenz() {
		// TODO: Lizenz-Dialog oder externer Link
		MpsqCameraClient.LOGGER.info("[MPSQ Kameras] Lizenz: LGPL-2.1-or-later");
	}

	// ── Rendering ───────────────────────────────────────────────────────────

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);

		int cx = this.width / 2;
		int logoTop = this.height / 4 - LOGO_H / 2;

		// Logo placeholder – filled rectangle + border
		int logoX = cx - LOGO_W / 2;
		context.fill(logoX, logoTop, logoX + LOGO_W, logoTop + LOGO_H, 0x55000000);
		// Border (1px)
		context.drawBorder(logoX, logoTop, LOGO_W, LOGO_H, 0xFFAAAAAA);
		// Center text inside the box
		context.drawCenteredTextWithShadow(
				this.textRenderer,
				Text.translatable("gui.mpsqcamera.hauptmenu.logo_platzhalter"),
				cx, logoTop + LOGO_H / 2 - 4,
				0xAAAAAA
		);

		// Label above the text field
		context.drawCenteredTextWithShadow(
				this.textRenderer,
				Text.translatable("gui.mpsqcamera.hauptmenu.zugangscode"),
				cx, logoTop + LOGO_H + 3,
				0xFFFFFF
		);

		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
