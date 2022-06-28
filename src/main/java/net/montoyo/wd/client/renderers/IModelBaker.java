/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.renderers;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.data.IDynamicBakedModel;

public interface IModelBaker extends IDynamicBakedModel{

    void loadTextures(TextureAtlas texMap);

}
