package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Konfigurations-Screen für einen einzelnen platzierten Bildschirm.
 * Erlaubt dem Spieler den Eingabe-Modus (Link / Kamera) zu wählen.
 */
public class ScreenConfigScreen extends Screen {

	private final LocalScreenStore.LocalScreenData target;

	private CyclingButtonWidget<LocalScreenStore.ScreenInputType> modusButton;
	private TextFieldWidget urlField;
	private ButtonWidget kameraButton;

	private final List<CameraChoice> kameraAuswahl = new ArrayList<>();
	private int kameraIndex = 0;

	public ScreenConfigScreen(LocalScreenStore.LocalScreenData target) {
		super(Text.translatable("gui.mpsqcamera.bildschirmconfig.titel"));
		this.target = target;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int y  = this.height / 2 - 60;
		int w  = 220;

		// Mode toggle
		modusButton = CyclingButtonWidget.builder(LocalScreenStore.ScreenInputType::text)
				.values(LocalScreenStore.ScreenInputType.values())
				.initially(target.inputType())
				.build(cx - w / 2, y, w, 20,
						Text.translatable("gui.mpsqcamera.bildschirmconfig.eingabemodus"),
						(button, value) -> sichtbarkeitAktualisieren(value));
		addDrawableChild(modusButton);
		y += 28;

		// URL field (Link mode)
		urlField = new TextFieldWidget(this.textRenderer, cx - w / 2, y, w, 20,
				Text.translatable("gui.mpsqcamera.bildschirmconfig.url"));
		urlField.setMaxLength(2048);
		urlField.setPlaceholder(Text.literal("https://…"));
		urlField.setText(target.url() == null ? "" : target.url());
		addDrawableChild(urlField);
		y += 28;

		// Stub camera choices – replace with real CameraStore later
		kameraAuswahl.add(new CameraChoice(null, "Keine Kamera"));
		kameraAuswahl.add(new CameraChoice(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Kamera A"));
		kameraAuswahl.add(new CameraChoice(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Kamera B"));

		kameraIndex = kameraIndexFinden(target.cameraId());

		kameraButton = ButtonWidget.builder(kameraButtonText(), b -> {
			kameraIndex = (kameraIndex + 1) % kameraAuswahl.size();
			b.setMessage(kameraButtonText());
		}).dimensions(cx - w / 2, y, w, 20).build();
		addDrawableChild(kameraButton);
		y += 32;

		// Save / Close
		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.bildschirmconfig.speichern"),
				b -> speichernUndSchliessen()
		).dimensions(cx - w / 2, y, 106, 20).build());

		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.bildschirmconfig.schliessen"),
				b -> close()
		).dimensions(cx + 4, y, 106, 20).build());

		sichtbarkeitAktualisieren(modusButton.getValue());
	}

	// ── Helpers ─────────────────────────────────────────────────────────────

	private int kameraIndexFinden(UUID id) {
		for (int i = 0; i < kameraAuswahl.size(); i++) {
			UUID cId = kameraAuswahl.get(i).id;
			if ((cId == null && id == null) || (cId != null && cId.equals(id))) return i;
		}
		return 0;
	}

	private Text kameraButtonText() {
		return Text.literal("Kamera: " + kameraAuswahl.get(kameraIndex).name);
	}

	private void sichtbarkeitAktualisieren(LocalScreenStore.ScreenInputType modus) {
		boolean link = modus == LocalScreenStore.ScreenInputType.LINK;
		urlField.setVisible(link);
		urlField.setEditable(link);
		kameraButton.visible = !link;
		kameraButton.active  = !link;
	}

	private void speichernUndSchliessen() {
		LocalScreenStore.ScreenInputType modus = modusButton.getValue();
		String url    = urlField.getText();
		UUID kameraId = kameraAuswahl.get(kameraIndex).id;

		if (modus == LocalScreenStore.ScreenInputType.LINK) {
			kameraId = null;
		} else {
			url = "";
		}

		LocalScreenStore.updateConfig(target.id(), modus, url, kameraId);
		close();
	}

	// ── Rendering ───────────────────────────────────────────────────────────

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title,
				this.width / 2, this.height / 2 - 86, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("Anker: " + target.pos1().toShortString()),
				this.width / 2, this.height / 2 - 74, 0xAAAAAA);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	// ── Inner types ─────────────────────────────────────────────────────────

	private static final class CameraChoice {
		final UUID   id;
		final String name;

		CameraChoice(UUID id, String name) {
			this.id   = id;
			this.name = name;
		}
	}
}
