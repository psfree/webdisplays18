/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.montoyo.wd.client.gui.GuiRedstoneCtrl;
import net.montoyo.wd.utilities.Vector3i;

public class RedstoneCtrlData extends GuiData {

    public ResourceLocation dimension;
    public Vector3i pos;
    public String risingEdgeURL;
    public String fallingEdgeURL;
    
    public RedstoneCtrlData() {
        super();
    }
    
//    public RedstoneCtrlData(FriendlyByteBuf buf) {
//        super(buf);
//    }
    
    public RedstoneCtrlData(ResourceLocation d, BlockPos p, String r, String f) {
        dimension = d;
        pos = new Vector3i(p);
        risingEdgeURL = r;
        fallingEdgeURL = f;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Screen createGui(Screen old, Level world) {
        return new GuiRedstoneCtrl(old.getTitle(), dimension, pos, risingEdgeURL, fallingEdgeURL); //TODO is getTitle() correct?
    }

    @Override
    public String getName() {
        return "RedstoneCtrl";
    }

}
