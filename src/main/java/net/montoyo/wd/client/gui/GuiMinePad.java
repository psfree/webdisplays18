/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.client.ClientProxy;
import net.montoyo.wd.utilities.BlockSide;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

import static net.minecraftforge.api.distmarker.Dist.CLIENT;
import static org.lwjgl.opengl.GL11.glColor4f;

@OnlyIn(CLIENT)
public class GuiMinePad extends WDScreen {

    private ClientProxy.PadData pad;
    private double vx;
    private double vy;
    private double vw;
    private double vh;

    public GuiMinePad() {
        super(Component.nullToEmpty(null));
    }

    public GuiMinePad(ClientProxy.PadData pad) {
        this();
        this.pad = pad;
    }

    @Override
    public void init() {
        super.init();

        vw = ((double) width) - 32.0f;
        vh = vw / WebDisplays.PAD_RATIO;
        vx = 16.0f;
        vy = (((double) height) - vh) / 2.0f;
    }

    private static void addRect(BufferBuilder bb, double x, double y, double w, double h) {
        bb.vertex(x, y, 0.0).endVertex();
        bb.vertex(x + w, y, 0.0).endVertex();
        bb.vertex(x + w, y + h, 0.0).endVertex();
        bb.vertex(x, y + h, 0.0).endVertex();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float ptt) {
        renderBackground(poseStack);

        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(0.73f, 0.73f, 0.73f, 1.0f);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder bb = t.getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        addRect(bb, vx, vy - 16, vw, 16);
        addRect(bb, vx, vy + vh, vw, 16);
        addRect(bb, vx - 16, vy, 16, vh);
        addRect(bb, vx + vw, vy, 16, vh);
        t.end();

        RenderSystem.enableTexture();

        if (pad.view != null) {
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            pad.view.draw(vx, vy + vh, vx + vw, vy);
        }

        RenderSystem.enableCull();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        key(keyCode, scanCode, true);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }



    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        key(keyCode, scanCode, false);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void key(int keyCode, int scanCode, boolean pressed) {
        Optional<Character> key = getChar(keyCode, scanCode);

        if (pad.view != null && key.isPresent()) {
            char c = key.get();

            if (pressed)
                pad.view.injectKeyPressedByKeyCode(keyCode, c, 0);
            else
                pad.view.injectKeyReleasedByKeyCode(keyCode, c, 0);

            if (c != 0)
                pad.view.injectKeyTyped(c, 0);
        }

    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        mouse(-1, false, (int) mouseX, (int) mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouse(button, true, (int) mouseX, (int) mouseY);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        mouse(button, false, (int) mouseX, (int) mouseY);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public void mouse(int btn, boolean pressed, int sx, int sy) {

        int vx = screen2DisplayX((int) this.vx);
        int vy = screen2DisplayY((int) this.vy);
        int vh = screen2DisplayX((int) this.vh);
        int vw = screen2DisplayY((int) this.vw);

        if (pad.view != null && sx >= vx && sx <= vx + vw && sy >= vy && sy <= vy + vh) {
            sx -= vx;
            sy -= vy;
            sy = vh - sy;

            //Scale again according to the webview
            sx = (int) (((double) sx) / ((double) vw) *  new WebDisplays().padResX);
            sy = (int) (((double) sy) / ((double) vh) *  new WebDisplays().padResY);

            if (btn == -1)
                pad.view.injectMouseMove(sx, sy, 0, false);
            else
                pad.view.injectMouseButton(sx, sy, 0, btn + 1, pressed, 1);

        }
    }

    public static Optional<Character> getChar(int keyCode, int scanCode) {
        String keystr = GLFW.glfwGetKeyName(keyCode, scanCode);
        if(keystr == null){
            keystr = "\0";
        }
        if(keyCode == GLFW.GLFW_KEY_ENTER){
            keystr = "\n";
        }
        if(keystr.length() == 0){
            return Optional.empty();
        }

        return Optional.of(keystr.charAt(keystr.length() - 1));
    }

    @Override
    public void tick() {
        if(pad.view == null)
            minecraft.setScreen(null); //In case the user dies with the pad in the hand
    }

    @Override
    public boolean isForBlock(BlockPos bp, BlockSide side) {
        return false;
    }

}
