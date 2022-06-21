/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.data;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.montoyo.wd.client.gui.GuiRedstoneCtrl;
import net.montoyo.wd.utilities.Vector3i;

public class RedstoneCtrlData extends GuiData {

    public int dimension;
    public Vector3i pos;
    public String risingEdgeURL;
    public String fallingEdgeURL;

    public RedstoneCtrlData() {
    }

    public RedstoneCtrlData(int d, BlockPos p, String r, String f) {
        dimension = d;
        pos = new Vector3i(p);
        risingEdgeURL = r;
        fallingEdgeURL = f;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Screen createGui(Screen old, Level world) {
        return new GuiRedstoneCtrl(dimension, pos, risingEdgeURL, fallingEdgeURL);
    }

    @Override
    public String getName() {
        return "RedstoneCtrl";
    }

}
