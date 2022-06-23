/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.client.ClientProxy;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL;

@OnlyIn(Dist.CLIENT)
public final class MinePadRenderer implements IItemRenderer {

    private static final float PI = (float) Math.PI;
    private final Minecraft mc = Minecraft.getInstance();
    private final ResourceLocation tex = new ResourceLocation("webdisplays", "textures/models/minepad.png");
    private final ModelMinePad model = new ModelMinePad();
    private final ClientProxy clientProxy = (ClientProxy) WebDisplays.PROXY;

    private float sinSqrtSwingProg1;
    private float sinSqrtSwingProg2;
    private float sinSwingProg1;
    private float sinSwingProg2;

    public static void drawAxis() {
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_LINES);
        glColor4f(1.f, 0.f, 0.f, 1.f); glVertex3d(0.0, 0.0, 0.0);
        glColor4f(1.f, 0.f, 0.f, 1.f); glVertex3d(5.0, 0.0, 0.0);
        glColor4f(0.f, 1.f, 0.f, 1.f); glVertex3d(0.0, 0.0, 0.0);
        glColor4f(0.f, 1.f, 0.f, 1.f); glVertex3d(0.0, 5.0, 0.0);
        glColor4f(0.f, 0.f, 1.f, 1.f); glVertex3d(0.0, 0.0, 0.0);
        glColor4f(0.f, 0.f, 1.f, 1.f); glVertex3d(0.0, 0.0, 5.0);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    @Override
    public final void render(PoseStack stack, ItemStack is, float handSideSign, float swingProgress, float equipProgress) {
        //Pre-compute values
        float sqrtSwingProg = (float) Math.sqrt(swingProgress);
        sinSqrtSwingProg1 = (float) Math.sin(sqrtSwingProg * PI);
        sinSqrtSwingProg2 = (float) Math.sin(sqrtSwingProg * PI * 2.0f);
        sinSwingProg1 = (float) Math.sin(swingProgress * PI);
        sinSwingProg2 = (float) Math.sin(swingProgress * swingProgress * PI);

        glDisable(GL_CULL_FACE);
        glEnable(GL_RESCALE_NORMAL);

        //Render arm
        glPushMatrix();
        renderArmFirstPerson(equipProgress, handSideSign);
        glPopMatrix();

        //Prepare minePad transform
        glPushMatrix();
        glTranslatef(handSideSign * -0.4f * sinSqrtSwingProg1, 0.2f * sinSqrtSwingProg2, -0.2f * sinSwingProg1);
        glTranslatef(handSideSign * 0.56f, -0.52f - equipProgress * 0.6f, -0.72f);
        glRotatef(handSideSign * (45.0f - sinSwingProg2 * 20.0f), 0.0f, 1.0f, 0.0f);
        glRotatef(handSideSign * sinSqrtSwingProg1 * -20.0f, 0.0f, 0.0f, 1.0f);
        glRotatef(sinSqrtSwingProg1 * -80.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(handSideSign * -45.0f, 0.0f, 1.0f, 0.0f);

        if(handSideSign >= 0.0f)
            glTranslatef(-1.065f, 0.0f, 0.0f);
        else {
            glTranslatef(0.0f, 0.0f, -0.2f);
            glRotatef(20.0f, 0.0f, 1.0f, 0.0f);
            glTranslatef(-0.475f, -0.1f, 0.0f);
            glRotatef(1.0f, 0.0f, 0.0f, 1.0f);
        }

        //Render model
        glPushMatrix();
        glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
//TODO        mc.renderEngine.bindTexture(tex);
        model.render(1.f / 16.f);
        glPopMatrix();

        //Render web view
        if(is.getTag() != null && is.getTag().contains("PadID")) {
            ClientProxy.PadData pd = clientProxy.getPadByID(is.getTag().getInt("PadID"));

            if(pd != null) {
                glTranslatef(0.063f, 0.28f, 0.001f);
                glDisable(GL_TEXTURE_2D);
                pd.view.draw(0.0, 0.0, 27.65 / 32.0 + 0.01, 14.0 / 32.0 + 0.002);
            }
        }

        glPopMatrix();
        glDisable(GL_RESCALE_NORMAL);
        glEnable(GL_CULL_FACE);
    }

    private void renderArmFirstPerson(float equipProgress, float handSideSign) {
        float tx = -0.3f * sinSqrtSwingProg1;
        float ty = 0.4f * sinSqrtSwingProg2;
        float tz = -0.4f * sinSwingProg1;

        glTranslatef(handSideSign * (tx + 0.64000005f), ty - 0.6f - equipProgress * 0.6f, tz - 0.71999997f);
        glRotatef(handSideSign * 45.0f, 0.0f, 1.0f, 0.0f);
        glRotatef(handSideSign * sinSqrtSwingProg1 * 70.0f, 0.0f, 1.0f, 0.0f);
        glRotatef(handSideSign * sinSwingProg2 * -20.0f, 0.0f, 0.0f, 1.0f);
        glTranslatef(-handSideSign, 3.6f, 3.5f);
        glRotatef(handSideSign * 120.0f, 0.0f, 0.0f, 1.0f);
        glRotatef(200.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(handSideSign * -135.0f, 0.0f, 1.0f, 0.0f);
        glTranslatef(handSideSign * 5.6f, 0.0f, 0.0f);

        /*RenderPlayer playerRenderer = (RenderPlayer) mc.getRenderManager().<AbstractClientPlayer>getEntityRenderObject(mc.player);
        mc.getTextureManager().bindTexture(mc.player.getLocationSkin());

        if(handSideSign >= 0.0f)
            playerRenderer.renderRightArm(mc.player);
        else
            playerRenderer.renderLeftArm(mc.player);*/
    }

}
