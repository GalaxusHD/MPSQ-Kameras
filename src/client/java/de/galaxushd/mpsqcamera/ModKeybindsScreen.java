package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Zeigt nur die Tastenbelegungen der MPSQ-Kameras-Mod an und erlaubt das
 * direkte Neubinden per Klick auf den jeweiligen Button.
 */
public class ModKeybindsScreen extends Screen {

    private static final String MOD_CATEGORY = "category.mpsqcamera.main";

    private final Screen parent;
    private final List<KeyBinding> modKeys = new ArrayList<>();
    private KeyBinding pendingRebind = null;

    public ModKeybindsScreen(Screen parent) {
        super(Text.literal("Tastenbelegung – MPSQ Kameras"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        modKeys.clear();
        for (KeyBinding key : this.client.options.allKeys) {
            if (MOD_CATEGORY.equals(key.getCategory())) {
                modKeys.add(key);
            }
        }

        int cx      = this.width / 2;
        int btnW    = 130;
        int startY  = this.height / 2 - modKeys.size() * 14;

        for (KeyBinding key : modKeys) {
            final KeyBinding kb = key;
            boolean isPending = (pendingRebind == kb);

            Text btnText = isPending
                    ? Text.literal("› Taste drücken ‹")
                    : kb.getBoundKeyLocalizedText();

            int rowY = startY + modKeys.indexOf(key) * 28;

            addDrawableChild(ButtonWidget.builder(btnText, b -> {
                pendingRebind = kb;
                clearAndInit();
            }).dimensions(cx + 10, rowY, btnW, 20).build());
        }

        // Alle zurücksetzen
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Alle zurücksetzen"),
                b -> {
                    for (KeyBinding k : modKeys) k.setBoundKey(k.getDefaultKey());
                    KeyBinding.updateKeysByCode();
                    pendingRebind = null;
                    clearAndInit();
                }
        ).dimensions(cx - 155, this.height - 28, 150, 20).build());

        // Zurück
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Zurück"),
                b -> {
                    pendingRebind = null;
                    this.client.setScreen(parent);
                }
        ).dimensions(cx + 5, this.height - 28, 150, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (pendingRebind != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                pendingRebind.setBoundKey(InputUtil.UNKNOWN_KEY);
            } else {
                pendingRebind.setBoundKey(InputUtil.fromKeyCode(keyCode, scanCode));
            }
            KeyBinding.updateKeysByCode();
            pendingRebind = null;
            clearAndInit();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.client.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (pendingRebind != null) {
            pendingRebind.setBoundKey(InputUtil.Type.MOUSE.createFromCode(button));
            KeyBinding.updateKeysByCode();
            pendingRebind = null;
            clearAndInit();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, this.width, this.height);

        int panelW = 380;
        int panelH = modKeys.size() * 28 + 70;
        MpsqTheme.drawPanel(context,
                (this.width - panelW) / 2,
                this.height / 2 - modKeys.size() * 14 - 30,
                panelW, panelH);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx     = this.width / 2;
        int startY = this.height / 2 - modKeys.size() * 14;

        // Titel
        context.drawCenteredTextWithShadow(
                this.textRenderer, this.title,
                cx, startY - 22, MpsqTheme.TEXT_TITEL);

        // Tastenbelegungs-Labels (linke Spalte)
        for (int i = 0; i < modKeys.size(); i++) {
            KeyBinding key = modKeys.get(i);
            int rowY = startY + i * 28 + 5;

            // Hervorhebung wenn Taste gerade umbelegt wird
            if (pendingRebind == key) {
                context.fill(cx - 170, rowY - 2, cx + 145, rowY + 16, 0x33FFFFFF);
            }

            context.drawTextWithShadow(
                    this.textRenderer,
                    Text.translatable(key.getTranslationKey()),
                    cx - 170, rowY,
                    key.isDefault() ? MpsqTheme.TEXT_NORMAL : MpsqTheme.TEXT_TITEL);
        }

        // Hinweis wenn im Warte-Modus
        if (pendingRebind != null) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Drücke eine Taste oder Maustaste • ESC zum Löschen"),
                    cx, this.height - 55, MpsqTheme.TEXT_GEDAEMPT);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
