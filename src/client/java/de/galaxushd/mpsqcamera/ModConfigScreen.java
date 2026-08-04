package de.galaxushd.mpsqcamera;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModConfigScreen extends Screen {
	private static final Identifier LOGO_TEXTURE =
			Identifier.of(MpsqCameraClient.MOD_ID, "textures/gui/mpsqlogo.png");
	private static final int LOGO_TEXTURE_SIZE = 864;
	private static final int LOGO_MAX_SIZE = 192;
	private static final int LOGO_TOP_MARGIN = 8;
	private static final int LOGO_BOTTOM_MARGIN = 12;
	private static final int HORIZONTAL_MARGIN = 12;
	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_SPACING = 6;
	private static final int MENU_CONTROL_COUNT = 5;
	private static final int LICENSE_MARGIN = 6;
	private static final int LICENSE_WIDTH = 60;

	private TextFieldWidget codeInputField;
	private ButtonWidget joinButton;

	public ModConfigScreen() {
		super(Text.translatable("gui.mpsqcamera.main.title"));
	}

	@Override
	protected void init() {
		int buttonWidth = Math.min(BUTTON_WIDTH, this.width - HORIZONTAL_MARGIN * 2);
		int menuHeight = MENU_CONTROL_COUNT * BUTTON_HEIGHT
				+ (MENU_CONTROL_COUNT - 1) * BUTTON_SPACING;
		int menuTop = this.height / 2 + Math.max(0, (this.height / 2 - menuHeight) / 2);
		int menuX = (this.width - buttonWidth) / 2;

		codeInputField = new TextFieldWidget(
				this.textRenderer,
				menuX,
				menuTop,
				buttonWidth,
				BUTTON_HEIGHT,
				Text.translatable("gui.mpsqcamera.main.activation.placeholder")
		);
		codeInputField.setMaxLength(5);
		codeInputField.setChangedListener(code -> updateActivationCodeState());
		addDrawableChild(codeInputField);
		setInitialFocus(codeInputField);

		joinButton = addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.main.join"),
				button -> submitCode()
		).dimensions(menuX, nextControlY(menuTop, 1), buttonWidth, BUTTON_HEIGHT).build());

		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.main.screens"),
				button -> openScreens()
		).dimensions(menuX, nextControlY(menuTop, 2), buttonWidth, BUTTON_HEIGHT).build());

		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.main.cameras"),
				button -> openCameras()
		).dimensions(menuX, nextControlY(menuTop, 3), buttonWidth, BUTTON_HEIGHT).build());

		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.main.settings"),
				button -> openSettings()
		).dimensions(menuX, nextControlY(menuTop, 4), buttonWidth, BUTTON_HEIGHT).build());

		addDrawableChild(ButtonWidget.builder(
				Text.translatable("gui.mpsqcamera.hauptmenu.lizenz"),
				button -> openLicense()
		).dimensions(
				LICENSE_MARGIN,
				this.height - LICENSE_MARGIN - BUTTON_HEIGHT,
				LICENSE_WIDTH,
				BUTTON_HEIGHT
		).build());

		updateActivationCodeState();
	}

	private int nextControlY(int menuTop, int index) {
		return menuTop + index * (BUTTON_HEIGHT + BUTTON_SPACING);
	}

	private void updateActivationCodeState() {
		boolean hasFocus = codeInputField.isFocused();
		boolean hasText = !codeInputField.getText().isEmpty();
		codeInputField.setPlaceholder(hasFocus || hasText
				? Text.empty()
				: Text.translatable("gui.mpsqcamera.main.activation.placeholder"));

		if (joinButton != null) {
			joinButton.active = codeInputField.getText().length() == 5;
		}
	}

	private void submitCode() {
		String code = codeInputField.getText();
		if (code.length() != 5) {
			return;
		}

		MpsqCameraClient.LOGGER.info("[MPSQ] Trete Bildschirm mit Code bei: " + code);
		codeInputField.setText("");
	}

	private void openScreens() {
		this.client.setScreen(new BildschirmListScreen(this));
	}

	private void openCameras() {
		// Der Einstiegspunkt bleibt bewusst im Hauptmenü, bis die Kamera-Übersicht existiert.
	}

	private void openSettings() {
		this.client.setScreen(new ModSettingsScreen(this));
	}

	private void openLicense() {
		this.client.setScreen(new LizenzScreen(this));
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fillGradient(0, 0, this.width, this.height, 0xCC1A1A1A, 0xCC050505);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		updateActivationCodeState();
		super.render(context, mouseX, mouseY, delta);

		int availableLogoHeight = this.height / 2 - LOGO_TOP_MARGIN - LOGO_BOTTOM_MARGIN;
		int logoSize = Math.max(1, Math.min(LOGO_MAX_SIZE,
				Math.min(this.width - HORIZONTAL_MARGIN * 2, availableLogoHeight)));
		int logoX = (this.width - logoSize) / 2;

		context.drawTexture(
				RenderPipelines.GUI_TEXTURED,
				LOGO_TEXTURE,
				logoX,
				LOGO_TOP_MARGIN,
				0,
				0,
				logoSize,
				logoSize,
				LOGO_TEXTURE_SIZE,
				LOGO_TEXTURE_SIZE
		);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
