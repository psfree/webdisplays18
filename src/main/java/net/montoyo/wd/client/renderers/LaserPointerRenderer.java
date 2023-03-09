/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.renderers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LaserPointerRenderer implements IItemRenderer {

    private static final float PI = (float) Math.PI;
    private final Tesselator t = Tesselator.getInstance();
    private final BufferBuilder bb = t.getBuilder();

    public LaserPointerRenderer() {}

    @Override
    public void render(PoseStack poseStack, ItemStack is, float handSideSign, float swingProgress, float equipProgress, MultiBufferSource multiBufferSource, int packedLight) {
        //This whole method is a fucking hack
        float sqrtSwingProg = (float) Math.sqrt(swingProgress);
        float sinSqrtSwingProg1 = (float) Math.sin(sqrtSwingProg * PI);

        RenderSystem.disableCull();
        RenderSystem.disableTexture();

        //Laser pointer
        poseStack.pushPose();
        poseStack.translate(handSideSign * -0.4f * sinSqrtSwingProg1, (float) (0.2f * Math.sin(sqrtSwingProg * PI * 2.0f)), (float) (-0.2f * Math.sin(swingProgress * PI)));
        poseStack.translate(handSideSign * 0.56f, -0.52f - equipProgress * 0.6f, -0.72f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees((float) (handSideSign * (45.0f - Math.sin(swingProgress * swingProgress * PI) * 20.0f))));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(handSideSign * sinSqrtSwingProg1 * -20.0f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(sinSqrtSwingProg1 * -80.0f));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(handSideSign * -30.0f));
        poseStack.translate(0.0f, 0.2f, 0.0f);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(10.0f));
        poseStack.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);

        RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1.0f);

        var matrix = poseStack.last().pose();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bb.vertex(matrix, 0.0f, 0.0f, 0.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 1.0f, 0.0f, 0.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 1.0f, 0.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 0.0f, 0.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();

        bb.vertex(matrix, 0.0f, 0.0f, 0.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 0.0f, -1.0f, 0.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 0.0f, -1.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 0.0f, 0.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();

        bb.vertex(matrix,1.0f, 0.0f, 0.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix,1.0f, -1.0f, 0.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix,1.0f, -1.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix,1.0f, 0.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();

        bb.vertex(matrix, 0.0f, -1.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 1.0f, -1.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 1.0f, 0.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        bb.vertex(matrix, 0.0f, 0.0f, 4.0f).color(0.5f, 0.5f, 0.5f, 1.0f).endVertex();
        t.end();

        drawLine(bb, t, matrix);
        RenderSystem.enableTexture(); //Fix for shitty minecraft fire
        RenderSystem.enableCull();
        poseStack.popPose();
    }

    public static void drawLine(BufferBuilder bb, Tesselator t, Matrix4f matrix) {
        GlStateManager._enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(255f, 0f,0f, 127.5f);
        RenderSystem.enableDepthTest();
        bb.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);

        bb.vertex(matrix, 0, 0, 0).color(255f, 0f,0f, 127.5f).endVertex();
        bb.vertex(matrix, 20, 0, 20).color(255f, 0f,0f, 127.5f).endVertex();
        t.end();
        GlStateManager._disableBlend();
    }
}
