/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.renderers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

@OnlyIn(Dist.CLIENT)
public final class LaserPointerRenderer implements IItemRenderer {

    private static final float PI = (float) Math.PI;
    private final Tesselator t = Tesselator.getInstance();
    private final BufferBuilder bb = t.getBuilder();
    private final VertexBuffer vb = new VertexBuffer();
    private final FloatBuffer matrix1 = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer renderBuffer = BufferUtils.createFloatBuffer(8);

    public boolean isOn = false;

    public LaserPointerRenderer() {
        for(int i = 0; i < 8; i++)
            renderBuffer.put(0.0f);

        renderBuffer.position(0);
    }

    @Override
    public void render(PoseStack poseStack, ItemStack is, float handSideSign, float swingProgress, float equipProgress, MultiBufferSource multiBufferSource, int packedLight) {
        //This whole method is a fucking hack
        float sqrtSwingProg = (float) Math.sqrt(swingProgress);
        float sinSqrtSwingProg1 = (float) Math.sin(sqrtSwingProg * PI);

        RenderSystem.disableCull();
        RenderSystem.disableTexture();

        //Laser pointer
        glPushMatrix();
        glTranslatef(handSideSign * -0.4f * sinSqrtSwingProg1, (float) (0.2f * Math.sin(sqrtSwingProg * PI * 2.0f)), (float) (-0.2f * Math.sin(swingProgress * PI)));
        glTranslatef(handSideSign * 0.56f, -0.52f - equipProgress * 0.6f, -0.72f);
        glRotatef((float) (handSideSign * (45.0f - Math.sin(swingProgress * swingProgress * PI) * 20.0f)), 0.0f, 1.0f, 0.0f);
        glRotatef(handSideSign * sinSqrtSwingProg1 * -20.0f, 0.0f, 0.0f, 1.0f);
        glRotatef(sinSqrtSwingProg1 * -80.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(handSideSign * -30.0f, 0.0f, 1.0f, 0.0f);
        glTranslatef(0.0f, 0.2f, 0.0f);
        glRotatef(10.0f, 1.0f, 0.0f, 0.0f);
        glScalef(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);

        glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bb.vertex(0.0, 0.0, 0.0).endVertex();
        bb.vertex(1.0, 0.0, 0.0).endVertex();
        bb.vertex(1.0, 0.0, 4.0).endVertex();
        bb.vertex(0.0, 0.0, 4.0).endVertex();

        bb.vertex(0.0, 0.0, 0.0).endVertex();
        bb.vertex(0.0, -1.0, 0.0).endVertex();
        bb.vertex(0.0, -1.0, 4.0).endVertex();
        bb.vertex(0.0, 0.0, 4.0).endVertex();

        bb.vertex(1.0, 0.0, 0.0).endVertex();
        bb.vertex(1.0, -1.0, 0.0).endVertex();
        bb.vertex(1.0, -1.0, 4.0).endVertex();
        bb.vertex(1.0, 0.0, 4.0).endVertex();

        bb.vertex(0.0, -1.0, 4.0).endVertex();
        bb.vertex(1.0, -1.0, 4.0).endVertex();
        bb.vertex(1.0, 0.0, 4.0).endVertex();
        bb.vertex(0.0, 0.0, 4.0).endVertex();
        vb.draw();

        if(isOn) {
            glTranslatef(0.5f, -0.5f, 0.0f);
            matrix1.position(0);
            glGetFloat(GL_MODELVIEW_MATRIX); //Hax to get that damn position
        }

        glPopMatrix();

        if(isOn) {
            //Actual laser
            glPushMatrix();
            glLoadIdentity();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
            glColor4f(1.0f, 0.0f, 0.0f, 0.5f);
            RenderSystem.lineWidth(3.0f);

            matrix1.position(12);
            renderBuffer.put(matrix1.get());
            renderBuffer.put(matrix1.get());
            renderBuffer.put(matrix1.get() - 0.02f); //I know this is stupid, but it's the only thing that worked...
            renderBuffer.put(matrix1.get());
            renderBuffer.position(0);
            glVertexPointer(4, 0, 0 , renderBuffer);
            glEnableClientState(GL_VERTEX_ARRAY);
            glDrawArrays(GL_LINES, 0, 2);
            glDisableClientState(GL_VERTEX_ARRAY);
            glPopMatrix();
        }

        RenderSystem.enableTexture(); //Fix for shitty minecraft fire
    }

}
