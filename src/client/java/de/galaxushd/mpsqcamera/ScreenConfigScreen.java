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

public class ScreenConfigScreen extends Screen {
	private final LocalScreenStore.LocalScreenData target;

	private CyclingButtonWidget<LocalScreenStore.ScreenInputType> modeButton;
	private TextFieldWidget urlField;
	private ButtonWidget cameraButton;

	private final List<CameraChoice> cameraChoices = new ArrayList<>();
	private int cameraIndex = 0;

	public ScreenConfigScreen(LocalScreenStore.LocalScreenData target) {
		super(Text.literal("Screen Config"));
		this.target = target;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int y = this.height / 2 - 60;
		int w = 220;

		modeButton = CyclingButtonWidget.builder(LocalScreenStore.ScreenInputType::text)
			.values(LocalScreenStore.ScreenInputType.values())
			.initially(target.inputType())
			.build(centerX - w / 2, y, w, 20, Text.literal("Input Mode"),
				(button, value) -> updateVisibility(value));
		addDrawableChild(modeButton);

		y += 28;

		urlField = new TextFieldWidget(this.textRenderer, centerX - w / 2, y, w, 20, Text.literal("URL"));
		urlField.setMaxLength(2048);
		urlField.setText(target.url() == null ? "" : target.url());
		addDrawableChild(urlField);

		y += 28;

		// Stub camera list for menu-first phase. Replace with real camera store later.
		cameraChoices.add(new CameraChoice(null, "No Camera"));
		cameraChoices.add(new CameraChoice(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Camera A"));
		cameraChoices.add(new CameraChoice(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Camera B"));

		cameraIndex = findCameraIndex(target.cameraId());

		cameraButton = ButtonWidget.builder(cameraButtonText(), b -> {
			cameraIndex = (cameraIndex + 1) % cameraChoices.size();
			b.setMessage(cameraButtonText());
		}).dimensions(centerX - w / 2, y, w, 20).build();
		addDrawableChild(cameraButton);

		y += 32;

		ButtonWidget saveButton = ButtonWidget.builder(Text.literal("Save"), b -> saveAndClose())
			.dimensions(centerX - w / 2, y, 106, 20).build();
		addDrawableChild(saveButton);

		ButtonWidget closeButton = ButtonWidget.builder(Text.literal("Close"), b -> close())
			.dimensions(centerX + 4, y, 106, 20).build();
		addDrawableChild(closeButton);

		updateVisibility(modeButton.getValue());
	}

	private int findCameraIndex(UUID id) {
		for (int i = 0; i < cameraChoices.size(); i++) {
			CameraChoice c = cameraChoices.get(i);
			if ((c.id == null && id == null) || (c.id != null && c.id.equals(id))) {
				return i;
			}
		}
		return 0;
	}

	private Text cameraButtonText() {
		return Text.literal("Camera: " + cameraChoices.get(cameraIndex).name);
	}

	private void updateVisibility(LocalScreenStore.ScreenInputType mode) {
		boolean link = mode == LocalScreenStore.ScreenInputType.LINK;
		urlField.setVisible(link);
		urlField.setEditable(link);
		cameraButton.visible = !link;
		cameraButton.active = !link;
	}

	private void saveAndClose() {
		LocalScreenStore.ScreenInputType mode = modeButton.getValue();
		String url = urlField.getText();
		UUID cameraId = cameraChoices.get(cameraIndex).id;

		if (mode == LocalScreenStore.ScreenInputType.LINK) {
			cameraId = null;
		} else {
			url = "";
		}

		LocalScreenStore.updateConfig(target.id(), mode, url, cameraId);
		close();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 86, 0xFFFFFF);
		context.drawCenteredTextWithShadow(
			this.textRenderer,
			Text.literal("Target: " + target.anchor().toShortString()),
			this.width / 2,
			this.height / 2 - 74,
			0xAAAAAA
		);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private static final class CameraChoice {
		private final UUID id;
		private final String name;

		private CameraChoice(UUID id, String name) {
			this.id = id;
			this.name = name;
		}
	}
}
