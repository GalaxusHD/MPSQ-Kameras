package de.galaxushd.mpsqcamera;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

/** Reusable, click-safe camera chooser for screen creation. */
public final class CameraPickerScreen extends Screen {
    private final Screen parent; private final Consumer<LocalCameraStore.CameraData> selected; private int scroll;
    public CameraPickerScreen(Screen parent, Consumer<LocalCameraStore.CameraData> selected) { super(Text.literal("Kamera auswählen")); this.parent = parent; this.selected = selected; }
    @Override protected void init() { addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), b -> client.setScreen(parent)).dimensions(width / 2 - 75, height - 36, 150, 20).build()); }
    @Override public void renderBackground(DrawContext c, int x, int y, float d) { super.renderBackground(c, x, y, d); MpsqTheme.drawBackground(c, width, height); }
    @Override public void render(DrawContext c, int x, int y, float d) { super.render(c, x, y, d); int cx=width/2, row=58-scroll; c.drawCenteredTextWithShadow(textRenderer,title,cx,28,MpsqTheme.TEXT_TITEL); List<LocalCameraStore.CameraData> cameras=LocalCameraStore.getAll(); if(cameras.isEmpty()) { c.drawCenteredTextWithShadow(textRenderer,Text.literal("Keine Kameras vorhanden."),cx,72,MpsqTheme.TEXT_GEDAEMPT); return; } for(LocalCameraStore.CameraData camera:cameras){ if(row>=54&&row<height-62){ c.fill(cx-140,row,cx+140,row+20,0x55000000); c.drawCenteredTextWithShadow(textRenderer,Text.literal(camera.name()),cx,row+6,MpsqTheme.TEXT_NORMAL); } row+=22; } }
    @Override public boolean mouseClicked(double x,double y,int button) { if(button==0){ int row=58-scroll; for(LocalCameraStore.CameraData camera:LocalCameraStore.getAll()){ if(y>=row&&y<row+20&&row>=54&&row<height-62){ selected.accept(camera); client.setScreen(parent); return true;} row+=22; }} return super.mouseClicked(x,y,button); }
    @Override public boolean mouseScrolled(double x,double y,double h,double v){ scroll=Math.max(0,scroll-(int)Math.signum(v)*44); return true; }
    @Override public boolean shouldPause(){ return false; }
}
