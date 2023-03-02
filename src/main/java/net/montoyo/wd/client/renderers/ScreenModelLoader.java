package net.montoyo.wd.client.renderers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ScreenModelLoader implements IGeometryLoader<ScreenModelLoader.ScreenModelGeometry> {

    public static final ResourceLocation SCREEN_LOADER = new ResourceLocation("webdisplays", "screen_loader");

    public static final ResourceLocation SCREEN_SIDE = new ResourceLocation("webdisplays", "block/screen");

    public static final Material MATERIAL_SIDE = ForgeHooksClient.getBlockMaterial(SCREEN_SIDE);

    @Override
    public ScreenModelGeometry read(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new ScreenModelGeometry();
    }


    public static class ScreenModelGeometry implements IUnbakedGeometry<ScreenModelGeometry> {

        @Override
        public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBakery arg, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides itemOverrides, ResourceLocation arg4) {
            return new ScreenBaker(modelState, spriteGetter, itemOverrides, iGeometryBakingContext.getTransforms());
        }

        @Override
        public Collection<Material> getMaterials(IGeometryBakingContext iGeometryBakingContext, Function<ResourceLocation, UnbakedModel> function, Set<Pair<String, String>> set) {
            return List.of(MATERIAL_SIDE);
        }
    }



}


