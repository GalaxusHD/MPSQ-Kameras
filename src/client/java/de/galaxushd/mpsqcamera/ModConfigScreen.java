diff --git a/src/client/java/de/galaxushd/mpsqcamera/ModConfigScreen.java b/src/client/java/de/galaxushd/mpsqcamera/ModConfigScreen.java
index cdc153a..0000000 100644
--- a/src/client/java/de/galaxushd/mpsqcamera/ModConfigScreen.java
+++ b/src/client/java/de/galaxushd/mpsqcamera/ModConfigScreen.java
@@ -1,124 +1,136 @@
 package de.galaxushd.mpsqcamera;
 
 import net.minecraft.client.gl.RenderPipelines;
 import net.minecraft.client.gui.DrawContext;
 import net.minecraft.client.gui.screen.Screen;
 import net.minecraft.client.gui.widget.ButtonWidget;
 import net.minecraft.client.gui.widget.TextFieldWidget;
 import net.minecraft.text.Text;
 import net.minecraft.util.Identifier;
 
-/**
- * Hauptmenü des Mods – wird per Tastenkürzel (Standard: K) geöffnet.
- *
- * Layout:
- *  ┌────────────────────────────────────┐  ← Rote Akzentlinie oben
- *  │         [LOGO oben-mitte]          │
- *  │         MPSQ Kameras               │
- *  │      [Code eingeben...]            │  ← Code-Input oben
- *  │  ╔══════════════════════════╗      │  ← Buttons (mittig)
- *  │  ║  [ Bildschirme        ] ║      │
- *  │  ║  [ Einstellungen      ] ║      │
- *  │  ╚══════════════════════════╝      │
- *  │ [Lizenz]                           │  ← Unten-links
- *  └────────────────────────────────────┘  ← Weinrote Akzentlinie unten
- */
 public class ModConfigScreen extends Screen {
-	// Logo-Datei in textures/gui/mixelpixel.png
-	private static final Identifier LOGO_TEXTURE = Identifier.of(MpsqCameraClient.MOD_ID, "textures/gui/mixelpixel.png");
-
-	private static final int LOGO_TEXTURE_W = 512;
-	private static final int LOGO_TEXTURE_H = 128;
-	private static final int LOGO_PAD_TOP   = 8;
-	private static final int CODE_INPUT_W = 180;
-	private static final int CODE_INPUT_H = 20;
+	private static final Identifier LOGO_TEXTURE =
+			Identifier.of(MpsqCameraClient.MOD_ID, "textures/gui/mpsqlogo.png");
+	private static final int LOGO_TEXTURE_SIZE = 512;
+	private static final int LOGO_MAX_SIZE = 128;
+	private static final int LOGO_TOP_MARGIN = 8;
+	private static final int LOGO_BOTTOM_MARGIN = 12;
+	private static final int HORIZONTAL_MARGIN = 12;
+	private static final int BUTTON_WIDTH = 200;
+	private static final int BUTTON_HEIGHT = 20;
+	private static final int BUTTON_SPACING = 6;
+	private static final int MENU_CONTROL_COUNT = 5;
+	private static final int LICENSE_MARGIN = 6;
+	private static final int LICENSE_WIDTH = 60;
 
 	private TextFieldWidget codeInputField;
+	private ButtonWidget joinButton;
 
 	public ModConfigScreen() {
-		super(Text.translatable("gui.mpsqcamera.hauptmenu.titel"));
+		super(Text.translatable("gui.mpsqcamera.main.title"));
 	}
 
 	@Override
 	protected void init() {
-		int cx   = this.width / 2;
-		int btnW = 180;
-
-		// Buttons in der unteren Hälfte (ab 60 % Bildschirmhöhe)
-		int btnAreaY = this.height * 6 / 10;
-
-		// ── Code-Input-Feld ──────────────────────────────────────────────────
-		codeInputField = new TextFieldWidget(this.textRenderer, cx - CODE_INPUT_W / 2, btnAreaY, CODE_INPUT_W, CODE_INPUT_H, Text.literal("Code"));
-		codeInputField.setPlaceholder(Text.literal("Aktivierungscode eingeben..."));
-		codeInputField.setMaxLength(20);
+		int buttonWidth = Math.min(BUTTON_WIDTH, this.width - HORIZONTAL_MARGIN * 2);
+		int menuHeight = MENU_CONTROL_COUNT * BUTTON_HEIGHT
+				+ (MENU_CONTROL_COUNT - 1) * BUTTON_SPACING;
+		int menuTop = this.height / 2 + Math.max(0, (this.height / 2 - menuHeight) / 2);
+		int menuX = (this.width - buttonWidth) / 2;
+
+		codeInputField = new TextFieldWidget(
+				this.textRenderer,
+				menuX,
+				menuTop,
+				buttonWidth,
+				BUTTON_HEIGHT,
+				Text.translatable("gui.mpsqcamera.main.activation.placeholder")
+		);
+		codeInputField.setMaxLength(5);
+		codeInputField.setChangedListener(code -> updateActivationCodeState());
 		addDrawableChild(codeInputField);
 		setInitialFocus(codeInputField);
 
-		// Code-Submit-Button (neben Input)
-		addDrawableChild(ButtonWidget.builder(
-				Text.literal("Beitreten"),
-				b -> submitCode()
-		).dimensions(cx + CODE_INPUT_W / 2 + 5, btnAreaY, 70, CODE_INPUT_H).build());
-
-		int btnY = btnAreaY + 28;
-
-		// "Bildschirme" – Haupt-Button
+		joinButton = addDrawableChild(ButtonWidget.builder(
+				Text.translatable("gui.mpsqcamera.main.join"),
+				button -> submitCode()
+		).dimensions(menuX, nextControlY(menuTop, 1), buttonWidth, BUTTON_HEIGHT).build());
 		addDrawableChild(ButtonWidget.builder(
-				Text.translatable("gui.mpsqcamera.hauptmenu.bildschirme"),
-				b -> onBildschirme()
-		).dimensions(cx - btnW / 2, btnY, btnW, 20).build());
-
-		// "Einstellungen"
+				Text.translatable("gui.mpsqcamera.main.screens"),
+				button -> openScreens()
+		).dimensions(menuX, nextControlY(menuTop, 2), buttonWidth, BUTTON_HEIGHT).build());
+		addDrawableChild(ButtonWidget.builder(
+				Text.translatable("gui.mpsqcamera.main.cameras"),
+				button -> openCameras()
+		).dimensions(menuX, nextControlY(menuTop, 3), buttonWidth, BUTTON_HEIGHT).build());
 		addDrawableChild(ButtonWidget.builder(
-				Text.translatable("gui.mpsqcamera.hauptmenu.einstellungen"),
-				b -> onEinstellungen()
-		).dimensions(cx - btnW / 2, btnY + 28, btnW, 20).build());
-
-		// "Lizenz" – unten-links
+				Text.translatable("gui.mpsqcamera.main.settings"),
+				button -> openSettings()
+		).dimensions(menuX, nextControlY(menuTop, 4), buttonWidth, BUTTON_HEIGHT).build());
 		addDrawableChild(ButtonWidget.builder(
 				Text.translatable("gui.mpsqcamera.hauptmenu.lizenz"),
-				b -> onLizenz()
-		).dimensions(6, this.height - 26, 60, 20).build());
+				button -> openLicense()
+		).dimensions(
+				LICENSE_MARGIN,
+				this.height - LICENSE_MARGIN - BUTTON_HEIGHT,
+				LICENSE_WIDTH,
+				BUTTON_HEIGHT
+		).build());
+		updateActivationCodeState();
 	}
 
-	// ── Aktionen ──────────────────────────────────────────────────────────────
+	private int nextControlY(int menuTop, int index) {
+		return menuTop + index * (BUTTON_HEIGHT + BUTTON_SPACING);
+	}
+
+	private void updateActivationCodeState() {
+		boolean hasFocus = codeInputField.isFocused();
+		boolean hasText = !codeInputField.getText().isEmpty();
+		codeInputField.setPlaceholder(hasFocus || hasText
+				? Text.empty()
+				: Text.translatable("gui.mpsqcamera.main.activation.placeholder"));
+		if (joinButton != null) {
+			joinButton.active = codeInputField.getText().length() == 5;
+		}
+	}
 
 	private void submitCode() {
-		String code = codeInputField.getText().trim();
-		if (code.isEmpty()) {
-			MpsqCameraClient.LOGGER.warn("[MPSQ] Code-Feld leer.");
+		String code = codeInputField.getText();
+		if (code.length() != 5) {
 			return;
 		}
-		
-		// Code-Validierung & Join-Anfrage an Backend
 
 		MpsqCameraClient.LOGGER.info("[MPSQ] Trete Bildschirm mit Code bei: " + code);
-		// TODO: Backend-Anfrage senden, Bildschirm zur Liste hinzufügen
-		
-		// Input-Feld leeren
 		codeInputField.setText("");
 	}
 
-	private void onBildschirme() {
-		// Öffne die Bildschirm-Liste
+	private void openScreens() {
 		this.client.setScreen(new BildschirmListScreen(this));
 	}
 
-	private void onEinstellungen() {
+	private void openCameras() {
+		// Der Einstiegspunkt bleibt bewusst im Hauptmenü, bis die Kamera-Übersicht existiert.
+	}
+
+	private void openSettings() {
 		this.client.setScreen(new ModSettingsScreen(this));
 	}
 
-	private void onLizenz() {
+	private void openLicense() {
 		this.client.setScreen(new LizenzScreen(this));
 	}
 
-	// ── Rendering ──────────────────────────────────────────────────────────────
-
 	@Override
 	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
-		// Dunkler Hintergrund-Gradient damit das Logo sichtbar ist
 		context.fillGradient(0, 0, this.width, this.height, 0xCC1A1A1A, 0xCC050505);
 	}
 
 	@Override
 	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
-		// 1. Hintergrund + Kinder-Elemente (Buttons, TextFields) zuerst rendern
+		updateActivationCodeState();
 		super.render(context, mouseX, mouseY, delta);
 
-		// 2. Logo NACH super.render() zeichnen, damit es nicht von der
-		//    MC-internen Rendering-Pipeline überschrieben wird
-		int cx         = this.width / 2;
-		int logoWidth  = Math.min(this.width - 16, LOGO_TEXTURE_W);
-		int logoHeight = Math.max(1, logoWidth * LOGO_TEXTURE_H / LOGO_TEXTURE_W);
-		int logoX      = cx - logoWidth / 2;
-		int logoY      = LOGO_PAD_TOP;
-
-		context.drawTexture(RenderPipelines.GUI_TEXTURED, LOGO_TEXTURE, logoX, logoY, 0, 0, logoWidth, logoHeight, LOGO_TEXTURE_W, LOGO_TEXTURE_H);
+		int availableLogoHeight = this.height / 2 - LOGO_TOP_MARGIN - LOGO_BOTTOM_MARGIN;
+		int logoSize = Math.max(1, Math.min(LOGO_MAX_SIZE,
+				Math.min(this.width - HORIZONTAL_MARGIN * 2, availableLogoHeight)));
+		int logoX = (this.width - logoSize) / 2;
+		context.drawTexture(
+				RenderPipelines.GUI_TEXTURED,
+				LOGO_TEXTURE,
+				logoX,
+				LOGO_TOP_MARGIN,
+				0,
+				0,
+				logoSize,
+				logoSize,
+				LOGO_TEXTURE_SIZE,
+				LOGO_TEXTURE_SIZE
+		);
 	}
 
 	@Override
 	public boolean shouldPause() {
 		return false;
 	}
 }
