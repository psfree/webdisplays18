/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.renderers;

/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ModelMinePad extends Model {

    // fields
    private final ModelPart base;
    private final ModelPart left;
    private final ModelPart right;

    public ModelMinePad() {
        super();
        textureWidth = 64;
        textureHeight = 32;

        base = new ModelPart(this, 0, 0);
        base.addBox(0F, 0F, 0F, 14, 1, 9);
        base.setRotationPoint(1F, 0F, 3.5F);
        base.setTextureSize(64, 32);
        base.mirror = true;
        clearRotation(base);
        left = new ModelRenderer(this, 0, 10);
        left.addBox(0F, 0F, 0F, 1, 1, 7);
        left.setRotationPoint(0F, 0F, 4.5F);
        left.setTextureSize(64, 32);
        left.mirror = true;
        clearRotation(left);
        right = new ModelRenderer(this, 30, 10);
        right.addBox(0F, 0F, 0F, 1, 1, 7);
        right.setRotationPoint(15F, 0F, 4.5F);
        right.setTextureSize(64, 32);
        right.mirror = true;
        clearRotation(right);
    }

    public final void render(float f5) {
        base.render(f5);
        left.render(f5);
        right.render(f5);
    }

    private void clearRotation(ModelPart model) {
        model.rotateAngleX = 0.0f;
        model.rotateAngleY = 0.0f;
        model.rotateAngleZ = 0.0f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

    }
}*/
