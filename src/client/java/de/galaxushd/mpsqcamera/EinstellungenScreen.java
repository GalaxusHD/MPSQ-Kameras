package de.galaxushd.mpsqcamera;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Einstellungs-Screen des Mods.
 *
 * Layout:
 *   ┌──────────────────────────────────────────┐
 *   │             [Einstellungen]               │  ← Titel
 *   │  Werkzeug-Item:                           │
 *   │  [  minecraft:arrow          ] [Zurück.]  │  ← Item-Feld + Reset
 *   │                                           │
 *   │  Tastenkürzel:                            │
 *   │  [       Tasten belegen…              ]   │  ← öffnet MC-Steuerung
 *   │                                           │
 *   │  [   Alle Bildschirme: 100%           ]   │  ← Lautstärke-Slider
 *   │                                           │
 *   │  [   Speichern   ]  [      Zurück     ]   │
 *   └──────────────────────────────────────────┘
 */
public class EinstellungenScreen extends Screen {

	private static final int WIDGET_W = 250;

	private final Screen parent;

	private TextFieldWidget toolItemField;
	private boolean         toolItemValid = true;
	private int             volumePercent;

	public EinstellungenScreen(Screen parent) {
		super(Text.translatable("gui.mpsqcamera.einstellungen.titel"));
		this.parent       = parent;
		this.volumePercent = ModConfig.getGlobalVolume();
	}

	// ── Init ─────────────────────────────────────────────────────────────────

	@Override
	protected void init() {
		int cx    = this.width / 2;
		int baseY = this.height / 2 - 80;

		// ── Section 1: Werkzeug-Item ─────────────────────────────────────────
		int fieldW = WIDGET_W - 64;
		toolItemField = new TextFieldWidget(
				this.textRenderer,
				cx - WIDGET_W / 2, baseY,
				fieldW, 20,
				Text.translatable("gui.mpsqcamera.einstellungen.werkzeug_item")
		);
		toolItemField.setMaxLength(128);
		toolItemField.setText(ModConfig.getToolItemId());
		toolItemField.setChangedListener(this::onToolItemChanged);
		addDrawableChild(toolItemField);

		// Reset-Button (rechts neben dem Textfeld)
		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.einstellungen.zuruecksetzen"),
				b -> {
					toolItemField.setText(ModConfig.DEFAULT_TOOL_ITEM_ID);
					onToolItemChanged(ModConfig.DEFAULT_TOOL_ITEM_ID);
				}
		).dimensions(cx - WIDGET_W / 2 + fieldW + 4, baseY, WIDGET_W - fieldW - 4, 20).build());

		// ── Section 2: Tastenbelegung ────────────────────────────────────────
		int keybindY = baseY + 44;
		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.einstellungen.tasten_belegen"),
				b -> MinecraftClient.getInstance().setScreen(
						new ControlsOptionsScreen(this, MinecraftClient.getInstance().options))
		).dimensions(cx - WIDGET_W / 2, keybindY, WIDGET_W, 20).build());

		// ── Section 3: Lautstärke-Slider ────────────────────────────────────
		int volumeY = baseY + 88;
		addDrawableChild(new VolumeSlider(cx - WIDGET_W / 2, volumeY, WIDGET_W, 20));

		// ── Buttons: Speichern + Zurück ──────────────────────────────────────
		int btnY  = baseY + 132;
		int btnW  = (WIDGET_W - 4) / 2;

		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.einstellungen.speichern"),
				b -> saveAndClose()
		).dimensions(cx - WIDGET_W / 2, btnY, btnW, 20).build());

		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.einstellungen.zurueck"),
				b -> close()
		).dimensions(cx - WIDGET_W / 2 + btnW + 4, btnY, WIDGET_W - btnW - 4, 20).build());
	}

	// ── Item validation ───────────────────────────────────────────────────────

	private void onToolItemChanged(String text) {
		toolItemValid = isValidItemId(text);
		toolItemField.setEditableColor(toolItemValid ? 0xE0E0E0 : 0xFF5555);
	}

	private static boolean isValidItemId(String text) {
		if (text == null || text.isBlank()) return false;
		Identifier id = Identifier.tryParse(text.trim());
		if (id == null) return false;
		return Registries.ITEM.containsId(id);
	}

	// ── Save ──────────────────────────────────────────────────────────────────

	private void saveAndClose() {
		String itemText = toolItemField.getText().trim();
		// Only save item if valid; otherwise keep existing value
		if (isValidItemId(itemText)) {
			ModConfig.setToolItemId(itemText);
		}
		ModConfig.setGlobalVolume(volumePercent);
		ModConfig.save();
		close();
	}

	@Override
	public void close() {
		MinecraftClient.getInstance().setScreen(parent);
	}

	// ── Rendering ─────────────────────────────────────────────────────────────

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);

		int cx    = this.width / 2;
		int baseY = this.height / 2 - 80;

		// Titel
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, 20, 0xFFFFFF);

		// Abschnitts-Labels
		context.drawTextWithShadow(
				this.textRenderer,
				Text.translatable("gui.mpsqcamera.einstellungen.werkzeug_item_label"),
				cx - WIDGET_W / 2, baseY - 10, 0xFFFFFF);

		context.drawTextWithShadow(
				this.textRenderer,
				Text.translatable("gui.mpsqcamera.einstellungen.tastenkuerzel_label"),
				cx - WIDGET_W / 2, baseY + 34, 0xFFFFFF);

		super.render(context, mouseX, mouseY, delta);

		// Validierungshinweis unterhalb des Item-Felds
		if (!toolItemValid && !toolItemField.getText().isBlank()) {
			context.drawTextWithShadow(
					this.textRenderer,
					Text.translatable("gui.mpsqcamera.einstellungen.item_ungueltig"),
					cx - WIDGET_W / 2, baseY + 22, 0xFF5555);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	// ── Inner: Lautstärke-Slider ──────────────────────────────────────────────

	private final class VolumeSlider extends SliderWidget {

		VolumeSlider(int x, int y, int width, int height) {
			super(x, y, width, height, Text.empty(), ModConfig.getGlobalVolume() / 100.0);
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			int pct = (int) Math.round(this.value * 100.0);
			setMessage(Text.translatable("gui.mpsqcamera.einstellungen.alle_bildschirme", pct));
		}

		@Override
		protected void applyValue() {
			volumePercent = (int) Math.round(this.value * 100.0);
		}
	}
}
