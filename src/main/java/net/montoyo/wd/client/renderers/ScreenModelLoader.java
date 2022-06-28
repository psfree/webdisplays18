package net.montoyo.wd.client.renderers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ScreenModelLoader implements IModelLoader<ScreenModelLoader.ScreenModelGeometry> {

    public static final ResourceLocation SCREEN_LOADER = new ResourceLocation("webdisplays", "screen_loader");

    public static final ResourceLocation SCREEN_SIDE = new ResourceLocation("webdisplays", "block/screen");

    public static final Material MATERIAL_SIDE = ForgeHooksClient.getBlockMaterial(SCREEN_SIDE);

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }

    @Override
    public ScreenModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new ScreenModelGeometry();
    }


    public static class ScreenModelGeometry implements IModelGeometry<ScreenModelGeometry> {
        @Override
        public BakedModel bake(IModelConfiguration owner, ModelBakery modelBakery, Function<net.minecraft.client.resources.model.Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides itemOverrides, ResourceLocation resourceLocation) {
            return new ScreenBaker(modelState, spriteGetter, itemOverrides, owner.getCameraTransforms());
        }



        @Override
        public Collection<net.minecraft.client.resources.model.Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            return List.of(MATERIAL_SIDE);
        }
    }



}


