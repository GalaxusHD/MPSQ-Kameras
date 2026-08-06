package de.galaxushd.mpsqcamera;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.List;
import java.util.UUID;

/** Assigns one existing camera to a screen. More cameras are added in the next camera-cycle step. */
public final class CameraAssignmentScreen extends Screen {
    private final Screen parent;
    private final UUID screenId;
    private int scroll;
    public CameraAssignmentScreen(Screen parent, UUID screenId) { super(Text.literal("Kamera auswählen")); this.parent = parent; this.screenId = screenId; }
    @Override protected void init() { addDrawableChild(ButtonWidget.builder(Text.translatable("gui.mpsqcamera.back"), b -> client.setScreen(parent)).dimensions(width / 2 - 75, height - 28, 150, 20).build()); }
    @Override public void renderBackground(DrawContext c, int x, int y, float d) { super.renderBackground(c, x, y, d); MpsqTheme.drawBackground(c, width, height); }
    @Override public void render(DrawContext c, int x, int y, float d) {
        super.render(c, x, y, d); int cx=width/2, row=58-scroll; c.drawCenteredTextWithShadow(textRenderer,title,cx,28,MpsqTheme.TEXT_TITEL);
        List<LocalCameraStore.CameraData> cameras=LocalCameraStore.getAll();
        if(cameras.isEmpty()) { c.drawCenteredTextWithShadow(textRenderer,Text.literal("Keine Kameras vorhanden."),cx,70,MpsqTheme.TEXT_GEDAEMPT); return; }
        for(LocalCameraStore.CameraData camera:cameras) { if(row>=54&&row<height-60) { c.fill(cx-140,row,cx+140,row+18,0x33000000); c.drawCenteredTextWithShadow(textRenderer,Text.literal(camera.name()),cx,row+5,MpsqTheme.TEXT_NORMAL); } row+=20; }
    }
    @Override public boolean mouseClicked(double x,double y,int button) { if(button==0) { int row=58-scroll; for(LocalCameraStore.CameraData camera:LocalCameraStore.getAll()) { if(y>=row&&y<row+18) { JsonObject body=new JsonObject(); body.addProperty("cameraId",camera.id().toString()); body.addProperty("sortOrder",0); MpsqApiClient.post("/screens/"+screenId+"/cameras",body).thenCompose(ignored -> ScreenSyncManager.refresh()).whenComplete((ignored,error) -> client.execute(() -> client.setScreen(parent))); return true; } row+=20; } } return super.mouseClicked(x,y,button); }
    @Override public boolean mouseScrolled(double x,double y,double h,double v) { scroll=Math.max(0,scroll-(int)v*20); return true; }
    @Override public boolean shouldPause() { return false; }
}
