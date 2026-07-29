package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

/**
 * Zeigt die Liste aller Bildschirme an, denen der Spieler beigetreten ist.
 * - Bildschirme, die der Spieler erstellt hat (als Creator)
 * - Bildschirme, die der Spieler über Code beigetreten ist (als Gast)
 */
public class BildschirmListScreen extends Screen {

    private final Screen parent;
    private List<BildschirmEntry> bildschirme = new ArrayList<>();
    private int scrollOffset = 0;
    private int selectedIndex = -1;

    public BildschirmListScreen(Screen parent) {
        super(Text.literal("Bildschirme"));
        this.parent = parent;
        // TODO: Bildschirme aus Daten laden (nur beigetretene/erstellte)
        loadJoinedScreens();
    }

    private void loadJoinedScreens() {
        // TODO: Backend-Anfrage: Lade nur Bildschirme, die der Spieler erstellt hat ODER beigetreten ist
        // Beispiel-Daten (später aus Backend):
        // bildschirme.add(new BildschirmEntry("screen_1", "Mein erster Screen", true));
        // bildschirme.add(new BildschirmEntry("screen_2", "Eingeladener Screen", false));
    }

    @Override
    protected void init() {
        // Zurück-Button unten
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Zurück"),
                b -> this.client.setScreen(parent)
        ).dimensions(this.width / 2 - 75, this.height - 36, 150, 20).build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        MpsqTheme.drawBackground(context, this.width, this.height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int startY = 28;

        // Titel
        context.drawCenteredTextWithShadow(
                this.textRenderer, this.title, cx, startY, MpsqTheme.TEXT_TITEL);
        startY += 16;

        // Trennlinie
        context.fill(cx - 130, startY, cx + 130, startY + 1, MpsqTheme.WEINROT);
        startY += 10;

        // Wenn keine Bildschirme
        if (bildschirme.isEmpty()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer, 
                    Text.literal("Keine Bildschirme beigetreten."),
                    cx, startY + 30, 
                    MpsqTheme.TEXT_GEDAEMPT);
            return;
        }

        // Bildschirm-Liste (klickbar)
        int lineY = startY - scrollOffset;
        int lineIdx = 0;
        int maxDisplayY = this.height - 50;

        for (BildschirmEntry bs : bildschirme) {
            if (lineY > startY && lineY < maxDisplayY) {
                int bgColor = (lineIdx == selectedIndex) ? 0x33FFFFFF : 0x00000000;
                context.fill(cx - 140, lineY - 2, cx + 140, lineY + 12, bgColor);
                
                String label = bs.getName() + (bs.isCreator() ? " [CREATOR]" : " [GUEST]");
                context.drawCenteredTextWithShadow(
                        this.textRenderer, Text.literal(label),
                        cx, lineY, MpsqTheme.TEXT_NORMAL);
            }
            lineY += 16;
            lineIdx++;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (int)(verticalAmount * 16);
        scrollOffset = Math.max(0, scrollOffset);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Linksklick
            int cx = this.width / 2;
            int startY = 28 + 16 + 10;
            int lineY = startY - scrollOffset;
            
            for (int i = 0; i < bildschirme.size(); i++) {
                if (lineY > startY && lineY < this.height - 50 &&
                    mouseY >= lineY - 2 && mouseY <= lineY + 12) {
                    selectedIndex = i;
                    // Öffne Detail-Screen
                    BildschirmEntry bs = bildschirme.get(i);
                    this.client.setScreen(new BildschirmDetailScreen(this, bs.getId(), bs.getName(), bs.isCreator()));
                    return true;
                }
                lineY += 16;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() { return false; }

    // ──────────────────────────────────────────────────────────────────────────
    public static class BildschirmEntry {
        private String id;
        private String name;
        private boolean isCreator;

        public BildschirmEntry(String id, String name, boolean isCreator) {
            this.id = id;
            this.name = name;
            this.isCreator = isCreator;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public boolean isCreator() { return isCreator; }
    }
}
